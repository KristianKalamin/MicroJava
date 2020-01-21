package com.kalamin.pp.micro.java.parser;

import com.kalamin.pp.micro.java.scanner.Token;
import com.kalamin.pp.micro.java.scanner.TokenData;

import java.util.Objects;

import static com.kalamin.pp.micro.java.scanner.TokenScanner.*;
import static com.kalamin.pp.micro.java.symtab.Table.lookFor;
import static com.kalamin.pp.micro.java.symtab.Table.searchSymTabByKind;

class Statements {

    Statements() throws Exception {
        program();
        if (!Objects.equals(lookFor("main"), "main"))
            throw new Exception("Program must have 'main' function");
    }

    private void program() {
        try {
            classStatement();
        } catch (Exception e) {
            System.out.println("Syntax error");
            e.printStackTrace();
            System.exit(-1);
        }

    }

    static void methodCall() throws Exception {
        new Expression().id().check()
                .lparen()
                .methodParams(new Expression().checkArgs())
                .rparen()
                .callFunc();
    }

    private static void classStatement() throws Exception {
        new Expression()
                .classPart()
                .className()
                .lbracked()
                .classBody()
                .rbracked();
    }

    static void method() {
        while (counter < tokens.size() - 1) {
            try {
                new Expression()
                        .accessModifier()
                        .methodType()
                        .type()
                        .id()
                        .lparen()
                        .methodParams(new Expression().recursivelyAddParams())
                        .rparen()
                        .insertMethod()
                        .lbracked()
                        .methodBody()
                        .rbracked();
            } catch (Exception e) {
                System.out.println("Syntax error");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    static void bodyStatement() throws Exception {
        while (Objects.requireNonNull(peekNextToken()).getToken() != Token.RETURN
                && Objects.requireNonNull(peekNextToken()).getToken() != Token.RBRACKED) {
            blockStatement(Objects.requireNonNull(peekNextToken()));
        }
        returnStatement();
    }

    static void blockStatement(TokenData tokenData) throws Exception {
        switch (tokenData.getToken()) {
            case INT:
            case CHAR:
                variableStatement();
                break;
            case ID: {
                if (searchSymTabByKind("FUN", tokenData.getName()) != -1)
                    methodCall();
                else
                    variableAssignmentStatement();
            }
            break;
            case WHILE:
                whileStatement();
                break;
            case IF:
                ifStatement();
                break;
            case ELSE:
                elseStatement();
                break;
            default:
                break;
        }
    }

    private static void variableAssignmentStatement() throws Exception {
        Expression e = new Expression();
        e
                .id()
                .check();

        if (!e.postfixOperator()) { // ++ or --
            e
                    .or()
                    .equals() // =
                    .assignId(); // i = x or i = x + a or i = func()
        }
    }

    private static void ifStatement() throws Exception {
        new Expression()
                .ifPart()
                .lparen()
                .relExp()
                .rparen()
                .lbracked()
                .block()
                .rbracked("ENDIF");
    }

    private static void elseStatement() throws Exception {
        new Expression()
                .elsePart()
                .lbracked()
                .block()
                .rbracked("ENDIF");
    }

    private static void whileStatement() throws Exception {
        new Expression()
                .whilePart()
                .lparen()
                .relExp()
                .rparen()
                .lbracked()
                .block()
                .rbracked("ENDW");
    }

    private static void variableStatement() throws Exception {
        new Expression()
                .type()
                .id()
                .insertVariable();
    }

    private static void returnStatement() throws Exception {
        new Expression().returnPart();
    }
}
