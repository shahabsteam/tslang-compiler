package com.tslang;

public class IR {
    public String print;
    public String varName;
    public String type;
    public int literal;

    public int getLiteral() {
        return literal;
    }

    public void setLiteral(int literal) {
        this.literal = literal;
    }

    public IR(String print, String varName) {
        this.print = print;
        this.varName = varName;
    }

    public IR(String print, String varName, String type) {
        this.print = print;
        this.varName = varName;
        this.type = type;
    }

    public void setPrint(String print) {
        this.print = print;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrint() {
        return print;
    }

    public String getVarName() {
        return varName;
    }

    public String getType() {
        return type;
    }

}
