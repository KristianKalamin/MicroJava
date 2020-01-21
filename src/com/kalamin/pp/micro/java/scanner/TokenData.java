package com.kalamin.pp.micro.java.scanner;

public class TokenData {

    private int line;
    private Token token; // enum token
    private String name;

    TokenData(int line, Token token, String name) {
        this.line = line;
        this.token = token;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenData{" +
                "line=" + line +
                ", token=" + token +
                ", name='" + name + '\'' +
                '}';
    }
}
