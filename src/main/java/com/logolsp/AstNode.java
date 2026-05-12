package com.logolsp;

public abstract class AstNode {

    // Token position
    public int line;
    public int startChar;
    public int length;

    public void setPosition(Token token) {
        this.line = token.line;
        this.startChar = token.startChar;
        this.length = token.length;
    }

    // --- Node types ---

    public static final class BinaryOpNode extends AstNode {
        public AstNode left;
        public Token operator;
        public AstNode right;
        public BinaryOpNode(AstNode left, Token operator, AstNode right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }
}
