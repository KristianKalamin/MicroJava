package com.kalamin.pp.micro.java.parser;

import static com.kalamin.pp.micro.java.symtab.Table.*;

public class Parser {

    public Parser() {

        try {
            new Statements();
           // printSymTab();
            deleteSymTab();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
