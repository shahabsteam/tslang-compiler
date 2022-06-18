package com.tslang;

public class Function {
    public final Stmt.Function declaration;
    Function(Stmt.Function declaration) {
        this.declaration = declaration;
    }


    public int arity() {
        if (declaration.params==null)
            return 0;
        return declaration.params.size();
    }

}
