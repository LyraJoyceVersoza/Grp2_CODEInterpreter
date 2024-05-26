package code;

import java.util.List;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;

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
                else if (right instanceof Float) {
                    return -(float)right;
                }
            case PLUS: 
                checkNumberOperand(expr.operator, right);
                if(right instanceof Integer) {
                    return +(int)right;
                }
                else if (right instanceof Float) {
                    return +(float)right;
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
        if (operand instanceof Integer || operand instanceof Float) return;
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
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitScanStmt(Stmt.Scan stmt) {
        return null;
    }

    @Override
    public Void visitMultiVarStmt(Stmt.MultiVar stmt) {
        return null;
    }

    // @Override
    // public Void visitVarStmt(code.Stmt.Var stmt) {
    //     Object value = null;
    //     if (stmt.initializer != null) {
    //         value = evaluate(stmt.initializer);
    //     }

    //     environment.define(stmt.name.lexeme, value);
    //     return null;
    // }

    @Override
    public Void visitIntStmt(Stmt.Int stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Integer)){
                throw new RuntimeError(stmt.name, "Instance must be an integer.");
            }
        }
        String dataType = "INT";

        environment.define(stmt.name.lexeme, value,dataType);
        return null;
    }

    @Override
    public Void visitCharStmt(Stmt.Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Character)){
                throw new RuntimeError(stmt.name, "Instance must be a character.");
            }
        }

        String dataType = "CHAR";

        environment.define(stmt.name.lexeme, value,dataType);
        return null;
    }

    @Override
    public Void visitFloatStmt(Stmt.Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Float)){
                throw new RuntimeError(stmt.name, "Instance must be a float.");
            }
        }
        String dataType = "FLOAT";

        environment.define(stmt.name.lexeme, value, dataType);
        return null;
    }

    @Override
    public Void visitBoolStmt(Stmt.Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof Boolean)){
                throw new RuntimeError(stmt.name, "Instance must be a boolean.");
            }
        }

        String dataType = "BOOL";

        environment.define(stmt.name.lexeme, value,dataType);
        return null;
    }

    @Override
    public Void visitStringStmt(Stmt.String stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(!(value instanceof String)){
                throw new RuntimeError(stmt.name, "Instance must be a string.");
            }
        }
        String dataType = "STRING";

        environment.define(stmt.name.lexeme, value,dataType);
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
        if (obj instanceof Float) {
            return (double) obj;
        }
        if (obj instanceof Double) {
            return (double) obj;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
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
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() > rightVal.floatValue();
                }
               return leftVal.doubleValue() > rightVal.doubleValue();
           case GREATER_EQUAL:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() >= rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() >= rightVal.floatValue();
                }
                return leftVal.doubleValue() >= rightVal.doubleValue();
           case LESS:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                return leftVal.intValue() < rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() < rightVal.floatValue();
                }
                return leftVal.doubleValue() < rightVal.doubleValue();
           case LESS_EQUAL:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() <= rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() <= rightVal.floatValue();
                }
                return leftVal.doubleValue() <= rightVal.doubleValue();
           case MINUS:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() - rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() - rightVal.floatValue();
                }
                return leftVal.doubleValue() - rightVal.doubleValue();
           case PLUS:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() + rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() + rightVal.floatValue();
                }
                return leftVal.doubleValue() + rightVal.doubleValue();
           case SLASH:
                if (rightVal.doubleValue() == 0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() / rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() / rightVal.floatValue();
                }
                return leftVal.doubleValue() / rightVal.doubleValue();
           case STAR:
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() * rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() * rightVal.floatValue();
                }
                return leftVal.doubleValue() * rightVal.doubleValue();       
            case MODULO:
                if (rightVal.doubleValue() == 0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                if(leftVal instanceof Integer && rightVal instanceof Integer){
                    return leftVal.intValue() % rightVal.intValue();
                }
                if(leftVal instanceof Float && rightVal instanceof Float){
                    return leftVal.floatValue() % rightVal.floatValue();
                }
                return leftVal.doubleValue() % rightVal.doubleValue();      
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                break;                
        }

        // Unreachable.
       return null;
    }

}
