package code;

import javax.print.DocFlavor.STRING;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT, MINUS, PLUS, SLASH, STAR, MODULO, CONCAT, ESCAPE,
    NEXT_LINE,

    // One or two character tokens.
    EQUAL_EQUAL, NOT_EQUAL, EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    //Logical operators
    AND, OR, NOT,

    // Keywords.

    BEGIN, END, CODE,
    VAR, NIL,
    IF, ELSE,
    TRUE, FALSE,
    WHILE, FOR,
    DISPLAY, COLON, SCAN, SEMICOLON,
    INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, FLOAT_KEYWORD,STRING_KEYWORD,

    //DATATYPES
    INT_LITERAL, CHAR_LITERAL, BOOL_LITERAL, FLOAT_LITERAL,STRING_LITERAL,

    EOF
}
