package com.kalamin.pp.micro.java.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class TokenScanner {

    private static final int EOF = '\u0080';
    private static final char EOL = '\r'; // new line character in windows (\r\n)
    public static LinkedList<TokenData> tokens = new LinkedList<>();
    public static int counter = 0;

    private InputStream tokenScanner;
    private int line = 1;
    private char ch = ' ';

    public TokenScanner(InputStream inputStream)   {
        this.tokenScanner = inputStream;
    }

    private void getChar() {
        try {
            int x = this.tokenScanner.read();

            if (x == '\n')
                this.line++;
            else if (x == -1)
                this.ch = EOF;
            else
                this.ch = (char)x;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scanFile() {
        getChar();
        StringBuilder buffer = new StringBuilder();
        String tokenName;
        while (this.ch != EOF) {

            while (this.ch == ' ' || this.ch == '\r')
                getChar();

            // if is literal or keyword (starts with letter)
            if (((this.ch >= 'a') && (this.ch <= 'z')) || ((this.ch >= 'A') && (this.ch <= 'Z'))) {
                while ((this.ch >= 'a') && (this.ch <= 'z')
                        || (this.ch >= 'A') && (this.ch <= 'Z')
                        || (this.ch >= '0') && (this.ch <= '9')) {
                    buffer.append(this.ch);
                    getChar();
                }

                tokenName =  buffer.toString();

                switch (buffer.toString()) {
                    case "if": tokens.add(new TokenData(this.line, Token.IF, tokenName)); break;
                    case "else": tokens.add(new TokenData(this.line, Token.ELSE, tokenName)); break;
                    case "return": tokens.add(new TokenData(this.line, Token.RETURN, tokenName)); break;
                    case "void": tokens.add(new TokenData(this.line, Token.VOID, tokenName)); break;
                    case "while": tokens.add(new TokenData(this.line, Token.WHILE, tokenName)); break;
                    case "char": tokens.add(new TokenData(this.line, Token.CHAR, tokenName)); break;
                    case "int": tokens.add(new TokenData(this.line, Token.INT, tokenName)); break;
                    case "main": tokens.add(new TokenData(this.line, Token.ID, tokenName)); break;
                    case "class": tokens.add(new TokenData(this.line, Token.CLASS, tokenName)); break;
                    case "public": tokens.add(new TokenData(this.line, Token.PUBLIC, tokenName)); break;
                    case "static": tokens.add(new TokenData(this.line, Token.STATIC, tokenName)); break;
                    case "private": tokens.add(new TokenData(this.line, Token.PRIVATE, tokenName)); break;
                    default: tokens.add(new TokenData(this.line, Token.ID, buffer.toString()));
                }
                buffer.delete(0, tokenName.length());
            }
            // for numbers (only positive numbers)
            else if ((this.ch >= '0') && (this.ch <= '9')) {
                while ((this.ch >= '0') && (this.ch <= '9')) {
                    buffer.append(this.ch);
                    getChar();
                }
                tokens.add(new TokenData(this.line, Token.NUMBER, buffer.toString()));
                buffer.delete(0, buffer.toString().length());
            }
            // for char any letter and \n only
            else if (this.ch == '\'') {
                getChar();
                while (this.ch != '\''){
                    buffer.append(this.ch);
                    getChar();
                }
                if (buffer.toString().length() > 1 && !buffer.toString().equals("\\n")) {
                    System.out.println("Syntax error on line: "+this.line+"");
                    throw new NumberFormatException();
                }
                tokens.add(new TokenData(this.line, Token.LETTER, buffer.toString()));
                buffer.delete(0, buffer.toString().length());
                getChar();
            }

            else {
                switch (this.ch) {
                    case '+': {
                        tokens.add(new TokenData(this.line, Token.PLUS, "+"));
                        getChar();
                        if (this.ch == '+') {
                            tokens.removeLast();
                            tokens.add(new TokenData(this.line, Token.INCREMENT, "++"));
                            getChar();
                        }
                    } break;
                    case '-': {
                        tokens.add(new TokenData(this.line, Token.MINUS, "-"));
                        getChar();
                        if (this.ch == '-') {
                            tokens.removeLast();
                            tokens.add(new TokenData(this.line, Token.DECREMENT, "--"));
                            getChar();
                        }
                    } break;
                    case ',': tokens.add(new TokenData(this.line, Token.COMMA, ",")); getChar(); break;
                    case '(': tokens.add(new TokenData(this.line, Token.LPAREN, "(")); getChar(); break;
                    case ')': tokens.add(new TokenData(this.line, Token.RPAREN, ")")); getChar(); break;
                    case '{': tokens.add(new TokenData(this.line, Token.LBRACKED, "{")); getChar(); break;
                    case '}': tokens.add(new TokenData(this.line, Token.RBRACKED, "}")); getChar(); break;
                    case '/': {
                        getChar();
                        // inline comment
                        if (this.ch == '/') {
                            while (this.ch != EOL && this.ch != EOF) {
                                getChar();
                            }
                        }
                    } break;
                    case '=': {
                        tokens.add(new TokenData(this.line, Token.ASSIGN, "="));
                        getChar();
                        if (this.ch == '=') {
                            tokens.removeLast();
                            tokens.add(new TokenData(this.line, Token.EQ, "=="));
                            getChar();
                        }
                        if (this.ch == '>'){
                            tokens.removeLast();
                            tokens.add(new TokenData(this.line, Token.GEQ, "=>"));
                            getChar();
                        }

                    } break;
                    case '!': {
                        getChar();
                        if (this.ch == '=') {
                            tokens.add(new TokenData(this.line, Token.NEQ, "!="));
                            getChar();
                        }
                        else {
                            throw new IllegalStateException();
                        }
                    } break;
                    case '<': {
                        tokens.add(new TokenData(this.line, Token.LESS, "<"));
                        getChar();
                        if (this.ch == '=') {
                            tokens.removeLast();
                            tokens.add(new TokenData(this.line, Token.LEQ, "<="));
                            getChar();
                        }
                    } break;
                    case '>': {
                        tokens.add(new TokenData(this.line, Token.GREATER, ">"));
                        getChar();
                    } break;
                    default: {
                        System.out.println("Error on line: "+this.line+", char -> '"+ch+"'");
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    public static TokenData getTokenData() {
        if (counter < tokens.size()) {
            TokenData t = tokens.get(counter);
            counter++;
            return t;
        } else {
            return null;
        }
    }

    public static void ungetToken() {
        --counter;
    }

    public static TokenData peekNextToken() {
        if (counter < tokens.size()) {
            return tokens.get(counter);
        } else {
            return null;
        }
    }
}
