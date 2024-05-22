package code;

import java.util.ArrayList;
import java.util.List;

import static code.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private Environment environment = new Environment();
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<code.Stmt> parse() {
        List<code.Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
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

    private code.Stmt declaration() {
        try {
//            if (match(VAR)) return varDeclaration();
//            -->original
            if (match(INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, FLOAT_KEYWORD)) return varDeclaration();
//            if (match(INT_KEYWORD)) return intDeclaration();
//            if (match(FLOAT_KEYWORD)) return floatDeclaration();
//            if (match(CHAR_KEYWORD)) return charDeclaration();
//            if (match(BOOL_KEYWORD)) return boolDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private code.Stmt varDeclaration() { //ORIGINAL
        Token datatype = previous(); // Capture the datatype token

        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();

            // Check if the assigned value matches the datatype
//            if (!checkDatatype(initializer, datatype.type)){
//                throw error(previous(), "Datatype mismatch in variable declaration.");
//            }
        }

//        return new Stmt.Var(name, initializer); -->orig
//        if(validType){
//            TokenType dataType = getExpressionDatatype(initializer);
//            environment.defineDataType(name.lexeme, datatype.lexeme); //executes as intended
//        }

        return new code.Stmt.Var(name, initializer);
    }

    //-----------------------additional code[start]-----------------
    private boolean checkDatatype(Expr initializer, TokenType expectedType) {
        // Check if initializer is null (no value assigned)
        if (initializer == null) {
            // Return false if no value is assigned
            return false;
        }

        // Determine the datatype of the initializer expression
        TokenType actualType = getExpressionDatatype(initializer);

        // Compare the actual datatype with the expected datatype
        return actualType == expectedType;
    }

    private TokenType getExpressionDatatype(Expr expr) {
        if (expr instanceof Expr.Literal) {
            // If it's a literal expression, return the datatype of the literal
            Object value = ((Expr.Literal) expr).value;
            if (value instanceof Integer) {
                return TokenType.INT_KEYWORD;
            } else if (value instanceof Character) {
                return TokenType.CHAR_KEYWORD;
            } else if (value instanceof Boolean) {
                return TokenType.BOOL_KEYWORD;
            } else if (value instanceof Float) {
                return TokenType.FLOAT_KEYWORD;
            }
        } else if (expr instanceof Expr.Grouping) {
            // If it's a grouping expression, recursively check the expression inside
            return getExpressionDatatype(((Expr.Grouping) expr).expression);
        }
//        else if (expr instanceof Expr.Variable) {
//            // If it's a variable expression, look up the datatype of the variable in the environment
//            Token variableName = ((Expr.Variable) expr).name;
//            // Assume you have a method to get the datatype of a variable from the environment
//            TokenType variableType = getVariableDatatype(variableName);
//            return variableType;
//        }
        // Return null if the datatype cannot be determined
        return null;
    }

//    private TokenType getVariableDatatype(Token variableName) {
//        /// Implement logic to look up the datatype of the variable from the environment
//        // Return the datatype (e.g., INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, FLOAT_KEYWORD)
//        // Return null if the variable is not found or if its datatype is unknown
//        return null;
//    }

//    private TokenType getVariableDatatype(Token variableName) {
//        Object value = environment.get(variableName); // Assuming you have a method to get variable value from the environment
//
//        if (value == null) {
//            // Variable not found in the environment
//            // You might want to throw an error or handle this case differently based on your requirements
//            return TokenType.UNKNOWN; // or return null, TokenType.UNKNOWN, or throw an error
//        }
//
//        // Determine the datatype of the variable based on its value
//        if (value instanceof Integer) {
//            return TokenType.INT_KEYWORD;
//        } else if (value instanceof String) {
//            return TokenType.CHAR_KEYWORD;
//        } else if (value instanceof Boolean) {
//            return TokenType.BOOL_KEYWORD;
//        } else if (value instanceof Double) {
//            return TokenType.FLOAT_KEYWORD;
//        }
//
//        // If the datatype cannot be determined from the value, you might return TokenType.UNKNOWN or handle it differently
//        return TokenType.UNKNOWN; // or return null, throw an error, etc.
//    }
    //-----------------------additional code[end]-------------------

//    private Stmt intDeclaration() {
//        Token name = consume(IDENTIFIER, "Expect variable name.");
//
//        Expr initializer = null;
//        if (match(EQUAL)) {
//            initializer = expression();
//        }
//
//        return new Stmt.VarInt(name, initializer,INT_KEYWORD);
//    }
//
//    private Stmt charDeclaration() {
//        Token name = consume(IDENTIFIER, "Expect variable name.");
//
//        Expr initializer = null;
//        if (match(EQUAL)) {
//            initializer = expression();
//            // // Check if the initializer is a character literal
//            // if (!(initializer instanceof Expr.Literal) || !(initializer.value instanceof Character)) {
//            //     throw error(peek(), "Initializer for char variable must be a character literal.");
//            // }
//        }
//
//        return new Stmt.VarChar(name, initializer,CHAR_KEYWORD);
//    }
//
//    private Stmt floatDeclaration() {
//        Token name = consume(IDENTIFIER, "Expect variable name.");
//
//        Expr initializer = null;
//        if (match(EQUAL)) {
//            initializer = expression();
//
//            // // Check if the initializer is a number literal (integer or float)
//            // if (!(initializer instanceof Expr.Literal) ||
//            //     !(initializer.value instanceof Number)) {
//            //     throw error(peek(), "Initializer for float variable must be a number literal.");
//            // }
//        }
//
//        return new Stmt.VarFloat(name, initializer,FLOAT_KEYWORD);
//    }
//
//    private Stmt boolDeclaration() {
//        Token name = consume(IDENTIFIER, "Expect variable name.");
//
//        Expr initializer = null;
//        if (match(EQUAL)) {
//            initializer = expression();
//
//            // // Check if the initializer is a boolean literal
//            // if (!(initializer instanceof Expr.Literal) ||
//            //     !(initializer.value instanceof Boolean)) {
//            //     throw error(peek(), "Initializer for bool variable must be a boolean literal.");
//            // }
//        }
//
//        return new Stmt.VarBool(name, initializer,BOOL_KEYWORD);
//    }

    private code.Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(DISPLAY)) {
            if(match(COLON)){
                return printStatement();
            }
        }
        if (match(WHILE)) return whileStatement();
