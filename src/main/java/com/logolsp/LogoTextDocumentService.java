package com.logolsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import java.util.List;

import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class LogoTextDocumentService implements TextDocumentService {

    private static final Logger LOG = Logger.getLogger(LogoTextDocumentService.class.getName());

    private LanguageClient client;
    private final DocumentStore documentStore = new DocumentStore();

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        LOG.info("File opened: " + uri);

        ParsedDocument doc = documentStore.update(uri, text);
        LOG.info(doc.symbolTable.toString());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        // Only one change event with the full file text
        String text = params.getContentChanges().get(0).getText();
        LOG.info("File changed: " + uri);

        ParsedDocument doc = documentStore.update(uri, text);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documentStore.remove(uri);
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
}
