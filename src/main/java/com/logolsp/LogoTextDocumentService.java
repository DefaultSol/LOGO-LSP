package com.logolsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

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

        ParsedDocument doc = documentStore.update(uri, text);
        LOG.info(doc.symbolTable.toString());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        // Only one change event with the full file text
        String text = params.getContentChanges().getFirst().getText();

        ParsedDocument doc = documentStore.update(uri, text);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documentStore.remove(uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        // nothing to handle on save
    }

    // LSP feature handlers
}