//        if (match(BEGIN)) return new Stmt.Block(block()); -->original
        if (match(BEGIN)) {
            if(match(CODE)){
                return new code.Stmt.Block(block());
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
            consume(BEGIN, "Expect 'BEGIN IF' after 'ELSE'.");
            consume(IF, "Expect 'BEGIN IF' after 'ELSE'.");

            elseBranch = statement();

            consume(END, "Expect 'END IF' after the statement body.");
            consume(IF, "Expect 'END IF' after the statement body.");
        }

        return new code.Stmt.If(condition, thenBranch, elseBranch);
    }

    //might be useful for later, but highly likely to be removed
    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        return tokens.get(current + 1).type == type;
    }

    private code.Stmt printStatement() {
        Expr value = expression();
        return new code.Stmt.Print(value);
    }

    private code.Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        consume(BEGIN, "Expect 'BEGIN WHILE' after ')'.");
        consume(WHILE, "Expect 'WHILE' after 'BEGIN'.");

        code.Stmt body = statement();

        consume(END, "Expect 'END WHILE' after statement body.");
        consume(WHILE, "Expect 'END WHILE' after statement body.");

        return new code.Stmt.While(condition, body);
    }

    private code.Stmt expressionStatement() {
        Expr expr = expression();
        return new code.Stmt.Expression(expr);
    }

    private List<code.Stmt> block() {
        List<code.Stmt> statements = new ArrayList<>();

//        while (!check(RIGHT_BRACE) && !isAtEnd()) {
//            statements.add(declaration());
//        }
//
//        consume(RIGHT_BRACE, "Expect '}' after block.");
//        -->GUIDE

        while (!(check(END)) && !isAtEnd()) {
            statements.add(declaration());
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
//                Token datatype = ((Expr.Variable) expr).dataType;
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

        if (match(INT_LITERAL, CHAR_LITERAL, BOOL_LITERAL, FLOAT_LITERAL, STRING, ESCAPE)) {
            Token previousObjToken = previous();

            if (check(NEXT_LINE) && !isAtEnd()) {
                //the newline token $
                advance();

                //if the newline $ is in the middle of a string, 
                //it will be treated as a binary operation for 2 strings
                if (!isAtEnd()) {
                    Token nextToken = peek();
                    return new Expr.Binary(new Expr.Literal(previousObjToken.getLiteral()),
                            new Token(NEXT_LINE, null, "\n", -1), primary());
                } else {
                    //if the newline $ is at the end of a string
                    System.out.print(previousObjToken.getLiteral());
                    return new Expr.Literal(new Token(NEXT_LINE, null, null, -1));
                }
            } else {
                return new Expr.Literal(previousObjToken.getLiteral());
            }
            // return new Expr.Literal(previous().literal);
        }

        

//        if (match(INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, FLOAT_KEYWORD)) {
//            Token nameToken = previous(); // Capture the datatype token
//            Token datatypeToken;
//
//            if (nameToken.type.equals(INT_KEYWORD)){
//                datatypeToken = consume(INT_KEYWORD, "Expect datatype after variable name."); // Consume the datatype token
//            }
//
//            return new Expr.Variable(datatypeToken, nameToken); // Create a Variable instance with both tokens
//        }

//        if (match(INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, CHAR_KEYWORD)) {
////            System.out.println("datatype keyword found");
////            Token dataType = previous();
//            Token dataType = tokens.get(current-2);
//            if (match(IDENTIFIER)) {
//                Token varName = previous();
//                return new Expr.Variable(dataType, varName);
//            }
//        }

        if (match(IDENTIFIER)) {
//            current+=2;
//            Token varName = previous();
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
//            if (previous().type == SEMICOLON) return;
//            if (
//                previous().type == CODE ||
//                previous().type == IF ||
//                previous().type == WHILE
//            ) return; --> to be excluded

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
