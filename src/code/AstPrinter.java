package code;


class AstPrinter implements code.Expr.Visitor<String> {
    String print(code.Expr expr) {
        return expr.accept(this);
    }


    @Override
    public String visitAssignExpr(code.Expr.Assign expr) {
        return null;
    }

    @Override
    public String visitBinaryExpr(code.Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(code.Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(code.Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(code.Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitUnaryExpr(code.Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(code.Expr.Variable expr) {
        return null;
    }

    private String parenthesize(String name, code.Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (code.Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        code.Expr expression = new code.Expr.Binary(
                new code.Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new code.Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new code.Expr.Grouping(
                        new code.Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }
}

