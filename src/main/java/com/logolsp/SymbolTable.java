package com.logolsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Stores declarations and references
public class SymbolTable {

    private final Map<String, Token> procedureDeclarations = new HashMap<>();

    private final Map<String, Integer> procedureParamCounts = new HashMap<>();

    private final Map<String, List<Token>> procedureParams = new HashMap<>();

    private final Map<String, Token> variableDeclarations = new HashMap<>();

    private final Map<String, Token> localVariableDeclarations = new HashMap<>();

    private final Map<String, List<Token>> procedureReferences = new HashMap<>();

    private final Map<String, List<Token>> variableReferences = new HashMap<>();


    // --- Register ---
    public void declareProcedure(Token nameToken, List<Token> params) {
        String key = nameToken.text.toLowerCase();
        procedureDeclarations.put(key, nameToken);
        procedureParamCounts.put(key, params.size());
        procedureParams.put(key, new ArrayList<>(params));

        // Parameters in the procedure's local scope
        for (Token param : params) {
            // Strip the ':'
            String varName = param.text.substring(1).toLowerCase();
            localVariableDeclarations.put(key + "/" + varName, param);
        }
    }

    public void declareVariable(Token nameToken, String currentProcedure) {
        // Strip the quote
        String varName = nameToken.text.substring(1).toLowerCase();

        variableDeclarations.put(varName, nameToken);
    }
}
