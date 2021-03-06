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

import org.antlr.symtab.Scope;
import org.junit.Test;
import wich.codegen.CompilerUtils;
import wich.errors.WichErrorHandler;
import wich.semantics.SymbolTable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestSymbolDefs {
	@Test
	public void testPredefinedTypes() throws Exception {
		String input =
			"";
		String expecting =
			"predefined {\n" +
			"    int\n" +
			"    float\n" +
			"    string\n" +
			"    []\n" +
			"    boolean\n" +
			"}\n";
		boolean includePredefined = true;
		checkAllScopes(input, includePredefined, expecting);
	}

	@Test
	public void testEmptyInput() throws Exception {
		String input =
			"";
		String expecting =
			"global {\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test public void testDefineGlobalSymbol() throws Exception {
		String input = "var x = 1\n";
		String expecting =
			"global {\n" +
			"    global.x:int\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testGlobalVector() throws Exception {
		String input =
			"var i = [1,2,3]\n";
		String expecting =
			"global {\n" +
			"    global.i:[]\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testGlobalString() throws Exception {
		String input =
			"var s = \"hi\"\n";
		String expecting =
			"global {\n" +
			"    global.s:string\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testGlobalBoolean() throws Exception {
		String lava =
				"var s = (1 == 1)";
		String expecting =
				"global {\n" +
						"    global.s:boolean\n" +
						"}\n";
		checkScopes(lava, expecting);
	}


	@Test
	public void testMultipleGlobal() throws Exception {
		String lava =
				"var i = 1\n" +
				"var f = 3.14" +
				"var j = \"hi\"\n" +
				"var k = [1,2,3.3]\n" +
				"var b = (1 == 1)\n";
		String expecting =
				"global {\n" +
				"    global.i:int\n" +
				"    global.f:float\n" +
				"    global.j:string\n" +
				"    global.k:[]\n" +
				"    global.b:boolean\n" +
				"}\n";
		checkScopes(lava, expecting);
	}

	@Test
	public void testFuncNoArgs() throws Exception {
		String input =
			"func f() { }\n";
		String expecting =
			"global {\n" +
			"    f {\n" +
			"        local {\n" +
			"        }\n" +

			"    }\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testFuncArgs() throws Exception {
		String input =
			"func f(x : int, y : []) { }\n";
		String expecting =
			"global {\n" +
			"    f {\n" +
			"        f.x:int\n" +
			"        f.y:[]\n" +
			"        local {\n" +
			"        }\n" +
			"    }\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testFuncLocals() throws Exception {
		String input =
			"func f() { var i = 3 var c = \"hi\" }\n";
		String expecting =
			"global {\n" +
			"    f {\n" +
			"        local {\n" +
			"            local.i:int\n" +
			"            local.c:string\n" +
			"        }\n" +
			"    }\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testFuncNestedLocals() throws Exception {
		String input =
			"func f() { var i = 3 if ( i>3 ) { var c = \"hi\" } }\n";
		String expecting =
			"global {\n" +
			"    f {\n" +
			"        local {\n" +
			"            local.i:int\n" +
			"            local {\n" +
			"                local.c:string\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testFuncArgsLocals() throws Exception {
		String input =
			"func f(x : int) { var i = 3 if ( i>3 ) { var c = \"hi\" } }\n";
		String expecting =
			"global {\n" +
			"    f {\n" +
			"        f.x:int\n" +
			"        local {\n" +
			"            local.i:int\n" +
			"            local {\n" +
			"                local.c:string\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n";
		checkScopes(input, expecting);
	}

	@Test
	public void testBooleanVar() throws Exception {
		String input =
				"var y = true\n";
		String expecting =
				"global {\n" +
						"    global.y:boolean\n" +
						"}\n";
		checkScopes(input, expecting);
	}


	public void checkScopes(String input, String expecting) throws IOException {
		boolean includePredefined = false;
		checkAllScopes(input, includePredefined, expecting);
	}

	public void checkAllScopes(String input, boolean includePredefined, String expecting) {
		SymbolTable symtab = new SymbolTable();
		WichErrorHandler err = new WichErrorHandler();
		CompilerUtils.getAnnotatedParseTree(input, symtab, err);
		Scope scope = symtab.getGlobalScope();
		if ( includePredefined ) scope = symtab.getPredefinedScope();
		String result = SymbolTable.dump(scope);
		assertEquals(expecting, result);
	}
}
