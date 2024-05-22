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
//            case BANG:  ----> excluded
            case NOT:
                return !isTruthy(right);
            case MINUS: //BUGGED -->  doesnt work
                checkNumberOperand(expr.operator, right);
                if(right instanceof IntegerValue) {
                    int rightVal = ((Number) right).intValue();
                    System.out.println("num is" + " " + rightVal);
                    return rightVal;
                }
                else if (right instanceof FloatValue) {
                    float rightVal = ((Number) right).floatValue();
                    return rightVal;
                }
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

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        if (left instanceof Float && right instanceof Float) return;
        if (left instanceof Integer && right instanceof Integer) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

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
    public Void visitPrintStmt(code.Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(code.Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
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

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
//
// find a way to be able to make use of this method
        switch (expr.operator.type) {
//            case GREATER:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left > (double)right;
//            case GREATER_EQUAL:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left >= (double)right;
//            case LESS:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left < (double)right;
//            case LESS_EQUAL:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left <= (double)right;
//            case MINUS:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left - (double)right;
//            case PLUS:
//                if (left instanceof Double && right instanceof Double) {
//                    return (double)left + (double)right;
//                }
//
//                if (left instanceof String && right instanceof String) {
//                    return (String)left + (String)right;
//                }
//                throw new RuntimeError(expr.operator,
//                        "Operands must be two numbers or two strings.");
////                break;
//            case SLASH:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left / (double)right;
//            case STAR:
//                checkNumberOperands(expr.operator, left, right);
//                return (double)left * (double)right;
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
//        }
            default:
                // Check if either operand is a string, if so, perform string concatenation
                if (expr.operator.type == TokenType.CONCAT && (left instanceof String || right instanceof String)) {
                    return stringify(left) + stringify(right);
                }

                // Otherwise, perform arithmetic operations
                if (left instanceof Double || right instanceof Double) {
                    double l = ((Number) left).doubleValue();
                    double r = ((Number) right).doubleValue();
                    return switch (expr.operator.type) {
                        case GREATER -> l > r;
                        case GREATER_EQUAL -> l >= r;
                        case LESS -> l < r;
                        case LESS_EQUAL -> l <= r;
                        case MINUS -> l - r;
                        case PLUS -> l + r;
                        case MODULO -> l % r;
                        case SLASH -> {
                            if (r == 0) {
                                throw new RuntimeError(expr.operator, "Division by zero.");
                            }
                            yield l / r;
                        }
                        case STAR -> l * r;
                        default -> throw new RuntimeError(expr.operator, "Invalid binary operator for numbers.");
                    };
                } else if (left instanceof Float || right instanceof Float) {
                    float l = ((Number) left).floatValue();
                    float r = ((Number) right).floatValue();
                    return switch (expr.operator.type) {
                        case GREATER -> l > r;
                        case GREATER_EQUAL -> l >= r;
                        case LESS -> l < r;
                        case LESS_EQUAL -> l <= r;
                        case MINUS -> l - r;
                        case PLUS -> l + r;
                        case MODULO -> l % r;
                        case SLASH -> {
                            if (r == 0) {
                                throw new RuntimeError(expr.operator, "Division by zero.");
                            }
                            yield l / r;
                        }
                        case STAR -> l * r;
                        default -> throw new RuntimeError(expr.operator, "Invalid binary operator for numbers.");
                    };
                } else {
                    int l = ((Number) left).intValue();
                    int r = ((Number) right).intValue();
                    return switch (expr.operator.type) {
                        case GREATER -> l > r;
                        case GREATER_EQUAL -> l >= r;
                        case LESS -> l < r;
                        case LESS_EQUAL -> l <= r;
                        case MINUS -> l - r;
                        case PLUS -> l + r;
                        case MODULO -> l % r;
                        case SLASH -> {
                            if (r == 0) {
                                throw new RuntimeError(expr.operator, "Division by zero.");
                            }
                            yield l / r;
                        }
                        case STAR -> l * r;
                        default -> throw new RuntimeError(expr.operator, "Invalid binary operator for numbers.");
                    };
                }
        }

        // Unreachable.
//        return null;
    }

}
