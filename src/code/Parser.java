package code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.source.doctree.SystemPropertyTree;

import static code.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private boolean executableCodeStart = false;
    private int current = 0;
    private boolean BEGINflag = false;
    private boolean ENDflag = false;
    

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<code.Stmt> parse() {
        List<code.Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.addAll(declaration());
        }

        return statements;
    }


    private Expr expression() {
        return assignment();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private List<Stmt> declaration() {
        // if(!BEGINflag){
        //     Code.error(null, "Cannot declare executables outside of BEGIN CODE and END CODE");
        // }

        List<Stmt> stmts = new ArrayList<>();    

        try {
            if (match(INT_KEYWORD)) {
                stmts.addAll(varDeclaration("INT"));
                return stmts;
            }
            if (match(CHAR_KEYWORD)) {
                stmts.addAll(varDeclaration("CHAR"));
                return stmts;
            }
            if (match(BOOL_KEYWORD)) {
                stmts.addAll(varDeclaration("BOOL"));
                return stmts;
            }
            if (match(FLOAT_KEYWORD)) {
                stmts.addAll(varDeclaration("FLOAT"));
                return stmts;
            }
            if (match(STRING_KEYWORD)) {
                stmts.addAll(varDeclaration("STRING"));
                return stmts;
            }

            stmts.add(statement());
          
        } catch (ParseError error) {
            synchronize();
        }
        

        return stmts;
    }

    private Stmt singleVarDeclaration(String datatype){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        switch(datatype){
            case "INT":
                return new Stmt.Int(name, initializer);
            case "CHAR":
                return new Stmt.Char(name, initializer);
            case "BOOL":
                return new Stmt.Bool(name, initializer);
            case "FLOAT":
                return new Stmt.Float(name, initializer);
            case "STRING":
                return new Stmt.String(name, initializer);
            default:
                break;

        }

        return null;
    }

    private List<Stmt> varDeclaration(String datatype) { 
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        TokenType tokenType;

        if (executableCodeStart) {
            Code.error(current, "Cannot declare variables after executable code.");
        }
        
        //saves the datatype declared
        switch(datatype){
            case "INT":
                tokenType = TokenType.INT_KEYWORD;
                break;
            case "CHAR":
                tokenType = TokenType.CHAR_KEYWORD;
                break;
            case "BOOL":
                tokenType = TokenType.BOOL_KEYWORD;
                break;
            case "FLOAT":
                tokenType = TokenType.FLOAT_KEYWORD;
                break;
            case "STRING":
                tokenType = TokenType.STRING_KEYWORD;
                break;
            default:
                System.out.println("Datatype does not exist.");
                return null;                
        }

        //adds every variable name and initializers to their respective arraylists
        do {
            Token name = consume(IDENTIFIER, "Expect variable name.");

            Expr initializer = null;
            if (match(EQUAL)) {
                initializer = expression();
            }

            names.add(name);
            initializers.add(initializer);

        } while (match(COMMA));                 

        List<Stmt> stmts = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Token name = names.get(i);
            Expr initializer = initializers.get(i);

            switch (tokenType) {
                case INT_KEYWORD:
                    stmts.add(new Stmt.Int(name, initializer));
                    break;
                case CHAR_KEYWORD:
                    stmts.add(new Stmt.Char(name, initializer));
                    break;
                case BOOL_KEYWORD:
                    stmts.add(new Stmt.Bool(name, initializer));
                    break;
                case FLOAT_KEYWORD:
                    stmts.add(new Stmt.Float(name, initializer));
                    break;
                case STRING_KEYWORD:
                    stmts.add(new Stmt.String(name, initializer));
                    break;
                default:
                    stmts.add(new Stmt.MultiVar(null, names, initializers));
                    break;
            }
        }
        // System.out.println(stmts);
        return stmts;
        
    }    

    private code.Stmt statement() {
        if (match(IF)) {
            executableCodeStart = true;
            return ifStatement();
        }      

        if (match(DISPLAY)) {
            if(match(COLON)){
                executableCodeStart = true;
                return displayStatement();
            }
            Code.error(previous(), "Expect ':' after 'DISPLAY'.");
        }

        if (match(SCAN)) {
            if(match(COLON)){
                executableCodeStart = true;
                return scanStatement();
            }
            Code.error(previous(), "Expect ':' after 'SCAN'.");            
        }

        if (match(WHILE)) {
            executableCodeStart = true;
            return whileStatement();
        }

        if (match(FOR)) {
            executableCodeStart = true;
            return forStatement();
        }
            

        if (match(BEGIN)) {
            if(match(CODE)){

                if(BEGINflag) {
                    Code.error(Scanner.getLine(), "Cannot allow multiple BEGIN CODE and END CODE declarations");
                    return null;
                }

                BEGINflag = true;
                return new code.Stmt.Block(block());
            }
        }

        if (match(END)) {
            if(match(CODE)){
                /*  executableCodeStart marked as false to generate the correct error
                    when there is code OUTSIDE of BEGIN CODE/END CODE
                    update:...this doesnt work. will figure it out later
                */
                executableCodeStart = false;

                if(ENDflag) {
                    Code.error(Scanner.getLine(), "Cannot allow multiple BEGIN CODE and END CODE declarations");
                    return null;
                }
                return null;
            }
        }

        return expressionStatement();
    }

    private code.Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        consume(BEGIN, "Expect 'BEGIN IF' after ')'.");
        consume(IF, "Expect 'BEGIN IF' after ')'.");

        code.Stmt thenBranch = statement();

        consume(END, "Expect 'END IF' after the statement body.");
        consume(IF, "Expect 'END IF' after the statement body.");

        code.Stmt elseBranch = null;
        if (match(ELSE)) {
            if (match(IF)) {
                elseBranch = ifStatement();
            } else {
                consume(BEGIN, "Expect 'BEGIN IF' after 'ELSE'.");
                consume(IF, "Expect 'BEGIN IF' after 'ELSE'.");

                elseBranch = statement();

                consume(END, "Expect 'END IF' after the statement body.");
                consume(IF, "Expect 'END IF' after the statement body.");
            }            
        }

        return new code.Stmt.If(condition, thenBranch, elseBranch);
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        return tokens.get(current + 1).type == type;
    }

    private code.Stmt displayStatement() {
        Expr value = expression();

        // Expr value = parseExpressionWithAmpersand();

        // // Check if there are more expressions to concatenate
        // while (match(CONCAT)) {
        //     // Ensure that CONCAT is followed by a valid expression
        //     if (!check(TokenType.IDENTIFIER) && !check(TokenType.STRING) && !check(TokenType.NEXT_LINE) && !check(TokenType.CONCAT)) {
        //         Code.error(previous(), "Expected valid expression after CONCAT");
        //         return null; // Return null to signify parsing failure
        //     }

        //     // Consume the CONCAT token
        //     advance();

        //     // Parse the next expression
        //     Expr nextExpr = parseExpressionWithAmpersand();

        //     // Concatenate the expressions using the '&' symbol
        //     value = new Expr.Binary(value, previous(), nextExpr);
        // }

        // // Ensure that there are no dangling CONCAT tokens at the end
        // if (match(TokenType.IDENTIFIER) || match(TokenType.STRING) || match(TokenType.NEXT_LINE) || match(TokenType.CONCAT)) {
        //     Code.error(previous(), "Expressions must be separated by CONCAT");
        //     return null; // Return null to signify parsing failure
        // }
        return new Stmt.Display(value);
    }

    private Expr parseExpressionWithAmpersand() {
        if (match(TokenType.CONCAT)) {
            advance();
            return new Expr.Literal("&"); // Represent '&' character as a special token
        } else {
            return parseExpressionWithNewLine(); // Use existing expression parsing logic
        }
    }
    private Expr parseExpressionWithNewLine() {
        if (match(TokenType.NEXT_LINE)) {
            advance();
            return new Expr.Literal("\n"); // Represent newline with a special token
        } else {
            return expression();
        }
    }

    private code.Stmt scanStatement() {
        List<Token> variables = new ArrayList<>();

        do {
            variables.add(consume(TokenType.IDENTIFIER, "Expect variable name."));
        } while (match(TokenType.COMMA));

        return new Stmt.Scan(variables);
    }

    private code.Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        consume(BEGIN, "Expect 'BEGIN WHILE' after ')'.");
        consume(WHILE, "Expect 'WHILE' after 'BEGIN'.");

        List<Stmt> body = new ArrayList<>();
        while (!check(END) || !checkNext(WHILE)) {
            body.addAll(declaration());
        }

        consume(END, "Expect 'END WHILE' after statement body.");
        consume(WHILE, "Expect 'END WHILE' after statement body.");

        return new Stmt.While(condition, new Stmt.Block(body));
    }

    private Stmt forStatement(){

        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;

        if (match(SEMICOLON)) {
            //seeing semicolon after ( means initializer has been omitted
            initializer = null;
        } else if (match(INT_KEYWORD)) {
            initializer = singleVarDeclaration("INT");
        } else if (match(CHAR_KEYWORD)) {
            initializer = singleVarDeclaration("CHAR");
        } else if (match(BOOL_KEYWORD)) {
            initializer = singleVarDeclaration("BOOL");
        } else if (match(FLOAT_KEYWORD)) {
            initializer = singleVarDeclaration("FLOAT");
        } else if (match(STRING_KEYWORD)) {
            initializer = singleVarDeclaration("STRING");
        } else {
            initializer = expressionStatement();
        }

        consume(SEMICOLON, "Expect ';' after initializer.");

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        consume(BEGIN, "Expect 'BEGIN FOR' after ')'.");
        consume(FOR, "Expect 'FOR' after 'BEGIN'.");          

        List<Stmt> body = new ArrayList<>();
        while (!check(END) || !checkNext(FOR)) {
            body.addAll(declaration());
        }

        if (increment != null) {
            body.add(new Stmt.Expression(increment));
        }
    
        Stmt whileBody = new Stmt.Block(body);
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
    
        Stmt whileStmt = new Stmt.While(condition, whileBody);
    
        if (initializer != null) {
            List<Stmt> fullBody = new ArrayList<>();
            fullBody.add(initializer);
            fullBody.add(whileStmt);

            consume(END, "Expect 'END FOR' after statement body.");
            consume(FOR, "Expect 'FOR' after statement body.");

            return new Stmt.Block(fullBody);
        }

        consume(END, "Expect 'END FOR' after statement body.");
        consume(FOR, "Expect 'FOR' after statement body.");
    
        return whileStmt;
    }

    private code.Stmt expressionStatement() {
        Expr expr = expression();
        return new code.Stmt.Expression(expr);
    }

    private List<code.Stmt> block() {
        List<code.Stmt> statements = new ArrayList<>();

        while (!(check(END)) && !isAtEnd()) {
            statements.addAll(declaration());
        }

        consume(END, "Expect 'END CODE' after block.");
        consume(CODE, "Expect 'CODE' after 'END'.");

        return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
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

        while (match(MINUS, PLUS, CONCAT,NEXT_LINE)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR, MODULO, NEXT_LINE)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS, PLUS, NEXT_LINE)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(INT_LITERAL, CHAR_LITERAL, BOOL_LITERAL, FLOAT_LITERAL, STRING_LITERAL, ESCAPE)) {
            // Token previousObjToken = previous();

            if (check(NEXT_LINE) && !isAtEnd()) {
                //the newline token $
                advance();

                //if the newline $ is in the middle of a string, 
                //it will be treated as a binary operation for 2 strings
                if (!isAtEnd()) {
                    Token nextToken = peek();
                    return new Expr.Binary(new Expr.Literal(previous().getLiteral()),
                            new Token(NEXT_LINE, null, "\n", -1), primary());
                } else {
                    //if the newline $ is at the end of a string
                    // System.out.print(previous().getLiteral());
                    return new Expr.Literal(new Token(NEXT_LINE, null, null, -1));
                }
            } else {
                return new Expr.Literal(previous().getLiteral());
            }
        }        

        if (match(NEXT_LINE)) {
            return new Expr.Literal("\n");
        }

        if(previous().type.equals(NEXT_LINE)){
            return new Expr.Literal("");
        }

        if (match(IDENTIFIER)) {
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

    private ParseError error(Token token, String message) {
        Code.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            switch (peek().type) {
                case BEGIN:
                case INT_KEYWORD:
                case CHAR_KEYWORD:
                case BOOL_KEYWORD:
                case FLOAT_KEYWORD:
                case SCAN:
                case DISPLAY:
                case IF:
                case WHILE:
                case END:
                    return;
            }

            advance();
        }
    }
}
