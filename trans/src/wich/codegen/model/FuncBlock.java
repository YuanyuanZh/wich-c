package wich.codegen.model;

public class FuncBlock extends Block {
	public Func enclosingFunc;

	public FuncBlock() {
		super(null, FUNC_BLOCK_NUMBER);
	}
}
