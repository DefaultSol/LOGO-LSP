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

        if (currentProcedure != null) {
            localVariableDeclarations.put(
                    currentProcedure.toLowerCase() + "/" + varName, nameToken);
        } else {
            variableDeclarations.put(varName, nameToken);
        }
    }

    public void referenceProcedure(Token nameToken) {
        String key = nameToken.text.toLowerCase();
        procedureReferences.computeIfAbsent(key, k -> new ArrayList<>()).add(nameToken);
    }

    public void referenceVariable(Token varToken, String currentProcedure) {
        String varName = varToken.text.substring(1).toLowerCase();
        variableReferences.computeIfAbsent(varName, k -> new ArrayList<>()).add(varToken);
    }

    // --- Lookup ---

    public Token findProcedureDeclaration(String name) {
        return procedureDeclarations.get(name.toLowerCase());
    }

    public Token findVariableDeclaration(String name, String currentProcedure) {
        String varName = name.toLowerCase();

        if (currentProcedure != null) {
            Token local = localVariableDeclarations.get(currentProcedure.toLowerCase() + "/" + varName);
            if (local != null)
                return local;
        }

        return variableDeclarations.get(varName);
    }

    public Map<String, List<Token>> getAllVariableReferences() {
        return java.util.Collections.unmodifiableMap(variableReferences);
    }

    public List<Token> getUnusedProcedures() {
        List<Token> unused = new ArrayList<>();
        for (Map.Entry<String, Token> entry : procedureDeclarations.entrySet()) {
            if (!procedureReferences.containsKey(entry.getKey())) {
                unused.add(entry.getValue());
            }
        }
        return unused;
    }

    public List<Token> getUndeclaredVariableReferences() {
        List<Token> undeclared = new ArrayList<>();
        for (Map.Entry<String, List<Token>> entry : variableReferences.entrySet()) {
            String varName = entry.getKey();
            boolean declaredGlobally = variableDeclarations.containsKey(varName);
            boolean declaredLocally = localVariableDeclarations.keySet().stream().anyMatch(k -> k.endsWith("/" + varName));
            if (!declaredGlobally && !declaredLocally) {
                undeclared.addAll(entry.getValue());
            }
        }
        return undeclared;
    }

    public List<Token> getProcedureReferences(String name) {
        return procedureReferences.getOrDefault(name.toLowerCase(), List.of());
    }

    public List<Token> getVariableReferences(String name) {
        return variableReferences.getOrDefault(name.toLowerCase(), List.of());
    }

    public Map<String, Token> getAllProcedureDeclarations() {
        return java.util.Collections.unmodifiableMap(procedureDeclarations);
    }

    public Map<String, Token> getAllVariableDeclarations() {
        return java.util.Collections.unmodifiableMap(variableDeclarations);
    }

    // -1: unknown
    public int getProcedureParamCount(String name) {
        return procedureParamCounts.getOrDefault(name.toLowerCase(), -1);
    }

    public List<Token> getProcedureParams(String name) {
        return procedureParams.getOrDefault(name.toLowerCase(), List.of());
    }

    // --- Util ---

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Symbol Table ---\n");
        sb.append("Procedures:\n");
        procedureDeclarations.forEach((name, token) ->
                sb.append("  ").append(name)
                        .append(" (").append(procedureParamCounts.get(name)).append(" params)")
                        .append(" at line ").append(token.line).append("\n"));
        sb.append("Global variables:\n");
        variableDeclarations.forEach((name, token) ->
                sb.append("  ").append(name)
                        .append(" at line ").append(token.line).append("\n"));
        sb.append("Local variables:\n");
        localVariableDeclarations.forEach((key, token) ->
                sb.append("  ").append(key)
                        .append(" at line ").append(token.line).append("\n"));
        return sb.toString();
    }
}
