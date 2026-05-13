package com.logolsp.lexer;

// Design note: LOGO lists and other keywords are excluded intentionally, to limit the scope of LOGO for this task
public enum TokenType {

    // Motion
    FORWARD, BACK, LEFT, RIGHT,
    SETX, SETY, SETXY, SETHEADING,
    HOME, ARC,

    // Motion queries
    POS, XCOR, YCOR, HEADING,

    // Pen
    PENUP, PENDOWN, SETWIDTH, SETCOLOR,

    // Display
    CLEARSCREEN, SHOWTURTLE, HIDETURTLE, WRAP,

    // Label
    LABEL, SETLABELHEIGHT, SETLABELFONT,

    // Procedure
    TO, END,

    // Control
    REPEAT, FOR,  WHILE,
    IF, IFELSE, WAIT, STOP, OUTPUT,

    // Grouping
    LBRACKET,   // [
    RBRACKET,   // ]
    LPAREN,     // (
    RPAREN,     // )

    // Literals
    NUMBER, WORD, VARIABLE,

    // Variable assignment
    MAKE,

    // Arithmetic
    PLUS, MINUS, MULTIPLY, DIVIDE,

    // Math
    SUM, DIFFERENCE, PRODUCT, QUOTIENT, RANDOM,

    // Comparison
    EQUALS, LESS, GREATER, NOTEQUAL,

    // Input/output
    PRINT, READWORD,

    // Logic
    AND, OR, NOT,

    // Boolean
    TRUE, FALSE,

    // User-defined
    IDENTIFIER,   // procedure names etc

    // Other
    COMMENT, ERROR, EOF,
}
