package code;

import static code.TokenType.DISPLAY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Code {

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        //indicate an error in the exit code
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static <Token> void run(String source) {
        Scanner scanner = new Scanner(source);
//        List<Token> tokens = scanner.scanTokens();
        List<code.Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
    //    for (code.Token token : tokens) {
    //        System.out.println(token);
    //    }

        Parser parser = new Parser((List<code.Token>) tokens);
        List<code.Stmt> statements = parser.parse();

        //flag to check if DISPLAY token exists
        boolean display_exists = false;
        // Stop if there was a syntax error.

        if (hadError) {
            return;
        } 

        interpreter.interpret(statements);
        
        //if there are no errors
        if(!hadRuntimeError && !hadError){
             //checks if there is DISPLAY token
             for (code.Token token : tokens) {
                if (token.type.equals(DISPLAY)){
                    display_exists=true;
                }
            }

            //prints no error if there is NO DISPLAY token and if no errors
            if(!display_exists){
                System.out.print("No error");
            }
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
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
}
