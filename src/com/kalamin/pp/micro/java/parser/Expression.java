package com.kalamin.pp.micro.java.parser;

import com.kalamin.pp.micro.java.codegen.x86;
import com.kalamin.pp.micro.java.scanner.Token;
import com.kalamin.pp.micro.java.scanner.TokenData;

import java.util.ArrayList;
import java.util.Objects;

import static com.kalamin.pp.micro.java.parser.Statements.*;
import static com.kalamin.pp.micro.java.scanner.TokenScanner.*;
import static com.kalamin.pp.micro.java.symtab.Table.*;

class Expression {

    private x86 code = x86.getInstance();
    private TokenData type;
    private TokenData literal;
    private TokenData id;
    private static int currentFuncIndex;
    private static int index = 0;
    private static int currentVariableScope = 0;
    private TokenData tokenData;
    private static int numOfFuncArgs = 0;
    private static boolean hasIf = false;
    private static String returnValue = "0";
    private static int args = 0;
    private static ArrayList<String> argsList = new ArrayList<>();
    private boolean isStatic = false;

    Expression ifPart() throws Exception {
        checkToken("if");
        hasIf = true;
        this.code.generateIf();
        return this;
    }

    Expression elsePart() throws Exception {
        checkToken("else");
        if (!hasIf)
            throw new Exception("Else statement without If statement");
        this.code.generateElse();
        hasIf = false;
        return this;
    }

