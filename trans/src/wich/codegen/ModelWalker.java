/*
The MIT License (MIT)

Copyright (c) 2015 Terence Parr, Hanzhou Shi, Shuai Yuan, Yuanyuan Zhang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package wich.codegen;

import org.antlr.symtab.Utils;
import wich.codegen.model.ModelElement;
import wich.codegen.model.OutputModelObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// visitor methods:
// return null means delete. return same object means don't replace. return diff object means replace.

public class ModelWalker {
	public static final Object NOTFOUND = new Object();
	public static final OutputModelObject NO_RESULT = new OutputModelObject();

	protected final Object listener;
	protected final Map<Class<?>, Object> visitorMethodCache = new HashMap<>();
	protected Object visitEveryModelObjectMethodCache = null;

	public ModelWalker(Object listener) {
		this.listener = listener;
	}

	public OutputModelObject walk(OutputModelObject omo) {
		if ( omo==null ) return NO_RESULT;

		final OutputModelObject result1 = visit(omo);
		final OutputModelObject result2 = visitEveryModelObject(omo);

		Class<? extends OutputModelObject> cl = omo.getClass();
		Field[] allAnnotatedFields = Utils.getAllAnnotatedFields(cl);
		List<Field> modelFields = new ArrayList<>();
		for (Field fi : allAnnotatedFields) {
			ModelElement annotation = fi.getAnnotation(ModelElement.class);
			if ( annotation != null ) {
				modelFields.add(fi);
			}
		}

		// WALK EACH NESTED MODEL OBJECT MARKED WITH @ModelElement
		for (Field fi : modelFields) {
			ModelElement annotation = fi.getAnnotation(ModelElement.class);
			if (annotation == null) {
				continue;
			}

			String fieldName = fi.getName();
			try {
				Object o = fi.get(omo);
				if ( o instanceof OutputModelObject ) {  // SINGLE MODEL OBJECT?
					OutputModelObject nestedOmo = (OutputModelObject)o;
					final OutputModelObject replacement = walk(nestedOmo);
					if ( replacement!=NO_RESULT && replacement!=nestedOmo ) {
						fi.set(omo, replacement);
					}
				}
				else if ( o instanceof OutputModelObject[] ) {
					walkArray((OutputModelObject[])o);
				}
				else if ( o instanceof List ) {
					walkList((List<OutputModelObject>)o);
				}
				else if ( o instanceof Map ) {
					walkMap((Map<Object, OutputModelObject>)o);
				}
				else if ( o!=null ) {
					System.err.println("type of "+fieldName+"'s model element isn't recognized: "+o.getClass().getSimpleName());
				}
			}
			catch (IllegalAccessException iae) {
				System.err.printf("Can't access field: "+fieldName+" in "+cl.getSimpleName());
			}
		}

		return result1!=NO_RESULT ? result1 : result2;
	}

	protected void walkArray(OutputModelObject[] elems) {
		int i = 0;
		while ( i < elems.length ) {
			OutputModelObject nestedOmo = elems[i];
			if ( nestedOmo==null ) continue;
			final OutputModelObject replacement = walk(nestedOmo);
			if ( replacement==null ) { // null means delete (shift array elements down) (expensive)
				System.arraycopy(elems,i+1,elems, i,elems.length-i-1);
				continue; // skip i++ as we deleted
			}
			else if ( replacement!=NO_RESULT && replacement!=nestedOmo ) {
				elems[i] = replacement;
			}
			i++;
		}
	}

	protected void walkList(List<OutputModelObject> nestedOmos) {
		int i = 0;
		while ( i < nestedOmos.size() ) {
			OutputModelObject nestedOmo = nestedOmos.get(i);
			if ( nestedOmo!=null ) {
				final OutputModelObject replacement = walk(nestedOmo);
				if ( replacement==null ) { // null means delete
					nestedOmos.remove(i);
					continue; // skip i++ as we deleted
				}
				else if ( replacement!=NO_RESULT && replacement != nestedOmo ) {
					nestedOmos.set(i, replacement);
				}
			}
			i++;
		}
	}

	protected void walkMap(Map<Object, OutputModelObject> nestedOmoMap) {
		for (Map.Entry<?, OutputModelObject> entry : nestedOmoMap.entrySet()) {
			final OutputModelObject nestedOmo = entry.getValue();
			final OutputModelObject replacement = walk(nestedOmo);
			if ( replacement==null ) { // null means delete
				nestedOmoMap.remove(entry.getKey());
			}
			else if ( replacement!=NO_RESULT && replacement!=nestedOmo ) {
				nestedOmoMap.put(entry.getKey(), replacement);
			}
		}
	}

	/** Use reflection to find & invoke overloaded visit(modeltype) method */
	protected OutputModelObject visit(OutputModelObject omo) {
		final Method m = getVisitorMethodForType(omo.getClass());
		return execVisit(omo, m);
	}

	protected OutputModelObject visitEveryModelObject(OutputModelObject omo) {
		final Method m = getVisitEveryNodeMethod();
		return execVisit(omo, m);
	}

	protected OutputModelObject execVisit(OutputModelObject omo, Method m) {
		Object result = NO_RESULT;
		if ( m!=null ) {
			try {
				result = m.invoke(listener, omo);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (OutputModelObject)result;
	}

	protected Method getVisitorMethodForType(Class cl) {
		Object m = visitorMethodCache.get(cl); // reflection is slow; cache.
		if ( m!=null ) {
			if ( m==NOTFOUND ) {
				return null;
			}
			return (Method)m;
		}
		try {
			m = listener.getClass().getMethod("visit", cl);
			visitorMethodCache.put(cl, m);
		}
		catch (NoSuchMethodException nsme) {
			m = null;
			visitorMethodCache.put(cl, NOTFOUND);
		}
		return (Method)m;
	}

	protected Method getVisitEveryNodeMethod() {
		if ( visitEveryModelObjectMethodCache!=null ) {
			if ( visitEveryModelObjectMethodCache==NOTFOUND ) {
				return null;
			}
			return (Method)visitEveryModelObjectMethodCache;
		}
		Method m;
		try {
			m = listener.getClass().getMethod("visitEveryModelObject", OutputModelObject.class);
			visitEveryModelObjectMethodCache = m;
		}
		catch (NoSuchMethodException nsme) {
			m = null;
			visitEveryModelObjectMethodCache = NOTFOUND;
		}
		return m;
	}
}