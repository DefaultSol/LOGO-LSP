package com.logolsp;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

// Handles go-to-declaration requests
public class DefinitionProvider {

    public Location findDefinition(ParsedDocument doc, Position cursorPosition) {
        Token tokenAtCursor = findTokenAt(doc, cursorPosition);
        if (tokenAtCursor == null) return null;

        return switch (tokenAtCursor.type) {
            case VARIABLE -> findVariableDeclaration(doc, tokenAtCursor);
            case IDENTIFIER -> findProcedureDeclaration(doc, tokenAtCursor);
            case WORD -> findVariableDeclaration(doc, tokenAtCursor);
            default -> null;
        };
    }

    private Token findTokenAt(ParsedDocument doc, Position cursor) {
        int targetLine = cursor.getLine();
        int targetChar = cursor.getCharacter();

        for (Token token : doc.tokens) {
            if (token.type == TokenType.EOF)
                continue;

            if (token.line == targetLine
                    && token.startChar <= targetChar
                    && targetChar < token.startChar + token.length) {
                return token;
            }
        }

        return null;
    }

    private Location findVariableDeclaration(ParsedDocument doc, Token token) {
        // Strip leading : or "
        String name = stripPrefix(token.text);

        // Determine scope
        String containingProcedure = findContainingProcedure(doc, token);

        Token declaration = doc.symbolTable.findVariableDeclaration(
                name, containingProcedure);

        if (declaration == null)
            return null;
        return toLocation(doc.uri, declaration);
    }

    private Location findProcedureDeclaration(ParsedDocument doc, Token token) {
        Token declaration = doc.symbolTable.findProcedureDeclaration(token.text);
        if (declaration == null)
            return null;
        return toLocation(doc.uri, declaration);
    }

    private String findContainingProcedure(ParsedDocument doc, Token token) {
        for (AstNode node : doc.ast.statements) {
            if (node instanceof AstNode.ProcedureDeclarationNode proc) {
                if (isInsideProcedure(proc, token)) {
                    return proc.nameToken.text;
                }
            }
        }
        return null;
    }

    // LOGO procedures can't be nested, so we use line numbers
    private boolean isInsideProcedure(AstNode.ProcedureDeclarationNode proc, Token token) {
        int start = proc.line;   // TO keyword
        int end = proc.endLine == -1 ? Integer.MAX_VALUE : proc.endLine;
        return token.line >= start && token.line <= end;
    }

    private Location toLocation(String uri, Token token) {
        Position start = new Position(token.line, token.startChar);
        Position end = new Position(token.line, token.startChar + token.length);
        Range range = new Range(start, end);
        return new Location(uri, range);
    }

    private String stripPrefix(String text) {
        if (text.startsWith(":") || text.startsWith("\"")) {
            return text.substring(1);
        }
        return text;
    }
}