package com.logolsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class LogoLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client;
    private final LogoTextDocumentService textDocumentService = new LogoTextDocumentService();
    private final LogoWorkspaceService workspaceService = new LogoWorkspaceService();

    private static final Logger LOG = Logger.getLogger(LogoLanguageServer.class.getName());

    public static void main(String[] args) {
        InputStream systemIn = System.in;
        OutputStream systemOut = System.out;
        // redirect System.out to System.err because we need to prevent
        // System.out from receiving anything that isn't an LSP message
        System.setOut(new PrintStream(System.err));
        LogoLanguageServer server = new LogoLanguageServer();
        Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, systemIn, systemOut);
        server.connect(launcher.getRemoteProxy());
        launcher.startListening();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        LOG.info("Initialize request received from client: " + params.getClientInfo());

        ServerCapabilities capabilities = new ServerCapabilities();

        // Send full file content on every change
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        // Semantic tokens capability
        SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
        semanticTokensOptions.setLegend(new SemanticTokensLegend(
                SemanticTokensProvider.TOKEN_TYPES,
                SemanticTokensProvider.TOKEN_MODIFIERS
        ));
        semanticTokensOptions.setFull(true);
        semanticTokensOptions.setRange(false);

        capabilities.setSemanticTokensProvider(semanticTokensOptions);

        capabilities.setDefinitionProvider(true);

        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        LOG.info("Shutdown request received.");
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public void exit() {
        LOG.info("Exit notification received. Exiting.");
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        textDocumentService.setClient(client);
        LOG.info("Client connection established.");
    }
}
