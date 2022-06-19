package com.tslang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    static boolean hadError = false;
    private static  Analyzer analyzer;
    public static void main(String[] args) throws IOException {
        runFile(args[0]);
    }
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));


    }
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
       /* for (Token token : tokens) {
            System.out.println(token);
        }*/
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // Stop if there was a syntax error.
        if (hadError) return;
        System.out.println("parsing finished");
        Environment functions = parser.getEnvironment();
        analyzer = new Analyzer(functions);

        analyzer.setEnvironment(functions);
        analyzer.interpret(statements);
        if(hadError) return ;

        IrGenerator ir = new IrGenerator();
        ir.generate(statements);


    }
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
    static void analyzeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");

    }
}
