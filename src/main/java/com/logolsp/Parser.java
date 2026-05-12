package com.logolsp;

import java.util.ArrayList;
import java.util.List;

import static com.logolsp.TokenType.*;

// Recursive descent parser for LOGO
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<AstNode> parse() {
        try {
            List<AstNode> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(parseExpression());
            }
            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    // --- Parsing ---

    private AstNode parseStatement() {
        return parseExpression();
    }

    // Literals, binary op, nested expr etc
    private AstNode parseExpression() {
        AstNode left = parsePrimary();

        if (isBinaryOperator()) {
            Token op = advance();
            AstNode right = parsePrimary();
            AstNode.BinaryOpNode node = new AstNode.BinaryOpNode(left, op, right);
            node.setPosition(op);
            return node;
        }

        return left;
    }

    private AstNode parsePrimary() {
        return null;
    }

    // --- Helper methods ---

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isBinaryOperator() {
        return switch (peek().type) {
            case PLUS, MINUS, MULTIPLY, DIVIDE,
                 EQUALS, LESS, GREATER, NOTEQUAL,
                 AND, OR -> true;
            default -> false;
        };
    }
}
