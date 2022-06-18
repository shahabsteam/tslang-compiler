package com.tslang;

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    public String identifier_type;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    Token(TokenType type, String lexeme, Object literal, int line,String identifier_type) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.identifier_type=identifier_type;
    }

    public String toString() {

         return type + " " + lexeme + " " + literal+" "+identifier_type;
    }
}
