package com.logolsp;

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
}
