package code;


import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> dataTypes = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) 
            return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

//    String getdataType(Token varName) {
//
//        if (dataTypes.containsKey(varName.lexeme)) { //returns false
////            System.out.println("varname is " + varName.lexeme);
//            return dataTypes.get(varName.lexeme);
//        }
//
//        if (enclosing != null) return enclosing.getdataType(varName);
//
//        throw new RuntimeError(varName,
//                "Undefined variable '" + varName.lexeme + " aguy'.");
//    }

//    void assign(Token name, Object value) {
//        if (values.containsKey(name.lexeme)) {
//            values.put(name.lexeme, value);
//            return;
//        }
//
//        if (enclosing != null) {
//            enclosing.assign(name, value);
//            return;
//        }
//
//        throw new RuntimeError(name,
//                "Undefined variable '" + name.lexeme + "'.");
//    }
// ------> above is orig

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {

            values.put(name.lexeme, value);
            return;
//            }
//
//            throw new RuntimeError(name,
//                "Cannot assign to a variable of type" + dt);
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String varName, Object value, String dataType) {
        values.put(varName, value);
        dataTypes.put(varName, dataType);
    //    System.out.println(values.get(name) + "added to values hashmap");
    }

}
