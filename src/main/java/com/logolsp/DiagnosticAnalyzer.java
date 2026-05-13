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

        addUndefinedProcedureCalls(doc, diagnostics);
        addUndefinedVariableReferences(doc, diagnostics);
        addWrongArgumentCounts(doc, diagnostics);

        // Hints
        addUnusedProcedures(doc, diagnostics);
        addUnusedVariables(doc, diagnostics);

        return diagnostics;
    }

    // --- Syntax ---

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

    // --- Semantic ---

    private void addUndefinedProcedureCalls(ParsedDocument doc, List<Diagnostic> out) {
        // Walk the AST looking for ProcedureCallNodes
        visitForUndefinedCalls(doc.ast, doc, out);
    }

    private void visitForUndefinedCalls(AstNode node, ParsedDocument doc, List<Diagnostic> out) {
        if (node == null)
            return;

        if (node instanceof AstNode.ProcedureCallNode call) {
            String name = call.nameToken.text;
            if (doc.symbolTable.findProcedureDeclaration(name) == null) {
                out.add(makeDiagnostic(call.nameToken, "Undefined procedure: '" + name + "'", DiagnosticSeverity.Error));
            }
            // Arguments they may contain further calls
            call.arguments.forEach(arg -> visitForUndefinedCalls(arg, doc, out));

        } else if (node instanceof AstNode.ProcedureDeclarationNode proc) {
            proc.body.forEach(s -> visitForUndefinedCalls(s, doc, out));

        } else if (node instanceof AstNode.RepeatNode repeat) {
            visitForUndefinedCalls(repeat.count, doc, out);
            repeat.body.forEach(s -> visitForUndefinedCalls(s, doc, out));

        } else if (node instanceof AstNode.ForNode forNode) {
            visitForUndefinedCalls(forNode.start, doc, out);
            visitForUndefinedCalls(forNode.end, doc, out);
            if (forNode.step != null)
                visitForUndefinedCalls(forNode.step, doc, out);
            forNode.body.forEach(s -> visitForUndefinedCalls(s, doc, out));

        } else if (node instanceof AstNode.WhileNode whileNode) {
            visitForUndefinedCalls(whileNode.condition, doc, out);
            whileNode.body.forEach(s -> visitForUndefinedCalls(s, doc, out));

        } else if (node instanceof AstNode.IfNode ifNode) {
            visitForUndefinedCalls(ifNode.condition, doc, out);
            ifNode.thenBlock.forEach(s -> visitForUndefinedCalls(s, doc, out));
            ifNode.elseBlock.forEach(s -> visitForUndefinedCalls(s, doc, out));

        } else if (node instanceof AstNode.MakeNode make) {
            visitForUndefinedCalls(make.value, doc, out);

        } else if (node instanceof AstNode.OutputNode output) {
            visitForUndefinedCalls(output.value, doc, out);

        } else if (node instanceof AstNode.BinaryOpNode binary) {
            visitForUndefinedCalls(binary.left, doc, out);
            visitForUndefinedCalls(binary.right, doc, out);

        } else if (node instanceof AstNode.BuiltInCommandNode cmd) {
            cmd.arguments.forEach(arg -> visitForUndefinedCalls(arg, doc, out));
        }
    }

    private void addUndefinedVariableReferences(ParsedDocument doc, List<Diagnostic> out) {
        List<Token> undeclared = doc.symbolTable.getUndeclaredVariableReferences();
        for (Token token : undeclared) {
            out.add(makeDiagnostic(
                    token,
                    "Undefined variable: '" + token.text + "'",
                    DiagnosticSeverity.Error
            ));
        }
    }

    private void addWrongArgumentCounts(ParsedDocument doc, List<Diagnostic> out) {
        visitForWrongArgCounts(doc.ast, doc, out);
    }

    private void visitForWrongArgCounts(AstNode node, ParsedDocument doc, List<Diagnostic> out) {
        if (node == null)
            return;

        if (node instanceof AstNode.ProcedureCallNode call) {
            String name = call.nameToken.text;
            int expected = doc.symbolTable.getProcedureParamCount(name);
            int actual = call.arguments.size();

            if (expected != -1 && actual != expected) {
                out.add(makeDiagnostic(
                        call.nameToken,
                        "'" + name + "' expects " + expected +
                                " argument(s) but got " + actual,
                        DiagnosticSeverity.Error
                ));
            }
            call.arguments.forEach(a -> visitForWrongArgCounts(a, doc, out));

        } else if (node instanceof AstNode.BuiltInCommandNode cmd) {
            int expected = LogoBuiltIns.COMMAND_ARG_COUNTS
                    .getOrDefault(cmd.commandToken.type, 0);
            int actual = cmd.arguments.size();
            if (actual != expected) {
                out.add(makeDiagnostic(
                        cmd.commandToken,
                        "'" + cmd.commandToken.text + "' expects " + expected +
                                " argument(s) but got " + actual,
                        DiagnosticSeverity.Error
                ));
            }
            cmd.arguments.forEach(a -> visitForWrongArgCounts(a, doc, out));

        } else if (node instanceof AstNode.ProcedureDeclarationNode proc) {
            proc.body.forEach(s -> visitForWrongArgCounts(s, doc, out));

        } else if (node instanceof AstNode.RepeatNode repeat) {
            visitForWrongArgCounts(repeat.count, doc, out);
            repeat.body.forEach(s -> visitForWrongArgCounts(s, doc, out));

        } else if (node instanceof AstNode.ForNode forNode) {
            visitForWrongArgCounts(forNode.start, doc, out);
            visitForWrongArgCounts(forNode.end, doc, out);
            if (forNode.step != null)
                visitForWrongArgCounts(forNode.step, doc, out);
            forNode.body.forEach(s -> visitForWrongArgCounts(s, doc, out));

        } else if (node instanceof AstNode.WhileNode whileNode) {
            visitForWrongArgCounts(whileNode.condition, doc, out);
            whileNode.body.forEach(s -> visitForWrongArgCounts(s, doc, out));

        } else if (node instanceof AstNode.IfNode ifNode) {
            visitForWrongArgCounts(ifNode.condition, doc, out);
            ifNode.thenBlock.forEach(s -> visitForWrongArgCounts(s, doc, out));
            ifNode.elseBlock.forEach(s -> visitForWrongArgCounts(s, doc, out));

        } else if (node instanceof AstNode.MakeNode make) {
            visitForWrongArgCounts(make.value, doc, out);

        } else if (node instanceof AstNode.OutputNode output) {
            visitForWrongArgCounts(output.value, doc, out);

        } else if (node instanceof AstNode.BinaryOpNode binary) {
            visitForWrongArgCounts(binary.left, doc, out);
            visitForWrongArgCounts(binary.right, doc, out);
        }
    }

    // --- Hints ---

    private void addUnusedProcedures(ParsedDocument doc, List<Diagnostic> out) {
        for (Token token : doc.symbolTable.getUnusedProcedures()) {
            out.add(makeDiagnostic(
                    token,
                    "Procedure '" + token.text + "' is declared but never called",
                    DiagnosticSeverity.Hint
            ));
        }
    }

    private void addUnusedVariables(ParsedDocument doc, List<Diagnostic> out) {
        visitForUnusedVariables(doc.ast, doc, out);
    }

    private void visitForUnusedVariables(AstNode node, ParsedDocument doc, List<Diagnostic> out) {
        if (node == null)
            return;

        if (node instanceof AstNode.MakeNode make) {
            String varName = make.nameToken.text.substring(1);
            List<Token> refs = doc.symbolTable.getVariableReferences(varName);
            if (refs.isEmpty()) {
                out.add(makeDiagnostic(
                        make.nameToken,
                        "Variable '" + varName + "' is assigned but never used",
                        DiagnosticSeverity.Hint
                ));
            }
        } else if (node instanceof AstNode.ProcedureDeclarationNode proc) {
            proc.body.forEach(s -> visitForUnusedVariables(s, doc, out));

        } else if (node instanceof AstNode.RepeatNode repeat) {
            repeat.body.forEach(s -> visitForUnusedVariables(s, doc, out));

        } else if (node instanceof AstNode.ForNode forNode) {
            forNode.body.forEach(s -> visitForUnusedVariables(s, doc, out));

        } else if (node instanceof AstNode.WhileNode whileNode) {
            whileNode.body.forEach(s -> visitForUnusedVariables(s, doc, out));

        } else if (node instanceof AstNode.IfNode ifNode) {
            ifNode.thenBlock.forEach(s -> visitForUnusedVariables(s, doc, out));
            ifNode.elseBlock.forEach(s -> visitForUnusedVariables(s, doc, out));
        }
    }

    // --- Helpers ---

    private Diagnostic makeDiagnostic(Token token, String message, DiagnosticSeverity severity) {
        return makeDiagnostic(
                token.line, token.startChar, token.length,
                message, severity);
    }

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
