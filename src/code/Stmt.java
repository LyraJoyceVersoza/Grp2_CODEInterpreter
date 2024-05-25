package code;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitExpressionStmt(Expression stmt);
    R visitIfStmt(If stmt);
    R visitDisplayStmt(Display stmt);
    R visitScanStmt(Scan stmt);
    R visitIntStmt(Int stmt);
    R visitCharStmt(Char stmt);
    R visitFloatStmt(Float stmt);
    R visitBoolStmt(Bool stmt);
    R visitStringStmt(String stmt);
    R visitMultiVarStmt(MultiVar stmt);
    R visitWhileStmt(While stmt);
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class Display extends Stmt {
    Display(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitDisplayStmt(this);
    }

    final Expr expression;
  }
  static class Scan extends Stmt {
    Scan(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitScanStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class Int extends Stmt {
    Int(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIntStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class Char extends Stmt {
    Char(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCharStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class Float extends Stmt {
    Float(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFloatStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class Bool extends Stmt {
    Bool(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBoolStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class String extends Stmt {
    String(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitStringStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class MultiVar extends Stmt {
    MultiVar(String type, List<Token> names, List<Expr> initializers) {
      this.type = type;
      this.names = names;
      this.initializers = initializers;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitMultiVarStmt(this);
    }

    final String type;
    final List<Token> names;
    final List<Expr> initializers;
  }
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
