package com.kalamin.pp.micro.java;

import com.kalamin.pp.micro.java.codegen.x86;
import com.kalamin.pp.micro.java.parser.Parser;
import com.kalamin.pp.micro.java.scanner.TokenScanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        TokenScanner tokenScanner = new TokenScanner(new FileInputStream("program.mj"/*args[0]*/));
        tokenScanner.scanFile();

        new Parser();
        x86 asmCode = x86.getInstance();
        asmCode.writeASMFile("program");

    }
}
