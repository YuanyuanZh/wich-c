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
package wich.codegen.model.expr;

import org.antlr.symtab.Type;
import wich.codegen.model.ModelElement;
import wich.codegen.model.WichType;

/** An operation on two operands. Split out the operation into subclasses
 *  for primitive, string, vector operand types so the auto-template-construction
 *  allows a different template per operand type. See maps such as
 *  CPrimitiveBinaryOpMap in wich.stg, for example.
 */
public abstract class BinaryOpExpr extends Expr {
	public Type resultType;
	public String wichOp;
	@ModelElement public Expr left;
	@ModelElement public Expr right;

	public BinaryOpExpr(Expr left, String op, Expr right, WichType type, String tempVar) {
		this.left = left;
		this.wichOp = op;
		this.right = right;
		this.type = type;
		this.varRef = tempVar;
	}

	@Override
	public Type getType() {
		return resultType;
	}
}
