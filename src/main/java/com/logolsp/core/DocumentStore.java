package com.logolsp.core;

import com.logolsp.symbols.SymbolTable;
import com.logolsp.symbols.SymbolTableBuilder;
import com.logolsp.lexer.Lexer;
import com.logolsp.lexer.Token;
import com.logolsp.parser.ParseResult;
import com.logolsp.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Holds every opened parsed document
public class DocumentStore {

    private final Map<String, ParsedDocument> documents = new HashMap<>();

    // Parse or reparse a document
    public ParsedDocument update(String uri, String sourceText) {
        List<Token> tokens = new Lexer(sourceText).tokenize();
        ParseResult parseResult = new Parser(tokens).parse();
        SymbolTable symbolTable = new SymbolTableBuilder()
                .build(parseResult.program());

        ParsedDocument doc = new ParsedDocument(
                uri,
                sourceText,
                tokens,
                parseResult.program(),
                symbolTable,
                parseResult.errors()
        );
        documents.put(uri, doc);
        return doc;
    }

    public void remove(String uri) {
        documents.remove(uri);
    }

    public boolean contains(String uri) {
        return documents.containsKey(uri);
    }

    public ParsedDocument get(String uri) {
        return documents.get(uri);
    }
}
