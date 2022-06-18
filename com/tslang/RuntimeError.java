package com.tslang;



class RuntimeError extends RuntimeException {
    final Token token;
    RuntimeError(Token token, String message) {
        super(message);
        //prevent app from generating IR code
        Main.hadError=true;
        this.token = token;
    }
}