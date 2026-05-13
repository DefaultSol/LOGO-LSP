package com.logolsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class LogoTextDocumentService implements TextDocumentService {

    private static final Logger LOG = Logger.getLogger(LogoTextDocumentService.class.getName());

    private LanguageClient client;
    private final DocumentStore documentStore = new DocumentStore();

    private final DiagnosticAnalyzer diagnosticAnalyzer = new DiagnosticAnalyzer();

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        LOG.info("File opened: " + uri);

        ParsedDocument doc = documentStore.update(uri, text);
        publishDiagnostics(doc);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        // Only one change event with the full file text
        String text = params.getContentChanges().get(0).getText();
        LOG.info("File changed: " + uri);

        ParsedDocument doc = documentStore.update(uri, text);
        publishDiagnostics(doc);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documentStore.remove(uri);

        // Clear diagnostics for closed file
        if (client != null) {
            PublishDiagnosticsParams clearParams = new PublishDiagnosticsParams();
            clearParams.setUri(uri);
            clearParams.setDiagnostics(List.of());
            client.publishDiagnostics(clearParams);
        }

        LOG.info("File closed: " + uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // nothing to handle on save
        LOG.info("File saved: " + params.getTextDocument().getUri());
    }

    // --- LSP feature handlers ---

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {

        String uri = params.getTextDocument().getUri();
        ParsedDocument doc = documentStore.get(uri);

        if (doc == null) {
            return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
        }

        List<Integer> data = new SemanticTokensProvider().computeTokens(doc);
        return CompletableFuture.completedFuture(new SemanticTokens(data));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {

        String uri = params.getTextDocument().getUri();
        ParsedDocument doc = documentStore.get(uri);

        if (doc == null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of()));
        }

        Location location = new DefinitionProvider().findDefinition(doc, params.getPosition());

        if (location == null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of()));
        }

        return CompletableFuture.completedFuture(Either.forLeft(List.of(location)));
    }

    private void publishDiagnostics(ParsedDocument doc) {
        if (client == null)
            return;

        List<Diagnostic> diagnostics = diagnosticAnalyzer.analyze(doc);

        PublishDiagnosticsParams params = new PublishDiagnosticsParams();
        params.setUri(doc.uri);
        params.setDiagnostics(diagnostics);

        client.publishDiagnostics(params);

        LOG.info("Published " + diagnostics.size() + " diagnostic(s) for " + doc.uri);
    }
}
