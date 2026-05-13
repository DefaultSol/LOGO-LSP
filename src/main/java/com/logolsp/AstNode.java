package com.logolsp;

import java.util.List;

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

    // Root
    public static final class ProgramNode extends AstNode {
        public final List<AstNode> statements;
        public ProgramNode(List<AstNode> statements) {
            this.statements = statements;
        }
    }

    public static final class NumberNode extends AstNode {
        public final Token token;
        public NumberNode(Token token) {
            this.token = token;
        }
    }

    public static final class WordNode extends AstNode {
        public final Token token;
        public WordNode(Token token) {
            this.token = token;
        }
    }

    public static final class BooleanNode extends AstNode {
        public final Token token;
        public final boolean value;
        public BooleanNode(Token token, boolean value) {
            this.token = token;
            this.value = value;
        }
    }

    public static final class VariableRefNode extends AstNode {
        public final Token token;
        public VariableRefNode(Token token) {
            this.token = token;
        }
    }

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

    public static final class ProcedureDeclarationNode extends AstNode {
        public final Token nameToken;
        public final List<Token> paramTokens;
        public final List<AstNode> body;
        public int endLine = -1;

        public ProcedureDeclarationNode(Token nameToken, List<Token> paramTokens, List<AstNode> body) {
            this.nameToken = nameToken;
            this.paramTokens = paramTokens;
            this.body = body;
        }
    }

    public static final class ProcedureCallNode extends AstNode {
        public final Token nameToken;
        public final List<AstNode> arguments;
        public ProcedureCallNode(Token nameToken, List<AstNode> arguments) {
            this.nameToken = nameToken;
            this.arguments = arguments;
        }
    }

    public static final class BuiltInCommandNode extends AstNode {
        public final Token commandToken;
        public final List<AstNode> arguments;
        public BuiltInCommandNode(Token commandToken, List<AstNode> arguments) {
            this.commandToken = commandToken;
            this.arguments = arguments;
        }
    }

    public static final class RepeatNode extends AstNode {
        public final AstNode count;
        public final List<AstNode> body;
        public RepeatNode(AstNode count, List<AstNode> body) {
            this.count = count;
            this.body = body;
        }
    }

    public static final class ForNode extends AstNode {
        public final Token varToken;
        public final AstNode start;
        public final AstNode end;
        public final AstNode step;
        public final List<AstNode> body;
        public ForNode(Token varToken, AstNode start, AstNode end, AstNode step, List<AstNode> body) {
            this.varToken = varToken;
            this.start = start;
            this.end = end;
            this.step = step;
            this.body = body;
        }
    }

    public static final class WhileNode extends AstNode {
        public final AstNode condition;
        public final List<AstNode> body;
        public WhileNode(AstNode condition, List<AstNode> body) {
            this.condition = condition;
            this.body = body;
        }
    }

    public static final class IfNode extends AstNode {
        public final AstNode condition;
        public final List<AstNode> thenBlock;
        public final List<AstNode> elseBlock;   // can be empty
        public IfNode(AstNode condition, List<AstNode> thenBlock, List<AstNode> elseBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }
    }

    public static final class MakeNode extends AstNode {
        public final Token nameToken;
        public final AstNode value;
        public MakeNode(Token nameToken, AstNode value) {
            this.nameToken = nameToken;
            this.value = value;
        }
    }

    public static final class OutputNode extends AstNode {
        public final AstNode value;
        public OutputNode(AstNode value) {
            this.value = value;
        }
    }

    public static final class StopNode extends AstNode {}

    public static final class ErrorNode extends AstNode {
        public final String message;
        public ErrorNode(String message, Token token) {
            this.message = message;
            setPosition(token);
        }
    }
}
