package com.logolsp.parser;

import com.logolsp.lexer.Token;

public record ParseError(String message, int line, int startChar, int length) {

    // Convenience constructor from a token
    public static ParseError at(Token token, String message) {
        return new ParseError(message, token.line, token.startChar, token.length);
    }
}
