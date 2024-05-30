package code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static code.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();   
    private int start = 0;
    private int current = 0;
    // private int line = 1;
    private static int line;

    private static final Map<String, TokenType> keywords;
    private static final List<Character> charas = new ArrayList<>();

    //characters
    static {
        charas.add(':');
        charas.add('(');
        charas.add(')');
        charas.add(',');
        charas.add('.');
        charas.add('-');
        charas.add('+');
        charas.add('*');
        charas.add('/');
        charas.add('&');
        charas.add('[');
        charas.add(']');
        charas.add('%');
        charas.add('=');
        charas.add('<');
        charas.add('>');
        charas.add('#');
        charas.add('"');
        charas.add('\'');
        charas.add('$');
        charas.add(';');
    }

    //reserved words
    static {
        keywords = new HashMap<>();
        keywords.put("BEGIN", BEGIN);
        keywords.put("END", END);
        keywords.put("CODE", CODE);
        keywords.put("IF", IF);
        keywords.put("WHILE", WHILE);
        keywords.put("FOR", FOR);
        keywords.put("DISPLAY", DISPLAY);
        keywords.put("SCAN", SCAN);
        keywords.put("ELSE", ELSE);
        keywords.put("NIL", NIL);
        keywords.put("\"TRUE\"", TRUE);
        keywords.put("\"FALSE\"", FALSE);
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("NOT", NOT);
        keywords.put("VAR", VAR);
        keywords.put("INT",  INT_KEYWORD);
        keywords.put("CHAR",  CHAR_KEYWORD);
        keywords.put("BOOL",  BOOL_KEYWORD);
        keywords.put("FLOAT",  FLOAT_KEYWORD);
        keywords.put("STRING",  STRING_KEYWORD);
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

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ':': addToken(COLON); break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case '&': addToken(CONCAT); break;
            case '*': addToken(STAR); break;
            case '%': addToken(MODULO); break;
            case ';': addToken(SEMICOLON); break;
            case '[':
                escapechar();
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL: EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL :  (match('>') ? NOT_EQUAL : LESS));
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                addToken(SLASH);
                break;
            case '#':
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) advance();
                break;

            case ' ':
            case '\n':
                line++;
                break;
            case '\r':
            case '\t':
                break;

            case '$': //next line
                addToken(NEXT_LINE);
                line++;
                break;

            case '"':
                string();
                break;

            case '\'':
                char_type();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Code.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void escapechar(){
        while (peek() != ']' && !isAtEnd()) {
            if (peek() == '\n') return;
            advance();
        }

        if(current - start != 2){
            advance();
            
            if(peek() !=']') {
                Code.error(line, "Invalid escape character");
                return;
            }
            
        }

        // The closing ].
        advance();

        // Trim the surrounding quotes.
        char value = source.charAt(start + 1);

        //if the character inside the [] is in the List of characters 
        if(charas.contains(value)){
            addToken(ESCAPE, value);
            return;
        }

        Code.error(line, "Invalid Escape Character.");
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
            addToken(FLOAT_LITERAL, Double.parseDouble(source.substring(start, current)));
        } else {
            addToken(INT_LITERAL, Integer.parseInt(source.substring(start, current)));
        }
    }

    private void char_type() {
        // Consume the starting single quote
        advance();

        // Regular character handling
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Code.error(line, "Unterminated character.");
            return;
        }

        // Consume the closing single quote
        advance();

        // Check if the character literal is of valid length
        if (current - start > 3) {
            Code.error(line, "Invalid character input: Character literal must contain exactly one character.");
            return;
        }

        // Extract the character value
        String value = source.substring(start + 1, current - 1);

        // Check if the character literal contains exactly one character
        if (value.length() != 1) {
            Code.error(line, "Invalid character input: Character literal must contain exactly one character.");
            return;
        }

        // Add the character literal token
        addToken(CHAR_LITERAL, value.charAt(0));
    }

    private void string() {
        // Regular string handling
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            if (peek() == '$') {
                
                String val = source.substring(start + 1, current);
                addToken(STRING_LITERAL, val);
                addToken(NEXT_LINE);
                start = current;
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Code.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current-1);

        if(value.equals("TRUE")){
            addToken(TRUE, Boolean.parseBoolean(value));
        } else if (value.equals("FALSE")){
            addToken(FALSE, Boolean.parseBoolean(value));
        } else {
            addToken(STRING_LITERAL, value);
        }
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

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
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

    public static Map<String, TokenType> getKeywords() {
        return keywords;
    }

    public static List<Character> getCharacters() {
        return charas;
    }

    public static int getLine() {
        return line;
    }

}
