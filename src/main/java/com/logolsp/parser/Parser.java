package com.logolsp.parser;

import com.logolsp.core.LogoBuiltIns;
import com.logolsp.lexer.Token;
import com.logolsp.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

// Recursive descent parser for LOGO
// Will keep working even on broken files, to provide a partial AST and a list of all errors
public class Parser {
    private final List<Token> tokens;
    private final List<ParseError> errors = new ArrayList<>();
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ParseResult parse() {
        List<AstNode> statements = new ArrayList<>();
        while (!isAtEnd()) {
            AstNode statement = parseStatement();
            if (statement != null) statements.add(statement);
        }
        return new ParseResult(new AstNode.ProgramNode(statements), errors);
    }

    private AstNode parseStatement() {
        // skip errors and comments
        if (check(TokenType.ERROR)) {
            Token badToken = advance();
            errors.add(ParseError.at(badToken, "Unexpected character: '" + badToken.text + "'"));
            return null;
        }
        if (check(TokenType.COMMENT)) {
            advance();
            return null;
        }

        return switch (peek().type) {
            case TO         -> parseProcedureDeclaration();
            case MAKE       -> parseMake();
            case REPEAT     -> parseRepeat();
            case FOR        -> parseFor();
            case WHILE      -> parseWhile();
            case IF         -> parseIf();
            case IFELSE     -> parseIfElse();
            case OUTPUT     -> parseOutput();
            case STOP       -> parseStop();
            case IDENTIFIER -> parseProcedureCall();
            default         -> {
                if (LogoBuiltIns.isBuiltIn(peek().type)) {
                    yield parseBuiltinCommand();
                }

                Token unexpected = advance();
                errors.add(ParseError.at(unexpected,
                        "Unexpected token: '" + unexpected.text + "'"));
                synchronize();
                yield new AstNode.ErrorNode("Unexpected token", unexpected);
            }
        };
    }

    private AstNode parseProcedureDeclaration() {
        Token toToken = advance();

        if (!check(TokenType.IDENTIFIER)) {
            errors.add(ParseError.at(peek(),
                    "Expected a procedure name after TO, got: '" + peek().text + "'"));
            synchronize();
            return new AstNode.ErrorNode("Missing procedure name", toToken);
        }
        Token nameToken = advance();

        // Collect parameter tokens until statements start
        List<Token> params = new ArrayList<>();
        while (check(TokenType.VARIABLE)) {
            params.add(advance());
        }

        // Parse statements until END or EOF
        List<AstNode> body = new ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            AstNode statement = parseStatement();
            if (statement != null) body.add(statement);
        }

        AstNode.ProcedureDeclarationNode node = new AstNode.ProcedureDeclarationNode(nameToken, params, body);

        if (!check(TokenType.END)) {
            // Reached EOF without finding END
            errors.add(ParseError.at(peek(),
                    "Procedure '" + nameToken.text + "' is missing END"));
        } else {
            Token endToken = advance();
            node.endLine = endToken.line;
        }

