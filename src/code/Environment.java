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

        if (enclosing != null) return enclosing.get(name);

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

//            for(String dt:values.keySet()){
//                String key =  dt.toString();
//                String val = values.get(dt).toString();
//                System.out.println(key + " " + val);
//            }

//            System.out.println("vardata" + values.get(name.lexeme));

//            String dt = getdataType(name); //problem
//            boolean matchAssigned = false;
//            String valueDT;
//
//            switch (dt){
//                case "INT":
//                    if (value instanceof Integer)
//                        matchAssigned=true;
//                    break;
//                case "CHAR":
//                    if (value instanceof Character)
//                        matchAssigned=true;
//                    break;
//                case "BOOL":
//                    if (value instanceof Boolean)
//                        matchAssigned=true;
//                    break;
//                case "FLOAT":
//                    if (value instanceof Float)
//                        matchAssigned=true;
//                    break;
//            }
//
//            if(matchAssigned) {
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

    void define(String name, Object value) {
        values.put(name, value);
//        System.out.println(values.get(name) + "added to values hashmap");
    }

    void defineDataType(String name, String dataType) {
        dataTypes.put(name, dataType); //does get added
//        System.out.println(dataTypes.get(name) + "added to datatypes hashmap");
//        for(String dt:dataTypes.keySet()){
//            String key =  dt.toString();
//            String value = dataTypes.get(dt).toString();
//            System.out.println(key + " " + value);
//        }
//        return dataType;
    }

}
