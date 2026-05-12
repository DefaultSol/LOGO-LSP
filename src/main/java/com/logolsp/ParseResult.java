package com.logolsp;

import java.util.List;

public record ParseResult(AstNode.ProgramNode program, List<ParseError> errors) {}