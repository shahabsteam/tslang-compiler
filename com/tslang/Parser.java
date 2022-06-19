package com.tslang;

import java.util.ArrayList;
import java.util.List;
import static com.tslang.TokenType.*;
class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
    private Token currentFunction;
    private Environment environment = new Environment();
    Parser(List<Token> tokens) {

        this.tokens = tokens;
    }

    public Environment getEnvironment() {
        return environment;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
        Stmt declare =    proc();
       // if(declare==null)
           // break;
            statements.add(declare);
        }
        return statements;
    }
    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
       // if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        //check if the next token is identifier since we do not have a keyword for identifier like var or let we will consume it later
        if(check(IDENTIFIER)){
           advance();
           if(check(COLON)){
               getBack();

              return  varDeclaration();

           }
           getBack();
        }
        return expressionStatement();}
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();


        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
    //    System.out.println(peek()+" "+tokens.get(current+1));
        return statements;
    }
    private Stmt returnStatement() {
        Token keyword = previous();

        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
       // System.out.println(value+" "+currentFunction);
        return new Stmt.Return(currentFunction, value);
    }
    private Stmt forStatement() {
       // consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;
        if(check(IDENTIFIER)){
            Token forIdentfier = peek();
            advance();
            consume(COMMA,"must use , after specifying identifier");
            if(check(IDENTIFIER)){
                Token iterator = peek();
                advance();
                consume(LEFT_ARROW,"Left arrow after identifier");
                Expr iteratorExpr = expression();
                consume(COLON,"COLON after iterator expression");
                Stmt forStatement = statement();
                return new Stmt.For(forIdentfier,iterator,iteratorExpr,forStatement);


            }else{
                throw    error(peek(),"must specify iterator identifier");
            }

        }else{
         throw    error(peek(),"must specify identifier");
        }




    }
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }
    private Stmt ifStatement() {

        Expr condition = expression();
        consume(COLON, "Expect ':' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(IFNOT)) {
            consume(COLON,"Expect ':' after ifnot .");
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    private Stmt proc() {
        try {
            if (match(FC)) return function("function");
           // if (match(VAR)) return varDeclaration();
         //   return statement();
          throw   error(peek(), "you must declare a function , no global scope");
        } catch (ParseError error) {
            synchronize();

            return null;
        }

    }
    private Stmt declaration() {

            return statement();

    }
    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
           Token id =     consume(IDENTIFIER, "Expect parameter name.");

                consume(COLON,"expect colon after parameter name");
                if(check(NUMERIC)){
                    id.identifier_type="numeric";
                    advance();
                }else if (check(ARRAY)){
                    id.identifier_type="array";
                    advance();
                }else if (check(NONE)){
                    id.identifier_type="none";
                    advance();
                }else{
                  throw   error(peek(),"wrong parameter type definition");
                }

                parameters.add(id);
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(RIGHT_ARROW, "Expect '->' before " + kind + " return type");
        String returnType;
        if(check(NUMERIC)){
            returnType="numeric";
            advance();
        }else if (check(ARRAY)){
            returnType="array";
            advance();
        }else if (check(NONE)){
            returnType="none";
            advance();
        }else{
            throw   error(peek(),"wrong function return type");
        }
        consume(COLON,"expect : after function return type");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        // TODO : make sure current works correctly
        currentFunction = name;
        List<Stmt> body = block();
        Stmt.Function stmt = new Stmt.Function(name, parameters, body,returnType);
        Function functionAst = new Function(stmt);
        environment.define(stmt.name.lexeme,functionAst);
        return stmt;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {


        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(COLON,"Expect ':' before specifying variable type ");
        Expr initializer = null;

       String type = identifierType("undefined identifier type");


        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer,type);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }
    private Expr assignment() {

        // Expr expr = equality();
        Expr expr = or();
        if (match(EQUAL)) {

            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            //added support for array assignment
            if (expr instanceof Expr.VariableArray) {
                Token name = ((Expr.VariableArray) expr).name;
                //TODO : add array index expression to assign expression
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");

        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    private Expr equality() {
        Expr expr = comparison();
        //BANG_EQUAL
        while (match(EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR,REMAINDER)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr unary() {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }
    private Expr call() {

        Expr expr = primary();


        while (true) {

            if (match(LEFT_PAREN)) {
              //  System.out.println(getIdent());
                expr = finishCall(expr,getIdent());
            } else {
                break;
            }
        }

        return expr;
    }
    private Expr finishCall(Expr callee,Token calleName) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments,calleName);
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NONE)) return new Expr.Literal(null);
        if (match(NUMBER)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            Token identifier = previous();
            if(match(LEFT_BRACKET)){

                Expr expr =expression();
                consume(RIGHT_BRACKET,"Expect ']' after expression");
                return new Expr.VariableArray(identifier,expr);

            }

            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }



        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
    private Token peek() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current - 1);
    }
    private Token getIdent(){
        return tokens.get(current-2);
    }
    //only to check var declaration
    private void getBack(){
        current--;
    }
    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {

            //if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case FC:
                    return;
            }

            advance();
        }
    }
    private String identifierType(String errorMessage){
        if(check(NUMERIC)){

            advance();
            return "numeric";
        }else if (check(ARRAY)){
            advance();
            return "array";

        }else if (check(NONE)){
            advance();
            return "none";

        }else{
            throw   error(peek(),errorMessage);
        }
    }

}
