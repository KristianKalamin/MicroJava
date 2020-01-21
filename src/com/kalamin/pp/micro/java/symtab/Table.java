package com.kalamin.pp.micro.java.symtab;

import java.util.LinkedList;

public class Table {
    private static LinkedList<TableData> table = new LinkedList<>();

    public static int insertIntoSymTab(String name, String kind, String type, int scope, int numOfFuncArgs, int index) {
        table.add(new TableData(name, kind, type, scope, numOfFuncArgs, index));
        return table.getLast().getIndex();
    }

    // returns index of row
    public static int searchSymTabByKind(String kind, String id, int currentMethod) {
        for (int i = table.size() - 1; i >= lookForTablePos(currentMethod); i--) {
            if (table.get(i).getKind().equals(kind) && table.get(i).getName().equals(id)) return table.get(i).getIndex();
        }
        return -1;
    }

    public static int searchSymTabByKind(String kind, String id) {
        for (Object row : table)
            if (((TableData) row).getKind().equals(kind) && ((TableData) row).getName().equals(id)) return ((TableData) row).getIndex();

        return -1;
    }

    // returns index of row
    public static int searchSymTabByName(String name) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(name)) return ((TableData) row).getIndex();
        }

        return -1;
    }

    public static int getVariableScope(String name) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(name)) return ((TableData) row).getScope();
        }
        return -1;
    }

    public static String getVariable(String variable, int currentScope, int currentMethod) {
        if (searchSymTabByKind("LIT", variable, currentMethod) != -1)
            return variable;
        else {
            int idx = searchSymTabByKind("PAR", variable, currentMethod);
            if (idx != -1) {
                return "DWORD PTR[ESP + "+4*getParamNum(idx)+"]";
            }else
                for (int i = table.size() - 1; i > 0; i--) {
                    if ((table.get(i).getName().equals(variable)) && (table.get(i).getScope() <= currentScope))
                        return table.get(i).getName()+table.get(i).getScope();
                }
        }
        return null;
    }

    public static String lookFor(String name) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(name)) return ((TableData) row).getName();
        }
        return null;
    }

    private static int lookForTablePos(int currentMethod) {
        for (int i = table.size() - 1; i > -1; i--) {
            if (table.get(i).getIndex() == currentMethod) {
                return i - getMethodParamsCount(currentMethod);
            }
        }
        return -1;
    }

    //returns type
    public static String getType(String name) {
        for (int i = table.size() - 1; i > -1; i--) {
            if (table.get(i).getName().equals(name)) return table.get(i).getType();
        }
        return null;
    }

    public static String getType(int idx) {
        for (int i = table.size() - 1; i > -1; i--) {
            if (table.get(i).getIndex() == idx) return table.get(i).getType();
        }
        return null;
    }

    public static String getKind(String id) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(id)) return ((TableData) row).getKind();
        }
        return null;
    }

    public static int getParamNum(int idx) {
        for (Object row : table) {
            if (((TableData) row).getIndex() == idx) return ((TableData) row).getNumOfFuncArgs();
        }
        return -1;
    }

    public static int getMethodParamsCount(int funIdx) {
        for (Object row : table) {
            if (((TableData) row).getIndex() == funIdx) return ((TableData) row).getNumOfFuncArgs();
        }
        return 0;
    }

    public static int getMethodParamsCount(String funName) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(funName)) return ((TableData) row).getNumOfFuncArgs();
        }
        return 0;
    }

    // returns name/kind of row
    public static String searchSymTabByIndex(int index, String nameOrKind) {
        if (nameOrKind.equals("name")) {
            for (Object row : table) {
                if (((TableData) row).getIndex() == index) return ((TableData) row).getName();
            }
        }

        if (nameOrKind.equals("kind")) {
            for (Object row : table) {
                if (((TableData) row).getIndex() == index) return ((TableData) row).getKind();
            }
        }
        return null;
    }

    public static void printSymTab() {
        for (TableData tableData : table) {
            System.out.println(tableData);
        }
    }

    public static void deleteSymTab() {
        table.clear();
    }

    public static void deleteMethodData(int index) {
        table.removeIf(tableData -> tableData.getIndex() > index);
    }

    public static int getMethodTableIndex(String funcName) {
        for (Object row : table) {
            if (((TableData) row).getName().equals(funcName)) return ((TableData) row).getIndex();
        }
        return 0;
    }

    public static void deleteVariablesWithCurrentScope(int scope) {
        table.removeIf(tableData -> tableData.getScope() == scope);
    }
}
