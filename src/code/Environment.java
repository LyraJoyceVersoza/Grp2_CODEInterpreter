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

    String getDataType(String varName) {
        String varType = dataTypes.get(varName);
        return varType;       
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {

            String varDataType = dataTypes.get(name.lexeme);

            if (!isValidType(value, varDataType)) {
                throw new RuntimeError(name, "Input must be of type " + dataTypes.get(name.lexeme));
            }

            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    private boolean isValidType(Object value, String dataType) {
        // Check if the value's type matches the expected data type
        switch (dataType) {
            case "INT":
                return value instanceof Integer;
            case "FLOAT":
                return value instanceof Double;
            case "CHAR":
                return value instanceof Character;
            case "BOOL":
                return value instanceof Boolean;
            case "STRING":
                return value instanceof String;
            default:
                return false;
        }
    }

    void define(String varName, Object value, String dataType) {
        values.put(varName, value);
        dataTypes.put(varName, dataType);
    }

}
