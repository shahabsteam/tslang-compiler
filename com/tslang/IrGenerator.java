package com.tslang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IrGenerator implements Expr.Visitor<IR> ,
        Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment = globals;
    private Label labelGenerator = new Label();
    final TempVar tempVar = new TempVar();
    IrGenerator() {
        Stmt.Function printFunction  = new Stmt.Function(new Token(TokenType.FC,"print","print",0),
                null,null,"none");
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
    void generate(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);

            }
        } catch (RuntimeError error) {
            Main.analyzeError(error);
        }
    }
    @Override
    public IR visitBinaryExpr(Expr.Binary expr) {
        IR left = evaluate(expr.left);
        IR right = evaluate(expr.right);
        String temp;
        try{
            switch (expr.operator.type){
                case PLUS:
                   //  temp = tempVar.getTempVar();
                    System.out.println("add  "+left.varName+ ", "+left.varName+", "+right.varName);
                    return left;
                case MINUS:
                    System.out.println("sub  "+left.varName+ ", "+left.varName+", "+right.varName);
                    return left;
                case STAR:
                     temp = tempVar.getTempVar();
                     System.out.println("mul  "+temp+ ", "+left.varName+", "+right.varName);
                     return new IR(null,temp,"mul");
                case SLASH:
                    temp = tempVar.getTempVar();
                    System.out.println("div  "+temp+ ", "+left.varName+", "+right.varName);
                    return new IR(null,temp,"div");
                case REMAINDER:
                    temp = tempVar.getTempVar();
                    System.out.println("mod  "+temp+ ", "+left.varName+", "+right.varName);
                    return new IR(null,temp,"mod");

                case LESS:
                    temp = tempVar.getTempVar();
                    if(left.print!=null){
                        System.out.println(left.print);
                    }
                    if(right.print!=null){
                        System.out.println(right.print);
                    }
                    System.out.println("cmp<  "+temp+", "+left.varName+","+right.varName);
                    return new IR(null,temp,"cmp<");
                case LESS_EQUAL:
                    temp = tempVar.getTempVar();
                    if(left.print!=null){
                        System.out.println(left.print);
                    }
                    if(right.print!=null){
                        System.out.println(right.print);
                    }
                    System.out.println("cmp<=  "+temp+", "+left.varName+","+right.varName);
                    return new IR(null,temp,"cmp<=");
                case GREATER:
                    temp = tempVar.getTempVar();
                    if(left.print!=null){
                        System.out.println(left.print);
                    }
                    if(right.print!=null){
                        System.out.println(right.print);
                    }
                    System.out.println("cmp>  "+temp+", "+left.varName+","+right.varName);
                    return new IR(null,temp,"cmp>");
                case GREATER_EQUAL:
                    temp = tempVar.getTempVar();
                    if(left.print!=null){
                        System.out.println(left.print);
                    }
                    if(right.print!=null){
                        System.out.println(right.print);
                    }
                    System.out.println("cmp>=  "+temp+", "+left.varName+","+right.varName);
                    return new IR(null,temp,"cmp>=");
                case EQUAL_EQUAL:
                    temp = tempVar.getTempVar();
                    if(left.print!=null){
                        System.out.println(left.print);
                    }
                    if(right.print!=null){
                        System.out.println(right.print);
                    }
                    System.out.println("cmp=  "+temp+", "+left.varName+","+right.varName);
                    return new IR(null,temp,"cmp=");



                default:
                    return null;


            }


        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    " [line " + e.token.line + "]");
            return null;
        }




    }

    @Override
    public IR visitCallExpr(Expr.Call expr) {

      //  Object callee = evaluate(expr.callee);
        List<IR> arguments = new ArrayList<>();
        boolean isprimaryFunction =true;
        if( !expr.name.lexeme.equals("input")   && !expr.name.lexeme.equals("print")){

            System.out.print("call  "+expr.name.lexeme);
            isprimaryFunction=false;


        }

        for (Expr argument : expr.arguments) {

            IR vars = evaluate(argument);
            arguments.add(vars);
            if(!isprimaryFunction)
                System.out.print(","+vars.varName);

        }
        if(!isprimaryFunction){

            System.out.println();

        }

        if(expr.name.lexeme.equals("input")){

            return new IR("call  iget",null,"input");

        }
        if(expr.name.lexeme.equals("print")){
            System.out.println("call  iput "+arguments.get(0).varName);
            return new IR(null,null,"output");
        }


        try{

            return new IR(null,arguments.get(0).varName);


        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    " [line " + e.token.line + "]");
            return null;
        }

        //  return function.call(this, arguments);

    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        IR value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        if(value!=null){

            if( value.type.equals("literal")){
                System.out.println("mov  r0, "+value.literal);
            }else{
                if(!value.varName.equals("r0"))
                System.out.println("mov  r0, "+value.varName);
            }
        }
        System.out.println("ret");
        return null;
    }
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt);
        tempVar.setCounter(1);
        environment.define(stmt.name.lexeme, function);
        for(int i=0;i<stmt.params.size();i++){
            // System.out.println(stmt.params.get(i));
            environment.define(stmt.params.get(i).lexeme,stmt.params.get(i).identifier_type);
            tempVar.assign(stmt.params.get(i).lexeme);


        }
        System.out.println("proc  "+stmt.name.lexeme);
        executeBlock(stmt.body,this.environment);
        return null;
    }

    @Override
    public IR visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public IR visitBracketsExpr(Expr.Brackets expr) {
        //TODO : implementation
        System.out.println("i am here :|");
        return null;
    }

    @Override
    public IR visitLiteralExpr(Expr.Literal expr) {
        String temp = tempVar.getTempVar();
        IR literal = new IR("mov  "+temp+", "+expr.value,temp,"literal");
        literal.literal =(int) expr.value;
      //  System.out.println("mov  "+temp+", "+expr.value);
        return literal;
    }

    @Override
    public IR visitUnaryExpr(Expr.Unary expr) {
        IR right = evaluate(expr.right);
        String temp;
        switch (expr.operator.type){
            case MINUS:
                temp = tempVar.getTempVar();
                String minus1 = tempVar.getTempVar();
                System.out.println("mov  "+minus1+", "+"-1");
                System.out.println("mul  "+temp+ ", "+minus1+", "+right.varName);
                return new IR(null,temp,"unary");

        }
        return new IR(null,null,"unary");
    }

    private IR evaluate(Expr expr) {
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

            System.out.println("call iput");


        //  Object value = evaluate(stmt.expression);
        //  System.out.println(stringify(value));
        return null;
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null ;
        tempVar.assign(stmt.name.lexeme);
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, stmt.type);
        return null;
    }
    @Override
    public IR visitAssignExpr(Expr.Assign expr) {
        IR value = evaluate(expr.value);
        if(value.print!=null){
            if(value.print.equals("call  iget")){
                String varname =   tempVar.get(expr.name.lexeme);
                System.out.println("call  iget, "+varname);
                return null;
            }
            if(value.print.equals("call  iput")){

                String varname = tempVar.get(expr.name.lexeme);
                System.out.println("call  iput,"+varname);
                return null;
            }

        }



        System.out.println("mov  "+tempVar.get(expr.name.lexeme)+", "+value.varName);



        try {
        return null;

            }

        catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    "[line " + e.token.line + "]");
        }
        return null;

    }
    @Override
    public IR visitVariableExpr(Expr.Variable expr) {
        try {
            IR var = new IR(null,tempVar.get(expr.name.lexeme),"variable");
            return var;
        }catch (RuntimeError e){
            System.err.println(e.getMessage() +
                    "[line " + e.token.line + "]");
        }
        return null;

    }

    @Override
    public IR visitVariableArrayExpr(Expr.VariableArray expr) {
        System.out.println("arr called");
        return new IR(null,null,"vararr");
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
       IR cond =  evaluate(stmt.condition);
       String label =labelGenerator.getLabel();
        System.out.println("jz  "+cond.varName+", "+label);
        execute(stmt.thenBranch);
        System.out.println(label+":");
        if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
    @Override
    public IR visitLogicalExpr(Expr.Logical expr) {
        IR left = evaluate(expr.left);
        IR right=evaluate(expr.right);
        if(left.equals(right)){
            return left;
        }else{
            System.out.println("logic");
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
        tempVar.assign(stmt.Iterator.lexeme);
        tempVar.assign(stmt.forIdentfier.lexeme);

        executeForStatement(stmt.body,environmentFor);
        return null;
    }
}