    Expression id() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (this.tokenData.getToken() == Token.ID) {
            this.id = this.tokenData;
            return this;
        } else if (this.tokenData.getToken() == Token.NUMBER || this.tokenData.getToken() == Token.LETTER)
            return this;
        else throw new Exception("Unknown identifier "+this.tokenData.getName());
    }

    Expression type() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (this.tokenData.getToken() == Token.VOID || this.tokenData.getToken() == Token.INT || this.tokenData.getToken() == Token.CHAR) {
            this.type = this.tokenData;
            return this;
        } else {
            throw new Exception("Unknown data type on line: "+this.tokenData.getLine()+ " name: "+this.tokenData.getName());
        }
    }

    // params are separated with Token.COMMA by default
    Expression recursivelyAddParams() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (this.tokenData.getToken() == Token.INT
            || this.tokenData.getToken() == Token.CHAR
            || this.tokenData.getToken() == Token.VOID)
        {
            ungetToken();
            type();
            recursivelyAddParams();
        } else if (this.tokenData.getToken() == Token.ID) {
            ungetToken();
            id();

            insertParam(this.type, this.id);
            recursivelyAddParams();
        } else if (this.tokenData.getToken() == Token.COMMA) {
            recursivelyAddParams();
        } else if (this.tokenData.getName().equals(")")) {
            ungetToken();
            return this;
        } else throw new Exception("Error in method params");

        return this;
    }

    private void insertParam(TokenData type, TokenData id) {
        numOfFuncArgs++;
        insertIntoSymTab(id.getName(), "PAR", type.getName(), -1/*method global*/, numOfFuncArgs, ++index);
    }

    Expression insertMethod() throws Exception {
        if (this.id.getName().equals("main") && !isStatic)
            throw new Exception("Main method must be static");

        if (searchSymTabByName(this.id.getName()) == -1) {
            currentFuncIndex = insertIntoSymTab(this.id.getName(), "FUN", this.type.getName(),-1, numOfFuncArgs, ++index);
            this.code.generateProcedure(this.id.getName());

            numOfFuncArgs = 0;
            return this;
        } else throw new Exception("Error method overloading is not allowed");
    }

    Expression insertVariable() throws Exception {
        if (searchSymTabByName(this.id.getName()) == -1 || getVariableScope(this.id.getName()) != currentVariableScope) {
            insertIntoSymTab(this.id.getName(), "VAR", this.type.getName(), currentVariableScope, 0, ++index);
            this.code.declareVariable(this.id.getName(), currentVariableScope);
            return this;
        } else {
            throw new Exception("Redefinition of: "+this.id.getName());
        }
    }

    private String checkVar(TokenData id) throws Exception {
        if (searchSymTabByName(id.getName()) == -1) {
            throw new Exception("'"+id.getName()+"' not declared on line: "+id.getLine());
        }
        else {
            return getType(id.getName());
        }
    }

    Expression check() throws Exception {
        if (this.id != null) {
            if (searchSymTabByName(this.id.getName()) == -1) {
                throw new Exception("Variable '"+this.id.getName()+"' not declared on line: "+this.id.getLine());
            }
        }
        return this;
    }

    private boolean check(TokenData t) throws Exception {
        if (searchSymTabByName(t.getName()) == -1)
            throw new Exception("Variable '"+t.getName()+"' not declared on line: "+t.getLine());
        return !Objects.equals(getKind(t.getName()), "LIT");
    }

    Expression methodBody() throws Exception {
        bodyStatement();
        return this;
    }

    Expression classBody() {
        method();
        return this;
    }

    Expression block() throws Exception {
        while (Objects.requireNonNull(peekNextToken()).getToken() != Token.RBRACKED)
            blockStatement(Objects.requireNonNull(peekNextToken()));
        return this;
    }

    // (
    Expression lparen() throws Exception {
       checkToken("(");
       return this;
    }

    // )
    Expression rparen() throws Exception {
       checkToken(")");
       return this;
    }

    // {
    Expression lbracked() throws Exception {
        checkToken("{");
        ++currentVariableScope;
        return this;
    }

    // }
    Expression rbracked(String endOfControlFlow) throws Exception {
        checkToken("}");
        String id = searchSymTabByIndex(currentFuncIndex, "name");
        assert id != null;

        if (endOfControlFlow.equals("ENDIF"))
            this.code.endIf();
        else if (endOfControlFlow.equals("ENDW"))
            this.code.endWhile();

        deleteVariablesWithCurrentScope(currentVariableScope);
        --currentVariableScope;
        return this;
    }

    // }
    Expression rbracked() throws Exception {
        checkToken("}");
        String id = searchSymTabByIndex(currentFuncIndex, "name");
        assert id != null;
        if (id.equals("main") && currentVariableScope == 1)
            this.code.endMain();
        deleteVariablesWithCurrentScope(currentVariableScope);
        --currentVariableScope;
        return this;
    }

    // =
    Expression equals() throws Exception {
        checkToken("=");
        return this;
    }

    private String relationalOperators() throws Exception {
        switch (Objects.requireNonNull(peekNextToken()).getToken()) {
           case GREATER: return greaterThen(); // >
           case LESS: return lessSign(); // <
           case LEQ: return lessEqual(); // <=
           case GEQ: return greaterEqual(); // >=
           case EQ: return comparisons();  // ==
           case NEQ: return notEqual();  // !=
           default: throw new Exception("Unknown relational operator");
        }
    }

    // !=
    private String notEqual() throws Exception {
        checkToken("!=");
        return "!=";
    }

    // ==
    private String comparisons() throws Exception {
        checkToken("==");
        return "==";
    }

    // <
    private String lessSign() throws Exception {
        checkToken("<");
        return "<";
    }

    // <=
    private String lessEqual() throws Exception {
        checkToken("<=");
        return "<=";
    }

    // >
    private String greaterThen() throws Exception {
        checkToken(">");
        return ">";
    }

    // >=
    private String greaterEqual() throws Exception {
        checkToken(">=");
        return ">=";
    }

    private void checkToken(String token) throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (!this.tokenData.getName().equals(token))
            throw new Exception("Expected '"+token+"' got: "+this.tokenData.getName()+" on line: "+this.tokenData.getLine());
    }

    Expression methodParams(Expression args) {
        return this;
    }

    Expression relExp() throws Exception {
        TokenData t1 = getTokenData();
        assert t1 != null;

        String rp = relationalOperators();

        literal();
        ungetToken();
        TokenData t3 = getTokenData();
        assert t3 != null;

        if (searchSymTabByName(t1.getName()) == -1) {
            throw new Exception("Variable not declared on line "+t1.getLine());
        }

        if (searchSymTabByName(Objects.requireNonNull(t3).getName()) == -1) {
            throw new Exception("Variable not declared on line "+t3.getLine());
        }

        if (!Objects.equals(getType(t1.getName()), getType(t3.getName()))) {
            throw new Exception("Invalid operands: relational operator");
        }

        this.code.relationOperation(t1.getName(), rp, t3.getName(), currentVariableScope, currentFuncIndex);
        return this;
    }

    Expression whilePart() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (this.tokenData.getName().equals("while")) {
            this.code.generateWhile();
            return this;
        }
        else {
            throw new Exception("Expected 'while' keyword");
        }
    }

    Expression assignId() throws Exception {
        this.tokenData = getTokenData(); // variable (ID)
        assert this.tokenData != null;

        if (this.tokenData.getToken() == Token.NUMBER || this.tokenData.getToken() == Token.LETTER) {
            ungetToken();
            literal();
            if (!Objects.equals(getType(this.id.getName()), getType(this.literal.getName()))) {
                throw new Exception("Incompatible types in assignment on line "+this.id.getLine());
            }
            if (this.literal.getToken() == Token.NUMBER)
                this.code.assignValue(this.id.getName(), currentVariableScope, this.literal.getName(), currentFuncIndex);
            else
                this.code.assignValue(this.id.getName(), currentVariableScope, this.literal.getName(), currentFuncIndex);
        }

        if (!Objects.equals(getType(this.id.getName()), checkVar(this.tokenData))) { // ID = ID
            throw new Exception("Incompatible types in assignment between '"+this.id.getName()+"' and '"+this.tokenData.getName()+"' on line "+this.id.getLine());
        }

        if (searchSymTabByKind("FUN", this.tokenData.getName()) != -1) {
            ungetToken();
            methodCall();
            this.code.code("\tmov eax, ebx"); // moves method return value into eax
        } else
            if (check(this.tokenData))
                this.code.assignValue(this.id.getName(), currentVariableScope, this.tokenData.getName(), currentFuncIndex);


        if (isArithmeticSign(Objects.requireNonNull(peekNextToken()).getToken()))
            arithmeticOperations();

        return this;
    }

    private boolean isArithmeticSign(Token token) {
        switch (token) {
            case PLUS: case MINUS: return true;
            default: return false;
        }
    }

    private void arithmeticOperations() throws Exception {
        TokenData sign = getTokenData(); // sign (+ -)
        TokenData t = getTokenData(); // ID (2)

        assert sign != null;
        assert t != null;
        if (t.getName().matches("[0-9]+")) {
            ungetToken();
            literal();
            this.code.arithmetic(sign.getName(), t.getName(), currentVariableScope, currentFuncIndex);
            this.code.code("\tmov "+getVariable(this.id.getName(), currentVariableScope, currentFuncIndex)+", eax");
        } else {
            checkVar(t);
            this.code.arithmetic(sign.getName(), t.getName(), currentVariableScope, currentFuncIndex);
            this.code.code("\tmov "+getVariable(this.id.getName(), currentVariableScope, currentFuncIndex)+", eax");
        }

        if (searchSymTabByKind("FUN", t.getName()) != -1) {
            methodCall();
            this.code.arithmetic(sign.getName(), t.getName(), currentVariableScope, currentFuncIndex);
            this.code.code("\tmov "+getVariable(this.id.getName(), currentVariableScope, currentFuncIndex)+", eax");
        }

        if (Objects.requireNonNull(peekNextToken()).getToken() == Token.PLUS
                || Objects.requireNonNull(peekNextToken()).getToken() == Token.MINUS) {

            this.code.arithmetic(sign.getName(), t.getName(), currentVariableScope, currentFuncIndex);
            this.code.code("\tmov "+getVariable(this.id.getName(), currentVariableScope, currentFuncIndex)+", eax");
            arithmeticOperations();
        }
    }

    Expression literal() throws Exception {
       this.tokenData = getTokenData();
       assert this.tokenData != null;
       if (this.tokenData.getToken() == Token.ID)
           return this;
       else if (this.tokenData.getToken() != Token.NUMBER && this.tokenData.getToken() != Token.LETTER && this.tokenData.getToken() != Token.ID) {
            throw new Exception("Wrong literal for declared data type on line: "+this.tokenData.getLine());
       } else {
            this.literal = this.tokenData;
            if (this.tokenData.getName().matches("\\d+")) {
                insertIntoSymTab(this.literal.getName(), "LIT", "int", currentVariableScope, 0, ++index);
            }
            else {
                this.literal.setName(Integer.toHexString((int)this.literal.getName().charAt(0)) + "h");
                insertIntoSymTab(this.literal.getName(), "LIT", "char", currentVariableScope, 0, ++index);
            }
            return this;
        }
    }

    Expression or() {
        ungetToken();
        return this;
    }

    // i++ i--
    boolean postfixOperator() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;
        if (this.tokenData.getToken() != Token.INCREMENT && this.tokenData.getToken() != Token.DECREMENT)
           return false;
        else {
            if (searchSymTabByName(this.tokenData.getName()) != -1)
                throw new Exception("Variable not declared");
            else if (Objects.equals(getType(searchSymTabByName(this.tokenData.getName())), "void"))
                throw new Exception("Can't use unary operator on void data type");
            else if (this.tokenData.getToken() == Token.NUMBER)
                throw new Exception("Can't use unary operator on literal");
            else {
                this.code.postfixOperator(this.tokenData.getToken(), this.id.getName(), currentVariableScope, currentFuncIndex);
                return true;
            }
        }
    }

    void returnPart() throws Exception {
        this.tokenData = getTokenData();
        String funName = searchSymTabByIndex(currentFuncIndex, "name");
        String funcType = getType(funName);
        String returnType;
        boolean hasReturn = false;
        if (this.tokenData.getName().equals("return") && !Objects.equals(funcType, "void")) {
            hasReturn = true;
            literal();
            or();
            id();
            check();

            if (this.literal != null) {
                returnType = getType(searchSymTabByName(this.literal.getName()));
                returnValue = this.literal.getName();
            }
            else {
                if (searchSymTabByKind("FUN", this.id.getName()) != -1)
                    throw new Exception("Recursion is not allowed");
                else {
                    returnType = getType(searchSymTabByName(this.id.getName()));
                    returnValue = this.id.getName();
                }
            }

            if (!Objects.equals(funcType, returnType))
                throw new Exception("Wrong return value");

        }
        if (!hasReturn && Objects.equals(funcType, "void")) {
            ungetToken();
            this.code.retVoid(funName);
            return;
        }        if (!hasReturn && !Objects.equals(funcType, "void"))
            throw new Exception("Method doesn't have return");
        else {
            // delete table data for ending Method
            this.code.ret(funName, returnValue, currentVariableScope);
            deleteMethodData(currentFuncIndex);
        }

    }

    Expression checkArgs() throws Exception {
        this.tokenData = getTokenData();
        assert this.tokenData != null;

        while (this.tokenData.getToken() != Token.RPAREN) {
            if (this.tokenData.getToken() == Token.COMMA) {
                this.tokenData = getTokenData();
                continue;
            }
            if (this.tokenData.getToken() == Token.NUMBER ||
                this.tokenData.getToken() == Token.LETTER) {
                ungetToken();
                literal();
                argsList.add(this.literal.getName());
                args++;
            }
            else {
                int row = searchSymTabByName(this.tokenData.getName());
                args++;
                argsList.add(this.tokenData.getName());
                if (row == -1)
                    throw new Exception("Not declared");
            }

            this.tokenData = getTokenData();
        }

        ungetToken();
        return this;
    }

    void callFunc() throws Exception {
        int idx = searchSymTabByName(this.id.getName());
        String s = searchSymTabByIndex(idx, "name");
        int methodParamsCount = getMethodParamsCount(this.id.getName());
        if (methodParamsCount != args)
            throw new Exception("Invalid method call");

        this.code.callMethod(s, argsList, currentVariableScope, currentFuncIndex);
        this.code.freeStack(methodParamsCount);
        args = 0;
        argsList.clear();
    }

    Expression classPart() throws Exception {
        checkToken("class");
        return this;
    }

    Expression className() throws Exception {
        TokenData className = getTokenData();
        assert className != null;
        if (!className.getName().matches("[a-zA-Z]+"))
            throw new Exception("Class name can't have numbers");

        return this;
    }

    Expression accessModifier() throws Exception {
        TokenData acc = getTokenData();
        assert acc != null;
        if (acc.getName().equals("public") || acc.getName().equals("private"))
            return this;
        else throw new Exception("Method must have access modifier");
    }

    Expression methodType() throws Exception {
        TokenData m = getTokenData();
        assert m != null;
        if (!m.getName().equals("static")) {
            isStatic = false;
            ungetToken();
            type();
            ungetToken();
        } else isStatic = true;
        return this;
    }
}
