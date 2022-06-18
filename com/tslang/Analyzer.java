package com.tslang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Analyzer implements Expr.Visitor<Object> ,
        Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment = globals;
    /* void interpret(Expr expression) {
         try {
             Object value = evaluate(expression);
             System.out.println(stringify(value));
         } catch (RuntimeError error) {
             Lox.runtimeError(error);
         }
     }*/
    Analyzer() {
        Stmt.Function printFunction  = new Stmt.Function(new Token(TokenType.FC,"print","print",0),
                Arrays.asList(new Token(TokenType.NUMERIC,"numeric",null,0,"numeric")),null,"none");
        Stmt.Function Arrayfunction  = new Stmt.Function(new Token(TokenType.FC,"Array","Array",0),
                Arrays.asList(new Token(TokenType.NUMERIC,"numeric",null,0,"numeric")),null, "array");
        Stmt.Function InputFunction  = new Stmt.Function(new Token(TokenType.FC,"input","input",0),
                null,null, "numeric");

        Function print = new Function(printFunction);
        Function array = new Function(Arrayfunction);
        Function input = new Function(InputFunction);
        globals.define("print", print);
        globals.define("Array", array);
        globals.define("input",input);
    }
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);

            }
        } catch (RuntimeError error) {
            Main.analyzeError(error);
        }
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        try{
            String left_ = (String)left;
            String right_ = (String)right;
            if(left_ ==null || right_ == null){
                throw new RuntimeError(expr.operator,"type mismatch");
            }
            if(left_.equals(right_)){
                return left;

            }else{
                throw new RuntimeError(expr.operator,"type mismatch");
            }


        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    " [line " + e.token.line + "]");
            return null;
        }




    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {

            arguments.add(evaluate(argument));
        }

        try{
            if (!(callee instanceof Function)) {
                throw new RuntimeError(expr.paren,
                        "Can only call functions");
            }



            Function function = (Function) callee;

            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren, "Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size() + ".");
            }
           for(int i=0 ; i<arguments.size();i++){
               String argument = (String)arguments.get(i);






            if(argument!=null){
                if(!argument.equals(function.declaration.params.get(i).identifier_type)){
                    throw new RuntimeError(expr.paren,"" +
                            "wrong type for argument "+(i+1)+" of "+function.declaration.name.lexeme);
                }
            }


           }

        return function.declaration.returnType;

        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    " [line " + e.token.line + "]");
            return null;
        }

      //  return function.call(this, arguments);

    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
     //   System.out.println(stmt.keyword);
        //TODO : if a function and variable have same name here will cause an classCastException
        Function fn  = ( Function) environment.get(stmt.keyword);
        if (value != null) {
            if (value.equals(fn.declaration.returnType)){


            }else{
                System.err.println("return type mismatch at function " +
                        "["+stmt.keyword.lexeme +"] expected to return "+fn.declaration.returnType+" instead of "+value);
            }
        }else{
            if(!fn.declaration.returnType.equals("none")){
                System.err.println("return type mismatch at line ["+stmt.keyword.lexeme +"] expected to " +
                        "return "+fn.declaration.returnType+" instead of "+value);
            }
        }
        //   System.out.println(value+"value");

      //  throw new Return(value);
        return null;
    }
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt);
        environment.define(stmt.name.lexeme, function);
        for(int i=0;i<stmt.params.size();i++){
           // System.out.println(stmt.params.get(i));
            environment.define(stmt.params.get(i).lexeme,stmt.params.get(i).identifier_type);

        }
        executeBlock(stmt.body,this.environment);
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBracketsExpr(Expr.Brackets expr) {
        //TODO : implementation
        System.out.println("i am here :|");
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        if(expr.value!=null){
            return "numeric";
        }
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        return right;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
    void executeForStatement(Stmt statement,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

                execute(statement);

        } finally {
            this.environment = previous;
        }
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;

    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        evaluate(stmt.expression);
      //  Object value = evaluate(stmt.expression);
      //  System.out.println(stringify(value));
        return null;
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null ;
        if (stmt.initializer != null) {
            //value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, stmt.type);
        return null;
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);


        try {
            String type;
            type =  (String)environment.get(expr.name);


            if(type!=null && value!=null){
                if(!type.equals(value)){
                    throw new RuntimeError(expr.name,"assignment type mismatch");
                }

            }

        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    "[line " + e.token.line + "]");
        }
        return null;

    }
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        try {

            return environment.get(expr.name);
        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    "[line " + e.token.line + "]");
        }
        return null;

    }

    @Override
    public Object visitVariableArrayExpr(Expr.VariableArray expr) {
        System.out.println("arr called");
        return "array";
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
            evaluate(stmt.condition);
            execute(stmt.thenBranch);
     if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        Object right=evaluate(expr.right);
        if(left.equals(right)){
            return left;
        }else{
            throw new RuntimeError(expr.operator,"type mismatch");
        }

    }
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {

        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        Environment environmentFor = new Environment(this.environment);

        environmentFor.define(stmt.forIdentfier.lexeme,"numeric");
        environmentFor.define(stmt.Iterator.lexeme,"numeric");

        executeForStatement(stmt.body,environmentFor);
        return null;
    }
}
