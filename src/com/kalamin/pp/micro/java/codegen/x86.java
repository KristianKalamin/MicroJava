package com.kalamin.pp.micro.java.codegen;

import com.kalamin.pp.micro.java.scanner.Token;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static com.kalamin.pp.micro.java.symtab.Table.*;

public class x86 {

    private static x86 instance;
    private Charset utf8 = StandardCharsets.UTF_8;
    private static ArrayList<String> dataSection = new ArrayList<>(); // variables
    private static ArrayList<String> codeSection = new ArrayList<>();

    public static x86 getInstance() {
        if (instance == null)
            instance = new x86();

        return instance;
    }

    private x86() {
        dataSection.add(".386\n.model flat, stdcall\n.stack 4096\nExitProcess PROTO, dwExitCode: DWORD\n.data");
        codeSection.add("\n.code\n");
    }

    public void writeASMFile(String programName) {
        try {
            File file = new File(programName+".asm");
            file.delete();

            Files.write(Paths.get(programName + ".asm"), dataSection, utf8, StandardOpenOption.CREATE);
            Files.write(Paths.get(programName + ".asm"), codeSection, utf8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void declareVariable(String var, int variableScope) {
        int j = 0;
        for (String data : dataSection)
            if (data.equals("\t" + var + variableScope + " dd ?")) j++;

        if (j == 0)
            dataSection.add("\t" + var + variableScope + " dd ?");
    }

    public void assignValue(String idName, int currentVariableScope, String literalName, int currentMethod) {
        codeSection.add("\tmov eax, "+getVariable(literalName, currentVariableScope, currentMethod));
        codeSection.add("\tmov "+getVariable(idName, currentVariableScope, currentMethod)+", eax");
    }

    public void arithmetic(String operand, String var, int currentScope, int currentMethod) {
        if (operand.equals("+")) {
            codeSection.add("\tadd eax, "+getVariable(var, currentScope, currentMethod));
        }else {
            codeSection.add("\tsub eax, "+getVariable(var, currentScope, currentMethod));
        }
    }

    // for method return value (return value is in EBX register)
    public void code(String s) {
        codeSection.add(s);
    }

    public void relationOperation(String var1, String rp, String var2, int currentScope, int currentMethod) {
        String last = codeSection.get(codeSection.size() - 1);
        codeSection.set(codeSection.size() - 1,last+"("+getVariable(var1, currentScope, currentMethod)+" "+rp+" "+getVariable(var2, currentScope, currentMethod)+")");
    }

    public void callMethod(String funcName, ArrayList<String> params, int currentVariableScope, int method) {
        for (int i = params.size() - 1; i > -1; i--)
            codeSection.add("\tpush "+getVariable(params.get(i), currentVariableScope, method));

        codeSection.add("\tcall "+funcName);
    }

    public void ret(String funcName, String returnValue, int currentScope) {
        codeSection.add("\tmov ebx, "+getVariable(returnValue, currentScope, searchSymTabByName(funcName)));
        codeSection.add("\tret");
        if (funcName.equals("main"))
            codeSection.add("invoke ExitProcess, 0");
        codeSection.add(funcName+" ENDP\n");
    }


    public void retVoid(String funcName) {
        codeSection.add("\tret");
        codeSection.add(funcName+" ENDP\n");
    }

    public void endMain() {
        for (int i = codeSection.size() - 1; i > 0; i--) {
            if (codeSection.get(i).equals("\tret")) {
                codeSection.remove(i);
                break;
            }
        }
        codeSection.add("END main");
    }

    public void generateLabel(String name) {
        codeSection.add(name+":\n");
    }

    public void generateIf() {
        codeSection.add(".IF ");
    }

    public void endIf() {
        for (int i = codeSection.size() - 1; i > 0; i--) {
            if (codeSection.get(i).equals(".ENDIF\n")) {
                codeSection.remove(i);
                break;
            }
        }
        codeSection.add(".ENDIF\n");
    }

    public void generateElse() {
        codeSection.add(".ELSE");
    }

    public void generateWhile() {
        codeSection.add(".WHILE ");
    }

    public void endWhile() {
        codeSection.add(".ENDW\n");
    }

    public void postfixOperator(Token operator, String id, int currentScope, int currentMethod) {
        if (operator == Token.INCREMENT)
            codeSection.add("\tINC "+getVariable(id, currentScope, currentMethod)+"\n");
        else codeSection.add("\tDEC "+getVariable(id, currentScope, currentMethod)+"\n");
    }

    public void generateProcedure(String name) {
        codeSection.add(name+" PROC");
    }

    public void freeStack(int num) {
        for (int i = 0; i < num; i++)
            codeSection.add("\tpop ecx");
    }
}
