package com.kalamin.pp.micro.java.symtab;

public class TableData {

    private String name;
    private String kind;
    private String type;
    private int scope;
    private int numOfFuncArgs;
    private int index;

    public TableData(String name, String kind, String type, int scope, int numOfFuncArgs, int index) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.scope = scope;
        this.numOfFuncArgs = numOfFuncArgs;
        this.index = index;
    }

    public int getScope() {
        return scope;
    }

    public int getNumOfFuncArgs() {
        return numOfFuncArgs;
    }

    public void setNumOfFuncArgs(int numOfFuncArgs) {
        this.numOfFuncArgs = numOfFuncArgs;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TableData{" +
                "name='" + name + '\'' +
                ", kind='" + kind + '\'' +
                ", type='" + type + '\'' +
                ", scope=" + scope +
                ", numOfFuncArgs=" + numOfFuncArgs +
                ", index= "+index+
                '}';
    }


}
