package com.logolsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Stores declarations and references
public class SymbolTable {

    private final Map<String, Token> procedureDeclarations = new HashMap<>();

    private final Map<String, List<Token>> procedureParams = new HashMap<>();

    private final Map<String, Token> variableDeclarations = new HashMap<>();

    private final Map<String, List<Token>> procedureReferences = new HashMap<>();

    private final Map<String, List<Token>> variableReferences = new HashMap<>();
}
