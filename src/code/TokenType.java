package code;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT, MINUS, PLUS, SLASH, STAR, MODULO, CONCAT, ESCAPE,

    // One or two character tokens.
    //    BANG, BANG_EQUAL, --> unused
    EQUAL_EQUAL, NOT_EQUAL, EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    //Logical operators
    AND, OR, NOT,

    // Keywords.
    // CLASS, FUN, FOR, PRINT, NIL, RETURN, SUPER, THIS, VAR //THESE ARE EXCLUDED

    BEGIN, END, CODE,
    VAR, NIL,
    IF, ELSE,
    TRUE, FALSE,
    WHILE,
    DISPLAY, COLON, SCAN,
    INT_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD, FLOAT_KEYWORD,

    //DATATYPES
    INT_LITERAL, CHAR_LITERAL, BOOL_LITERAL, FLOAT_LITERAL,

    EOF
}
