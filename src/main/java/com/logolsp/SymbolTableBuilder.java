package com.logolsp;

// Walks the AST, fills SymbolTable
public class SymbolTableBuilder {

    private final SymbolTable table = new SymbolTable();
    private String currentProcedure = null;

    public SymbolTable build(AstNode.ProgramNode program) {
        for (AstNode statement : program.statements) {
            visitNode(statement);
        }
        return table;
    }

    private void visitNode(AstNode node) {
        if (node == null) return;

        switch (node) {
            case AstNode.ProcedureDeclarationNode proc -> visitProcedure(proc);
            case AstNode.ProcedureCallNode call -> visitProcedureCall(call);
            case AstNode.BuiltInCommandNode cmd -> visitChildren(cmd.arguments);
            case AstNode.MakeNode make -> visitMake(make);
            case AstNode.VariableRefNode ref -> visitVariableRef(ref);
            case AstNode.RepeatNode repeat -> {
                visitNode(repeat.count);
                visitChildren(repeat.body);
            }
            case AstNode.ForNode forNode -> visitFor(forNode);
            case AstNode.WhileNode whileNode -> {
                visitNode(whileNode.condition);
                visitChildren(whileNode.body);
            }
            case AstNode.IfNode ifNode -> {
                visitNode(ifNode.condition);
                visitChildren(ifNode.thenBlock);
                visitChildren(ifNode.elseBlock);
            }
            case AstNode.OutputNode output -> visitNode(output.value);
            case AstNode.BinaryOpNode binary -> {
                visitNode(binary.left);
                visitNode(binary.right);
            }
            // Leaf nodes
            default -> {}
        }
    }

    private void visitProcedure(AstNode.ProcedureDeclarationNode proc) {
        table.declareProcedure(proc.nameToken, proc.paramTokens);

        // Enter this procedure's scope
        String previousProcedure = currentProcedure;
        currentProcedure = proc.nameToken.text;

        visitChildren(proc.body);

        // Restore the previous scope
        currentProcedure = previousProcedure;
    }

    private void visitProcedureCall(AstNode.ProcedureCallNode call) {
        table.referenceProcedure(call.nameToken);
        // Arguments may contain variable references
        visitChildren(call.arguments);
    }

    private void visitMake(AstNode.MakeNode make) {
        table.declareVariable(make.nameToken, currentProcedure);
        visitNode(make.value);
    }

    private void visitVariableRef(AstNode.VariableRefNode ref) {
        table.referenceVariable(ref.token, currentProcedure);
    }

    private void visitFor(AstNode.ForNode forNode) {
        // FOR loop variable is local to the loop body
        if (forNode.varToken != null) {
            table.declareVariable(
                    new Token(TokenType.WORD,
                            forNode.varToken.text,
                            forNode.varToken.line,
                            forNode.varToken.startChar),
                            currentProcedure);
        }
        visitNode(forNode.start);
        visitNode(forNode.end);
        if (forNode.step != null)
            visitNode(forNode.step);
        visitChildren(forNode.body);
    }

    private void visitChildren(java.util.List<AstNode> nodes) {
        for (AstNode node : nodes) {
            visitNode(node);
        }
    }
}
