package com.logolsp.core;

import com.logolsp.symbols.SymbolTable;
import com.logolsp.lexer.Token;
import com.logolsp.parser.AstNode;
import com.logolsp.parser.ParseError;

import java.util.List;

// Processed source file, created on every change
public class ParsedDocument {

    public final String uri;
    public final String sourceText;
    public final List<Token> tokens;
    public final AstNode.ProgramNode ast;
    public final SymbolTable symbolTable;
    public final List<ParseError> errors;

    public ParsedDocument(
            String uri,
            String sourceText,
            List<Token> tokens,
            AstNode.ProgramNode ast,
            SymbolTable symbolTable,
            List<ParseError> errors) {
        this.uri = uri;
        this.sourceText = sourceText;
        this.tokens = tokens;
        this.ast = ast;
        this.symbolTable = symbolTable;
        this.errors = errors;
    }
}
