package com.logolsp;

public class Token {

    public final TokenType type;
    public final String text;
    public final int line;
    public final int startChar;
    public final int length;

    public Token(TokenType type, String text, int line, int startChar) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.startChar = startChar;
        this.length = text.length();
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", value='" + text + '\'' + ", line='" + line + '\'' + ", start=" + startChar + ", length=" + length + '}';
    }
}
