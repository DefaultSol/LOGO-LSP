package com.logolsp;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

// Analyses a parsed document for syntax errors (already caught by Parser) and semantic errors
public class DiagnosticAnalyzer {

    private static final String SOURCE = "logo-lsp";

    public List<Diagnostic> analyze(ParsedDocument doc) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        addSyntaxErrors(doc, diagnostics);

//        addUndefinedProcedureCalls(doc, diagnostics);
//        addUndefinedVariableReferences(doc, diagnostics);
//        addWrongArgumentCounts(doc, diagnostics);
//
//        // Hints
//        addUnusedProcedures(doc, diagnostics);
//        addUnusedVariables(doc, diagnostics);

        return diagnostics;
    }

    private void addSyntaxErrors(ParsedDocument doc, List<Diagnostic> out) {
        for (ParseError error : doc.errors) {
            out.add(makeDiagnostic(
                    error.line(),
                    error.startChar(),
                    error.length(),
                    error.message(),
                    DiagnosticSeverity.Error
            ));
        }
    }

    // --- Helpers ---

    private Diagnostic makeDiagnostic(int line, int startChar, int length, String message, DiagnosticSeverity severity) {
        Position start = new Position(line, startChar);
        Position end = new Position(line, startChar + length);
        Range range = new Range(start, end);

        Diagnostic diagnostic = new Diagnostic(range, message);
        diagnostic.setSeverity(severity);
        diagnostic.setSource(SOURCE);
        return diagnostic;
    }
}
