package com.logolsp.features;

import com.logolsp.core.ParsedDocument;
import com.logolsp.lexer.Token;
import com.logolsp.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.logolsp.lexer.TokenType.*;

// Classifies the tokens and encodes them
// Encoding format: [ deltaLine, deltaStartChar, length, tokenTypeIndex, tokenModifiers ]
public class SemanticTokensProvider {

    private static final int TYPE_KEYWORD  = 0;
    private static final int TYPE_VARIABLE = 1;
    private static final int TYPE_FUNCTION = 2;
    private static final int TYPE_NUMBER   = 3;
    private static final int TYPE_STRING   = 4;
    private static final int TYPE_COMMENT  = 5;
    private static final int TYPE_OPERATOR = 6;

    // Modifier for declarations
    private static final int MOD_NONE = 0;
    private static final int MOD_DECLARATION = 1;

    public static final List<String> TOKEN_TYPES = List.of(
            "keyword",
            "variable",
            "function",
            "number",
            "string",
            "comment",
            "operator");

    public static final List<String> TOKEN_MODIFIERS = List.of("declaration");

    // --- Classifications ---

    private static final Set<TokenType> KEYWORDS = Set.of(
            TO, END, REPEAT, FOR,
            WHILE, IF, IFELSE,
            STOP, OUTPUT, MAKE,
            TRUE, FALSE
    );

    private static final Set<TokenType> BUILTINS = Set.of(
            FORWARD, BACK, LEFT, RIGHT,
            HOME, SETX, SETY, SETXY,
            SETHEADING, ARC, POS, XCOR,
            YCOR, HEADING, PENUP, PENDOWN,
            SETWIDTH, SETCOLOR, CLEARSCREEN,
            SHOWTURTLE, HIDETURTLE, WRAP,
            LABEL, SETLABELHEIGHT, SETLABELFONT,
            PRINT, READWORD, WAIT,
            SUM, DIFFERENCE, PRODUCT,
            QUOTIENT, RANDOM, AND,
            OR, NOT
    );

    private static final Set<TokenType> OPERATORS = Set.of(
            PLUS, MINUS, MULTIPLY, DIVIDE,
            EQUALS, LESS, GREATER, NOTEQUAL
    );

    public List<Integer> computeTokens(ParsedDocument doc) {
        List<Integer> data = new ArrayList<>();
        int previousLine = 0;
        int previousStartChar = 0;
        boolean declaration = false;

        for (Token token : doc.tokens) {
            int typeIndex = getTypeIndex(token);
            if (typeIndex == -1) // doesn't need highlighting
                continue;

            int modifiers;
            if (token.type == TokenType.IDENTIFIER && declaration) {
                modifiers = MOD_DECLARATION;
                declaration = false;
            }
            else {
                modifiers = MOD_NONE;
            }

            int deltaLine = token.line - previousLine;
            int deltaStartChar = (deltaLine == 0) ? token.startChar - previousStartChar : token.startChar;

            data.add(deltaLine);
            data.add(deltaStartChar);
            data.add(token.length);
            data.add(typeIndex);
            data.add(modifiers);

            previousLine = token.line;
            previousStartChar = token.startChar;
            // Next token is a declaration
            if (token.type == TokenType.TO)
                declaration = true;
        }
        return data;
    }

    private int getTypeIndex(Token token) {
        if (KEYWORDS.contains(token.type))
            return TYPE_KEYWORD;
        if (BUILTINS.contains(token.type))
            return TYPE_FUNCTION;
        if (OPERATORS.contains(token.type))
            return TYPE_OPERATOR;

        return switch (token.type) {
            case VARIABLE -> TYPE_VARIABLE;
            case NUMBER -> TYPE_NUMBER;
            case WORD -> TYPE_STRING;
            case COMMENT -> TYPE_COMMENT;
            case IDENTIFIER -> TYPE_FUNCTION;
            default -> -1;
        };
    }
}
