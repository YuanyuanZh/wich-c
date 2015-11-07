package wich.codegen.model.expr;

import org.antlr.symtab.Type;
import wich.codegen.model.ModelElement;
import wich.codegen.model.WichType;
import wich.semantics.symbols.WVariableSymbol;

public class VarRef extends Expr {
	public WVariableSymbol symbol;
	public boolean isAssign;

	public VarRef(WVariableSymbol symbol, WichType type, boolean isAssign) {
		this.type = type;
		this.symbol = symbol;
		this.isAssign = isAssign;
	}

	public String getName() { return symbol.getName(); }

	@Override
	public Type getType() {
		return symbol.getType();
	}
}
