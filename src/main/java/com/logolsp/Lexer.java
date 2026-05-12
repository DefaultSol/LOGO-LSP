package com.logolsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.logolsp.TokenType.*;

// Reference used: Crafting Interpreters by Robert Nystrom
// https://craftinginterpreters.com/scanning.html
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 0;

    private static final Map<String, TokenType> KEYWORDS = Map.<String, TokenType>ofEntries(
            // Motion
            Map.entry("FORWARD",       FORWARD),
            Map.entry("FD",            FORWARD),
            Map.entry("BACK",          BACK),
            Map.entry("BK",            BACK),
            Map.entry("LEFT",          LEFT),
            Map.entry("LT",            LEFT),
            Map.entry("RIGHT",         RIGHT),
            Map.entry("RT",            RIGHT),
            Map.entry("HOME",          HOME),
            Map.entry("SETX",          SETX),
            Map.entry("SETY",          SETY),
            Map.entry("SETXY",         SETXY),
            Map.entry("SETHEADING",    SETHEADING),
            Map.entry("SETH",          SETHEADING),
            Map.entry("ARC",           ARC),

            // Motion queries
            Map.entry("POS",           POS),
            Map.entry("XCOR",          XCOR),
            Map.entry("YCOR",          YCOR),
            Map.entry("HEADING",       HEADING),

            // Pen
            Map.entry("PENUP",         PENUP),
            Map.entry("PU",            PENUP),
            Map.entry("PENDOWN",       PENDOWN),
            Map.entry("PD",            PENDOWN),
            Map.entry("SETWIDTH",      SETWIDTH),
            Map.entry("SETCOLOR",      SETCOLOR),
            Map.entry("SC",            SETCOLOR),

            // Display
            Map.entry("CLEARSCREEN",   CLEARSCREEN),
            Map.entry("CS",            CLEARSCREEN),
            Map.entry("SHOWTURTLE",    SHOWTURTLE),
            Map.entry("ST",            SHOWTURTLE),
            Map.entry("HIDETURTLE",    HIDETURTLE),
            Map.entry("HT",            HIDETURTLE),
            Map.entry("WRAP",          WRAP),

            // Label
            Map.entry("LABEL",         LABEL),
            Map.entry("SETLABELHEIGHT", SETLABELHEIGHT),
            Map.entry("SETLABELFONT",  SETLABELFONT),

            // Procedure
            Map.entry("TO",            TO),
            Map.entry("END",           END),

            // Control
            Map.entry("REPEAT",        REPEAT),
            Map.entry("FOR",           FOR),
            Map.entry("WAIT",          WAIT),
            Map.entry("WHILE",         WHILE),
            Map.entry("IF",            IF),
            Map.entry("IFELSE",        IFELSE),
            Map.entry("STOP",          STOP),
            Map.entry("OUTPUT",        OUTPUT),
            Map.entry("OP",            OUTPUT),

            // Variable
            Map.entry("MAKE",          MAKE),

            // Math functions
            Map.entry("SUM",           SUM),
            Map.entry("DIFFERENCE",    DIFFERENCE),
            Map.entry("PRODUCT",       PRODUCT),
            Map.entry("QUOTIENT",      QUOTIENT),
            Map.entry("RANDOM",        RANDOM),

            // Input/output
            Map.entry("PRINT",         PRINT),
            Map.entry("READWORD",      READWORD),

            // Logic
            Map.entry("AND",           AND),
            Map.entry("OR",            OR),
            Map.entry("NOT",           NOT),

            // Boolean
            Map.entry("TRUE",          TRUE),
            Map.entry("FALSE",         FALSE)
    );

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;
            scanToken();
        }
        tokens.add(new Token(EOF, "", line, currentChar()));
        return tokens;
    }

    // Scanning  -------------

    private void scanToken() {
        int tokenLine = line;
        int tokenChar = currentChar();
        char c = advance();

        switch (c) {
            case '[' -> addToken(LBRACKET, "[",  tokenLine, tokenChar);
            case ']' -> addToken(RBRACKET, "]",  tokenLine, tokenChar);
            case '(' -> addToken(LPAREN,   "(",  tokenLine, tokenChar);
            case ')' -> addToken(RPAREN,   ")",  tokenLine, tokenChar);
            case '+' -> addToken(PLUS,     "+",  tokenLine, tokenChar);
            case '*' -> addToken(MULTIPLY, "*",  tokenLine, tokenChar);
            case '/' -> addToken(DIVIDE,   "/",  tokenLine, tokenChar);
            case '=' -> addToken(EQUALS,   "=",  tokenLine, tokenChar);
            case '<' -> {
                // Could be < or <> (not equal)
                if (peek() == '>') {
                    advance();
                    addToken(NOTEQUAL, "<>", tokenLine, tokenChar);
                } else {
                    addToken(LESS, "<", tokenLine, tokenChar);
                }
            }
            case '>' -> addToken(GREATER, ">", tokenLine, tokenChar);
            case '-' -> {
                // Could be subtraction or a negative number
                if (Character.isDigit(peek())) {
                    scanNumber(tokenLine, tokenChar, true);
                } else {
                    addToken(MINUS, "-", tokenLine, tokenChar);
                }
            }
            case ';' -> scanComment(tokenLine, tokenChar);   // comment
            case ':' -> scanVariable(tokenLine, tokenChar);  // value of var
            case '"' -> scanWord(tokenLine, tokenChar);      // string
            default  -> {
                if (Character.isDigit(c)) {
                    current--;  // advance() consumed one char
                    scanNumber(tokenLine, tokenChar, false);
                } else if (Character.isLetter(c) || c == '_') {
                    current--;
                    scanIdentifierOrKeyword(tokenLine, tokenChar);
                } else {
                    addToken(TokenType.ERROR, String.valueOf(c), tokenLine, tokenChar);
                }
            }
        }
    }

    private void scanComment(int tokenLine, int tokenChar) {
        StringBuilder sb = new StringBuilder(";");
        while (!isAtEnd() && peek() != '\n') {
            sb.append(advance());
        }
        addToken(COMMENT, sb.toString(), tokenLine, tokenChar);
    }

    private void scanVariable(int tokenLine, int tokenChar) {
        StringBuilder sb = new StringBuilder(":");
        while (!isAtEnd() && isIdentifierChar(peek())) {
            sb.append(advance());
        }
        addToken(VARIABLE, sb.toString(), tokenLine, tokenChar);
    }

    private void scanWord(int tokenLine, int tokenChar) {
        StringBuilder sb = new StringBuilder("\"");
        while (!isAtEnd() && !Character.isWhitespace(peek())
                && peek() != '[' && peek() != ']'
                && peek() != '(' && peek() != ')') {
            sb.append(advance());
        }
        addToken(WORD, sb.toString(), tokenLine, tokenChar);
    }

    private void scanNumber(int tokenLine, int tokenChar, boolean isNegative) {
        StringBuilder sb = new StringBuilder();
        if (isNegative) sb.append('-');

        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        if (!isAtEnd() && peek() == '.' && Character.isDigit(peekNext())) {
            sb.append(advance()); // consume the dot
            while (!isAtEnd() && Character.isDigit(peek())) {
                sb.append(advance());
            }
        }

        addToken(NUMBER, sb.toString(), tokenLine, tokenChar);
    }

    private void scanIdentifierOrKeyword(int tokenLine, int tokenChar) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isIdentifierChar(peek())) {
            sb.append(advance());
        }

        String text = sb.toString();
        // Check for known keywords
        TokenType type = KEYWORDS.getOrDefault(text.toUpperCase(), TokenType.IDENTIFIER);
        addToken(type, text, tokenLine, tokenChar);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                advance();
                line++;
                start = current;
            } else {
                break;
            }
        }
    }

    // Helpers -----------------

    // 1 char lookahead
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // 2 char lookahead
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private int currentChar() {
        return current - start;
    }

    // Letter, digit, or underscore
    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private void addToken(TokenType type, String text, int line, int startChar) {
        tokens.add(new Token(type, text, line, startChar));
    }
}
