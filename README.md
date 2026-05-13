# LOGO Language Server (LSP)

This project is a Language Server for the **LOGO** programming language, built in Java on top of Eclipse LSP4J.

## Features

- Syntax highlighting for the language (semantic tokens)
- Go‑to‑declaration for procedures and variables
- Diagnostics: parser errors and selected semantic checks are reported to the client in real time


### Quick start

Prerequisites:
- Java 17+ (JDK)
- Windows, macOS, or Linux

Build a runnable (fat) JAR:
```
./gradlew jar      # on Linux/macOS
gradlew.bat jar    # on Windows
```

The build produces a single, self‑contained JAR at:
```
build/libs/LOGO-LSP-1.0-SNAPSHOT.jar
```

Run the server (stdio mode):
```
java -jar build/libs/LOGO-LSP-1.0-SNAPSHOT.jar
```

The server communicates over stdin/stdout as per the LSP specification. It is intended to be launched and managed by an LSP client.


### Using with an LSP client

You can connect any generic LSP client that supports launching a stdio server. Below are two convenient options.

1) JetBrains IDEs via LSP Support (LSP4IJ)
- Install the plugin: Settings/Preferences → Plugins → Marketplace → search for "LSP Support" and install.
- Open Settings/Preferences → Tools → Language Servers.
- Click "+" to add a server.
  - Name: LOGO LSP
  - File name patterns: `*.logo`
  - Start mode: "Run" (process)
  - Command: `java -jar <absolute-path-to>/build/libs/LOGO-LSP-1.0-SNAPSHOT.jar`
  - Working directory: project root (or leave empty)
- Apply and restart the server when prompted.

2) VS Code (with a lightweight generic runner)
- VS Code does not ship a built‑in generic LSP connector, but you can use community extensions that run a stdio server for a given file pattern, or create a minimal client extension using `vscode-languageclient` that launches:
  - Command: `java`
  - Args: `-jar <workspace>/build/libs/LOGO-LSP-1.0-SNAPSHOT.jar`
  - Document selector: `"language": "logo"` (or `"pattern": "**/*.logo"`)

Example LOGO file to try: `src/main/java/com/logolsp/test.logo` (you can copy it into your workspace and edit).


### Architecture and project layout

The design follows a small, layered structure:

- **Entry point**
  - `src/main/java/com/logolsp/Main.java`
    - Creates and launches the LSP server over stdin/stdout using LSP4J

- **LSP endpoint**
  - `src/main/java/com/logolsp/lsp/LogoLanguageServer.java`
    - Implements LSP server lifecycle and capabilities (semantic tokens, definitions, diagnostics)
  - `src/main/java/com/logolsp/lsp/LogoTextDocumentService.java`
    - Handles document open/change/close, computes semantic tokens and diagnostics, routes definition requests
  - `src/main/java/com/logolsp/lsp/LogoWorkspaceService.java`
    - Workspace‑level hooks (kept minimal for this assignment)

- **Core document model**
  - `src/main/java/com/logolsp/core/DocumentStore.java`
    - Maintains parsed state for open documents
  - `src/main/java/com/logolsp/core/ParsedDocument.java`
    - Immutable snapshot of tokens, AST, symbol table, and parse errors for a document
  - `src/main/java/com/logolsp/core/LogoBuiltIns.java`
    - Built‑in LOGO procedures/operators (for highlighting and resolution)

- **Language implementation**
  - Lexing: `src/main/java/com/logolsp/lexer/*` (`Lexer`, `Token`, `TokenType`)
  - Parsing + AST: `src/main/java/com/logolsp/parser/*` (`Parser`, `AstNode`, `ParseResult`, `ParseError`)
  - Symbols: `src/main/java/com/logolsp/symbols/*` (`SymbolTable`, `SymbolTableBuilder`)

- **LSP features**
  - `SemanticTokensProvider` — classifies tokens and encodes semantic tokens for LSP
  - `DefinitionProvider` — maps variable/procedure uses to their declarations
  - `DiagnosticAnalyzer` — converts parse/semantic issues into LSP diagnostics


### Building blocks and decisions

- **Transport:** stdio via LSP4J (`LSPLauncher.createServerLauncher`) for broad client compatibility
- **Single‑pass document pipeline:** tokenize → parse → symbols → snapshot; keeps feature code stateless and predictable
- **Scope rules for variables:** procedure‑local first, then global, aligned with common LOGO dialect expectations
- Token types are intentionally compact and mapped to client themes; the set can be extended easily


### Resources used

- Crafting Interpreters:
  https://craftinginterpreters.com/
- Groovy Language Server (reference implementation using LSP):
  https://github.com/GroovyLanguageServer/groovy-language-server
- Language Server Protocol v3.17 Specification:
  https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/

