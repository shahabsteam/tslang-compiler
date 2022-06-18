package com.tslang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tslang.TokenType.*;

class Scanner {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fc",    FC);
        keywords.put("if",     IF);
        keywords.put("or",     OR);
        keywords.put("return", RETURN);
        keywords.put("true",   TRUE);
        keywords.put("while",  WHILE);
        keywords.put("array",ARRAY);
        keywords.put("none",NONE);
        keywords.put("not",NOT);
   //     keywords.put("print",  PRINT);
        keywords.put("numeric",NUMERIC);
        keywords.put("ifnot",IFNOT);
    }

    Scanner(String source) {
        this.source = source;
    }
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
    private boolean isAtEnd() {
        return current >= source.length();
    }
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '+': addToken(PLUS); break;
            case ':' : addToken(COLON);break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH);break;
            case '%': addToken(REMAINDER);break;
            case '?': addToken(QUESTION);break;
            case '[': addToken(LEFT_BRACKET);break;
            case']':addToken(RIGHT_BRACKET);break;
            case '-':  addToken(match('>') ? RIGHT_ARROW : MINUS);
                break;

            case '#':
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();//go further in the comment untill its end

                break;

            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                if(match('=')){
                    addToken(LESS_EQUAL);
                    break;
                }
                if(match('-')){
                    addToken(LEFT_ARROW);
                    break;
                }
                addToken(LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }
                else {

                    System.out.println("Unexpected character at line "+ this.line);
                }
        }
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Integer.parseInt(source.substring(start, current)));
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

}