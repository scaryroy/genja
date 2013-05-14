package genja.transform;

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.SuperMemberAccessExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.stmt.YieldStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import japa.parser.ast.visitor.GenericVisitor;

/**
 * The node annotator does a preprocessing pass, writing annotations to nodes that could be useful
 * later (such as if a node has a yield).
 */
class NodeAnnotator implements GenericVisitor<Boolean, Void> {
    @Override
    public Boolean visit(Node n, Void arg) {
        throw new IllegalStateException(n.getClass().getName());
    }

    @Override
    public Boolean visit(CompilationUnit n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(PackageDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ImportDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(TypeParameter n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LineComment n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(BlockComment n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ClassOrInterfaceDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(EnumDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(EmptyTypeDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(EnumConstantDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(AnnotationDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(AnnotationMemberDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(FieldDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(VariableDeclarator n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(VariableDeclaratorId n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ConstructorDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(MethodDeclaration n, Void arg) {
        boolean hasYield = false;

        if (n.getBody() != null) {
            hasYield = n.getBody().accept(this, arg);
        }

        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;
    }

    @Override
    public Boolean visit(Parameter n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(EmptyMemberDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(InitializerDeclaration n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(JavadocComment n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ClassOrInterfaceType n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(PrimitiveType n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ReferenceType n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(VoidType n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(WildcardType n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayAccessExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayCreationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayInitializerExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(AssignExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(BinaryExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(CastExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ClassExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ConditionalExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(EnclosedExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(FieldAccessExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(InstanceOfExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(StringLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(IntegerLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LongLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(IntegerLiteralMinValueExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LongLiteralMinValueExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(CharLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(DoubleLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(BooleanLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(NullLiteralExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(MethodCallExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(NameExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ObjectCreationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(QualifiedNameExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(SuperMemberAccessExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ThisExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(SuperExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(UnaryExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(VariableDeclarationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(MarkerAnnotationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(SingleMemberAnnotationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(NormalAnnotationExpr n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(MemberValuePair n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ExplicitConstructorInvocationStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(TypeDeclarationStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(AssertStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(BlockStmt n, Void arg) {
        boolean hasYield = false;

        if (n.getStmts() != null) {
            for (Statement s : n.getStmts()) {
                hasYield = s.accept(this, arg) || hasYield;
            }
        }
        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;

    }

    @Override
    public Boolean visit(LabeledStmt n, Void arg) {
        if (n.getStmt() != null) {
            if (n.getStmt().accept(this, arg)) {
                NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
                n.setData(nodeAnnotation);
            }
        }

        return false;
    }

    @Override
    public Boolean visit(EmptyStmt n, Void arg) {
         return false;
   }

    @Override
    public Boolean visit(ExpressionStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(SwitchStmt n, Void arg) {
        boolean hasYield = false;

        n.getSelector().accept(this, arg);
        if (n.getEntries() != null) {
            for (SwitchEntryStmt e : n.getEntries()) {
                hasYield = e.accept(this, arg) || hasYield;
            }
        }
        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;
    }

    @Override
    public Boolean visit(SwitchEntryStmt n, Void arg) {
        boolean hasYield = false;

        if (n.getStmts() != null) {
            for (Statement s : n.getStmts()) {
                hasYield = s.accept(this, arg) || hasYield;
            }
        }
        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;
    }

    @Override
    public Boolean visit(BreakStmt n, Void arg) {
        NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
        n.setData(nodeAnnotation);
        return true;
    }

    @Override
    public Boolean visit(ReturnStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(IfStmt n, Void arg) {
        boolean hasYield = n.getThenStmt().accept(this, arg);

        hasYield = (n.getElseStmt() != null && n.getElseStmt().accept(this, arg)) || hasYield;
        
        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;
    }

    @Override
    public Boolean visit(WhileStmt n, Void arg) {
        if (n.getBody().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ContinueStmt n, Void arg) {
        NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
        n.setData(nodeAnnotation);
        return true;
    }

    @Override
    public Boolean visit(DoStmt n, Void arg) {
        if (n.getBody().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ForeachStmt n, Void arg) {
        if (n.getBody().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ForStmt n, Void arg) {
        if (n.getBody().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ThrowStmt n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(SynchronizedStmt n, Void arg) {
        if (n.getBlock().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(TryStmt n, Void arg) {
        boolean hasYield = n.getTryBlock().accept(this, arg);

        if (n.getCatchs() != null) {
            for (CatchClause c : n.getCatchs()) {
                hasYield = c.accept(this, arg) || hasYield;
            }
        }

        if (n.getFinallyBlock() != null) {
            hasYield = n.getFinallyBlock().accept(this, arg) || hasYield;
        }

        if (hasYield) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
        }
        return hasYield;
    }

    @Override
    public Boolean visit(CatchClause n, Void arg) {
        if (n.getCatchBlock().accept(this, arg)) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
            n.setData(nodeAnnotation);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(YieldStmt n, Void arg) {
        NodeAnnotation nodeAnnotation = new NodeAnnotation(true);
        n.setData(nodeAnnotation);
        return true;
    }
}
