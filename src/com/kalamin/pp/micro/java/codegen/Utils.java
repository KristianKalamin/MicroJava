package com.kalamin.pp.micro.java.codegen;

import com.kalamin.pp.micro.java.scanner.Token;

import java.util.HashMap;
import java.util.Map;

// this class is unused for now
class Utils {

    private static int registryPointer = 1;
    private static Map<String, String> regValues = new HashMap<>();

    static Map<Token, String> conditionJumps = new HashMap<>() {
        {
            put(Token.GREATER, "JG"); // jump if greater
            put(Token.GEQ, "JGE"); // jump if greater or equal
            put(Token.LESS, "JL"); // jump if less
            put(Token.LEQ, "JLE"); // jump if less or equal
            put(Token.NEQ, "JNE"); // jump if not equal
            put(Token.EQ, "JE"); // jump if equal
        }
    };

    static Map<String, String> jumps = new HashMap<>() {
        {
            put("jmp", "JMP"); // jump
            put("jz", "JZ"); // jump if zero
        }
    };

    static Map<Integer, String> reg32bit = new HashMap<>() {
        {
            //put(1, "EBX"); function return registry
            put(1, "EAX");
            put(2, "ECX");
            put(3, "EDX");
            put(4, "ESI");
            put(5, "EDI");
        }
    };


    static String getFreeRegistry(String name) throws Exception {
        String registry = reg32bit.get(registryPointer);
        if (registryPointer > 6) throw new Exception("No more free registers");
        regValues.put(reg32bit.get(registryPointer), name);
        registryPointer++;
        return registry;
    }

    static String freeRegistry() {
        String s = reg32bit.get(registryPointer);
        regValues.remove(reg32bit.get(registryPointer));
        --registryPointer;
        return s;
    }

    static String getRegistryOfParam(String returnValue) {
        return regValues.get(returnValue);
    }

}
