package com.logolsp.core;

import com.logolsp.lexer.TokenType;

import java.util.Map;

// Built-in commands
public class LogoBuiltIns {
    public static final Map<TokenType, Integer> COMMAND_ARG_COUNTS = Map.ofEntries(
            // Motion
            Map.entry(TokenType.FORWARD,      1),
            Map.entry(TokenType.BACK,         1),
            Map.entry(TokenType.LEFT,         1),
            Map.entry(TokenType.RIGHT,        1),
            Map.entry(TokenType.HOME,         0),
            Map.entry(TokenType.SETX,         1),
            Map.entry(TokenType.SETY,         1),
            Map.entry(TokenType.SETXY,        2),
            Map.entry(TokenType.SETHEADING,   1),
            Map.entry(TokenType.ARC,          2),

            // Motion queries
            Map.entry(TokenType.POS,          0),
            Map.entry(TokenType.XCOR,         0),
            Map.entry(TokenType.YCOR,         0),
            Map.entry(TokenType.HEADING,      0),

            // Pen
            Map.entry(TokenType.PENUP,        0),
            Map.entry(TokenType.PENDOWN,      0),
            Map.entry(TokenType.SETWIDTH,     1),
            Map.entry(TokenType.SETCOLOR,     1),

            // Display
            Map.entry(TokenType.CLEARSCREEN,  0),
            Map.entry(TokenType.SHOWTURTLE,   0),
            Map.entry(TokenType.HIDETURTLE,   0),
            Map.entry(TokenType.WRAP,         0),

            // Label
            Map.entry(TokenType.LABEL,        1),
            Map.entry(TokenType.SETLABELHEIGHT, 1),
            Map.entry(TokenType.SETLABELFONT, 1),

            // Control
            Map.entry(TokenType.WAIT,         1),
            Map.entry(TokenType.STOP,         0),
            Map.entry(TokenType.OUTPUT,       1),

            // Math functions
            Map.entry(TokenType.SUM,          2),
            Map.entry(TokenType.DIFFERENCE,   2),
            Map.entry(TokenType.PRODUCT,      2),
            Map.entry(TokenType.QUOTIENT,     2),
            Map.entry(TokenType.RANDOM,       1),

            // Input/output
            Map.entry(TokenType.PRINT,        1),
            Map.entry(TokenType.READWORD,     0),

            // Logic
            Map.entry(TokenType.NOT,          1),
            Map.entry(TokenType.AND,          2),
            Map.entry(TokenType.OR,           2)
    );

    public static boolean isBuiltIn(TokenType type) {
        return COMMAND_ARG_COUNTS.containsKey(type);
    }
}
