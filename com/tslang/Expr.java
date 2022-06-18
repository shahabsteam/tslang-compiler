package com.tslang;


import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitBracketsExpr(Brackets expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
        R visitVariableArrayExpr(VariableArray expr);
    }
    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }
    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
        Call(Expr callee, Token paren, List<Expr> arguments,Token name) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
            this.name=name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        final Expr callee;
        final Token paren;
         Token name;
        final List<Expr> arguments;
    }
    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }


        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }
    // same as grouping
    static class Brackets extends Expr {
        Brackets(Expr expression) {
            this.expression = expression;
        }


        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBracketsExpr(this);
        }

        final Expr expression;
    }
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }
    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }
    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }
    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;
    }
    static class VariableArray extends Expr {
        VariableArray(Token name,Expr arrayIndex) {
            this.name = name;
            this.arrayIndex=arrayIndex;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableArrayExpr(this);
        }

        final Token name;
        final Expr arrayIndex;
    }

    abstract <R> R accept(Visitor<R> visitor);
}