        node.setPosition(toToken);
        return node;
    }

    private AstNode parseMake() {
        Token makeToken = advance();

        if (!check(TokenType.WORD)) {
            errors.add(ParseError.at(peek(),
                    "Expected a quoted variable name after MAKE"));
            synchronize();
            return new AstNode.ErrorNode("Missing variable name after MAKE", makeToken);
        }
        Token nameToken = advance();
        AstNode value = parseExpression();

        AstNode.MakeNode node = new AstNode.MakeNode(nameToken, value);
        node.setPosition(makeToken);
        return node;
    }

    private AstNode parseRepeat() {
        Token repeatToken = advance();
        AstNode count = parseExpression();
        List<AstNode> body = parseBlock();

        AstNode.RepeatNode node = new AstNode.RepeatNode(count, body);
        node.setPosition(repeatToken);
        return node;
    }

    private AstNode parseWhile() {
        Token whileToken = advance();
        AstNode condition = parseExpression();
        List<AstNode> body = parseBlock();

        AstNode.WhileNode node = new AstNode.WhileNode(condition, body);
        node.setPosition(whileToken);
        return node;
    }

    // FOR [ :var start end step ] [ body ]
    private AstNode parseFor() {
        Token forToken = advance();

        if (!check(TokenType.LBRACKET)) {
            errors.add(ParseError.at(peek(), "Expected '[' after FOR"));
            synchronize();
            return new AstNode.ErrorNode("Missing FOR control list", forToken);
        }
        advance();

        Token varToken = null;
        if (check(TokenType.VARIABLE)) {
            varToken = advance();
        } else {
            errors.add(ParseError.at(peek(), "Expected variable in FOR"));
        }

        AstNode start = parseExpression();
        AstNode end   = parseExpression();
        AstNode step = parseExpression();

        if (!check(TokenType.RBRACKET)) {
            errors.add(ParseError.at(peek(), "Expected ']' to close FOR"));
        } else {
            advance();
        }

        List<AstNode> body = parseBlock();

        AstNode.ForNode node = new AstNode.ForNode(varToken, start, end, step, body);
        node.setPosition(forToken);
        return node;
    }

    private AstNode parseIf() {
        Token ifToken = advance();
        AstNode condition = parseExpression();
        List<AstNode> thenBlock = parseBlock();

        AstNode.IfNode node = new AstNode.IfNode(condition, thenBlock, List.of());  // empty else
        node.setPosition(ifToken);
        return node;
    }

    private AstNode parseIfElse() {
        Token ifToken = advance();
        AstNode condition = parseExpression();
        List<AstNode> thenBlock = parseBlock();
        List<AstNode> elseBlock = parseBlock();

        AstNode.IfNode node = new AstNode.IfNode(condition, thenBlock, elseBlock);
        node.setPosition(ifToken);
        return node;
    }

    private AstNode parseOutput() {
        Token outputToken = advance();
        AstNode value = parseExpression();

        AstNode.OutputNode node = new AstNode.OutputNode(value);
        node.setPosition(outputToken);
        return node;
    }

    private AstNode parseStop() {
        Token stopToken = advance();
        AstNode.StopNode node = new AstNode.StopNode();
        node.setPosition(stopToken);
        return node;
    }

    private AstNode parseProcedureCall() {
        Token nameToken = advance();
        List<AstNode> args = new ArrayList<>();

        // Parse arguments until new statement is started
        // Collect arguments only on the same line
        int callLine = nameToken.line;
        while (isExpressionStart() && peek().line == callLine) {
            args.add(parseExpression());
        }

        AstNode.ProcedureCallNode node = new AstNode.ProcedureCallNode(nameToken, args);
        node.setPosition(nameToken);
        return node;
    }

    private AstNode parseBuiltinCommand() {
        Token commandToken = advance();
        int argCount = LogoBuiltIns.COMMAND_ARG_COUNTS.get(commandToken.type);

        List<AstNode> args = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            if (!isExpressionStart()) {
                errors.add(ParseError.at(peek(),
                        "'" + commandToken.text + "' expects " + argCount +
                                " argument(s), but found only " + i));
                break;
            }
            args.add(parseExpression());
        }

        AstNode.BuiltInCommandNode node = new AstNode.BuiltInCommandNode(commandToken, args);
        node.setPosition(commandToken);
        return node;
    }

    // [ statements ]
    private List<AstNode> parseBlock() {
        List<AstNode> statements = new ArrayList<>();

        if (!check(TokenType.LBRACKET)) {
            errors.add(ParseError.at(peek(), "Expected '[' to start a block"));
            return statements;
        }
        advance();

        while (!check(TokenType.RBRACKET) && !isAtEnd()) {
            AstNode statement = parseStatement();
            if (statement != null) statements.add(statement);
        }

        if (!check(TokenType.RBRACKET)) {
            errors.add(ParseError.at(peek(), "Expected ']' to close block"));
        } else {
            advance();
        }

        return statements;
    }

    // Literals, binary op, nested expr
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
        Token t = peek();

        // (expr)
        if (check(TokenType.LPAREN)) {
            advance();
            AstNode inner = parseExpression();
            if (!check(TokenType.RPAREN)) {
                errors.add(ParseError.at(peek(), "Expected ')' to close expression"));
            } else {
                advance();
            }
            return inner;
        }

        if (check(TokenType.NUMBER)) {
            advance();
            AstNode.NumberNode node = new AstNode.NumberNode(t);
            node.setPosition(t);
            return node;
        }

        if (check(TokenType.VARIABLE)) {
            advance();
            AstNode.VariableRefNode node = new AstNode.VariableRefNode(t);
            node.setPosition(t);
            return node;
        }

        if (check(TokenType.WORD)) {
            advance();
            AstNode.WordNode node = new AstNode.WordNode(t);
            node.setPosition(t);
            return node;
        }

        if (check(TokenType.TRUE)) {
            advance();
            AstNode.BooleanNode node = new AstNode.BooleanNode(t, true);
            node.setPosition(t);
            return node;
        }

        if (check(TokenType.FALSE)) {
            advance();
            AstNode.BooleanNode node = new AstNode.BooleanNode(t, false);
            node.setPosition(t);
            return node;
        }

        // E.g. SUM 1 2, RANDOM 10, etc.
        if (LogoBuiltIns.isBuiltIn(t.type) && LogoBuiltIns.COMMAND_ARG_COUNTS.get(t.type) > 0) {
            return parseBuiltinCommand();
        }

        errors.add(ParseError.at(t, "Expected an expression, got: '" + t.text + "'"));
        advance();
        return new AstNode.ErrorNode("Expected expression", t);
    }

    // --- Helper methods ---

    // Skips unexpected tokens until new statement is found
    private void synchronize() {
        while (!isAtEnd()) {
            TokenType type = peek().type;
            if (type == TokenType.TO      ||
                    type == TokenType.END     ||
                    type == TokenType.REPEAT  ||
                    type == TokenType.FOR     ||
                    type == TokenType.WHILE   ||
                    type == TokenType.IF      ||
                    type == TokenType.IFELSE  ||
                    type == TokenType.MAKE    ||
                    type == TokenType.OUTPUT  ||
                    type == TokenType.STOP    ||
                    type == TokenType.RBRACKET||
                    LogoBuiltIns.isBuiltIn(type)) {
                return;
            }
            advance();
        }
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Returns true if the current token could start an expression
    private boolean isExpressionStart() {
        return switch (peek().type) {
            case NUMBER, VARIABLE, WORD, TRUE, FALSE, LPAREN -> true;
            default -> LogoBuiltIns.isBuiltIn(peek().type)
                    && LogoBuiltIns.COMMAND_ARG_COUNTS.getOrDefault(peek().type, 0) > 0;
        };
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
