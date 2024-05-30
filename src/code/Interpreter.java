package code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    void interpret(List<code.Stmt> statements) {
        try {
            for (code.Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // if(expr.value.equals("\n")) {
        //     System.out.println();
        // }
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }


    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case NOT:
                return !isTruthy(right);
            case MINUS: 
                checkNumberOperand(expr.operator, right);
                if(right instanceof Integer) {
                    return -(int)right;
                }
                else if (right instanceof Double) {
                    return -(double)right;
                }
            case PLUS: 
                checkNumberOperand(expr.operator, right);
                if(right instanceof Integer) {
                    return +(int)right;
                }
                else if (right instanceof Double) {
                    return +(double)right;
                }
            case NEXT_LINE:
                return "\n" + stringify(right);
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Integer || operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    // private void checkNumberOperands(Token operator,
    //                                  Object left, Object right) {
    //     if (left instanceof Double && right instanceof Double) return;
    //     if (left instanceof Float && right instanceof Float) return;
    //     if (left instanceof Integer && right instanceof Integer) return;

    //     throw new RuntimeError(operator, "Operands must be numbers.");
    // }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Boolean) {
            return object.toString().toUpperCase();
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }        

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(code.Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<code.Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (code.Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(code.Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitDisplayStmt(Stmt.Display stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Void visitScanStmt(Stmt.Scan stmt) {
            // read input from the user
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            
            // splits input string by commas
            String[] values = input.split(",");

            // checks if the number of input values matches the number of variables
            if (values.length != stmt.variables.size()) {
                throw new RuntimeError(stmt.variables.get(0), "Expected " + stmt.variables.size() + " values but got " + values.length + ".");
            }

            // assign each value to the corresponding variable
            for (int i = 0; i < stmt.variables.size(); i++) {
                Token variable = stmt.variables.get(i);
                Object value = parseValue(values[i].trim());

                // retrieve the expected data type associated with the variable name from the environment
                String dataType = environment.getDataType(variable.lexeme);

                // validates data type of input value
                if (!isValidType(value, dataType)) {
                    throw new RuntimeError(variable, "Input must be of type " + dataType);
                }

                // Assign the value to the variable
                environment.assign(variable, value);
            }
        
        return null;
    }

    private Object parseValue(String value) {
        // Try to parse the value as different types
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) { }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) { }

        if (value.length() == 1) {
            return value.charAt(0);
        }

        if (value.equals("\"TRUE\"")) {
            return true;
        }

        if (value.equals("\"FALSE\"")) {
            return false;
        }

        return value; // Treat as string if no other type matches
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

    @Override
    public Void visitMultiVarStmt(Stmt.MultiVar stmt) {
        for (int i = 0; i < stmt.names.size(); i++) {
            Token name = stmt.names.get(i);
            Expr initializer = stmt.initializers.get(i);

            Object value = null;
            if (initializer != null) {
                value = evaluate(initializer);
            } else {
                value = null;
            }

            environment.assign(name, value);
        }
        return null;
    }

    @Override
    public Void visitIntStmt(Stmt.Int stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Integer)){
                throw new RuntimeError(stmt.name, "Instance must be of type INT");
            }
        }
        String dataType = "INT";

        environment.define(stmt.name, value,dataType);
        return null;
    }

    @Override
    public Void visitCharStmt(Stmt.Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Character)){
                throw new RuntimeError(stmt.name, "Instance must be of type CHAR");
            }
        }

        String dataType = "CHAR";

        environment.define(stmt.name, value,dataType);
        return null;
    }

    @Override
    public Void visitFloatStmt(Stmt.Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Double)){
                throw new RuntimeError(stmt.name, "Instance must be of type FLOAT");
            }
        }
        String dataType = "FLOAT";

        environment.define(stmt.name, value, dataType);
        return null;
    }

    @Override
    public Void visitBoolStmt(Stmt.Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Boolean)){
                throw new RuntimeError(stmt.name, "Instance must be of type BOOL");
            }
            // if (!stmt.name.lexeme.equals("\"TRUE\"") || !stmt.name.lexeme.equals("\"FALSE\"")) {
            //     throw new RuntimeError(stmt.name, "Instance must be of type BOOL");
            // }
            // System.out.println("the bool value is: " + value);
        }

        String dataType = "BOOL";

        environment.define(stmt.name, value,dataType);
        return null;
    }

    @Override
    public Void visitStringStmt(Stmt.String stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof String)){
                throw new RuntimeError(stmt.name, "Instance must be of type STRING");
            }
        }
        String dataType = "STRING";

        environment.define(stmt.name, value,dataType);
        return null;
    }

    @Override
    public Void visitWhileStmt(code.Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    private Number getNumType(Object obj) {
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return (double) obj;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        try{
            Object left = evaluate(expr.left);
            Object right = evaluate(expr.right);
            Number leftVal = getNumType(left);
            Number rightVal = getNumType(right);

            switch (expr.operator.type) {
                case NEXT_LINE:
                    return (stringify(left) + "\n" + stringify(right));
                case CONCAT:
                    return stringify(left) + stringify(right);
                case GREATER:                    
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() > rightVal.intValue();
                    }
                return leftVal.doubleValue() > rightVal.doubleValue();
                case GREATER_EQUAL:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() >= rightVal.intValue();
                    }
                    return leftVal.doubleValue() >= rightVal.doubleValue();
                case LESS:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() < rightVal.intValue();
                    }
                    return leftVal.doubleValue() < rightVal.doubleValue();
                case LESS_EQUAL:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() <= rightVal.intValue();
                    }
                    return leftVal.doubleValue() <= rightVal.doubleValue();
                case MINUS:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() - rightVal.intValue();
                    }
                    return leftVal.doubleValue() - rightVal.doubleValue();
                case PLUS:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() + rightVal.intValue();
                    }
                    return leftVal.doubleValue() + rightVal.doubleValue();
                case SLASH:
                    if (rightVal.doubleValue() == 0) {
                        throw new RuntimeError(expr.operator, "Division by zero.");
                    }
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() / rightVal.intValue();
                    }
                    return leftVal.doubleValue() / rightVal.doubleValue();
                case STAR:
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() * rightVal.intValue();
                    }
                    return leftVal.doubleValue() * rightVal.doubleValue();       
                case MODULO:
                    if (rightVal.doubleValue() == 0) {
                        throw new RuntimeError(expr.operator, "Division by zero.");
                    }
                    if(leftVal instanceof Integer && rightVal instanceof Integer){
                        return leftVal.intValue() % rightVal.intValue();
                    }
                    return leftVal.doubleValue() % rightVal.doubleValue();      
                case NOT_EQUAL:
                    return !isEqual(left, right);
                case EQUAL_EQUAL:
                    return isEqual(left, right);
                default:
                    break;                        
            }
        } catch (NullPointerException e) {
            throw new RuntimeError(expr.operator, "Unexpected null value encountered.");
        }

        // Unreachable.
        return null;
    }

}
