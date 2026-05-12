package com.logolsp;


import java.util.List;

// Walks the AST, fills SymbolTable
public class SymbolTableBuilder {

    private final SymbolTable table = new SymbolTable();

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
    }

    private void visitProcedureCall(AstNode.ProcedureCallNode call) {
    }

    private void visitMake(AstNode.MakeNode make) {
    }

    private void visitVariableRef(AstNode.VariableRefNode ref) {
    }

    private void visitFor(AstNode.ForNode forNode) {
    }

    private void visitChildren(List<AstNode> nodes) {
    }
}
