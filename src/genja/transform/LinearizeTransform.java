package genja.transform;

import genja.CompilerSettings;

import java.util.ArrayList;
import java.util.List;

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
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
import japa.parser.ast.visitor.VoidVisitor;

/**
 * This transformation does the bulk of the work -- it linearizes control flow
 * into a state machine (only where necessary).
 */
class LinearizeTransform implements VoidVisitor<Generator> {
    /**
     * Transform a method declaration.
     */
    @Override
    public void visit(MethodDeclaration n, Generator s) {
        if (!n.isGenerator()) {
            throw new TransformException("cannot transform non-generator");
        }

        // Annotate ALL the nodes!
        n.accept(new NodeAnnotator(), null);

        // Transform all the eligible body nodes.
        n.getBody().accept(this, s);
        
        // Create a trap state that the generator can never escape.
        s.states.add(new SwitchEntryStmt(new IntegerLiteralExpr("-1"), new ArrayList<Statement>()));
        s.addStatement(Generator.generateDeferredJump(-1));
        s.addStatement(new ReturnStmt(new BooleanLiteralExpr(false)));

        // Create an exceptional trap state.
        s.states.add(new SwitchEntryStmt(new IntegerLiteralExpr("-2"), new ArrayList<Statement>()));
        s.addStatement(new ThrowStmt(Generator.EXCEPTION_VAR));

        
        for (SwitchEntryStmt st : s.states) {
            if (CompilerSettings.dumpJumpFree) {
                System.err.print(st);
            }

            // Transform labeled breaks.
            st.accept(new LabeledJumpTransform(), s);
        }

        if (CompilerSettings.dumpStates) {
            for (SwitchEntryStmt st : s.states) {
                System.err.print(st);
            }
        }
    }

    /**
     * Transform a yield.
     */
    @Override
    public void visit(YieldStmt n, Generator s) {
        // Remember the state to add the deferred jump to.
        SwitchEntryStmt entryStateNode = s.getCurrentStateNode();

        s.newState();

        List<Statement> stmts = entryStateNode.getStmts();
        stmts.add(Generator.generateDeferredJump(s.getCurrentState()));
        stmts.add(new ExpressionStmt(new AssignExpr(Generator.CURRENT_VAR,
                                                    n.getExpr(),
                                                    AssignExpr.Operator.assign)));
        stmts.add(new ReturnStmt(new BooleanLiteralExpr(true)));
    }

    @Override
    public void visit(CompilationUnit n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(PackageDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ImportDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(TypeParameter n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(LineComment n, Generator arg) {
        return;
    }

    @Override
    public void visit(BlockComment n, Generator arg) {
        return;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(EnumDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(EmptyTypeDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(EnumConstantDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(AnnotationDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(FieldDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(VariableDeclarator n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(VariableDeclaratorId n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ConstructorDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(Parameter n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(EmptyMemberDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(InitializerDeclaration n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(JavadocComment n, Generator arg) {
        return;
    }

    @Override
    public void visit(ClassOrInterfaceType n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(PrimitiveType n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ReferenceType n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(VoidType n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(WildcardType n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ArrayAccessExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ArrayCreationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ArrayInitializerExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(AssignExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(BinaryExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(CastExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ClassExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ConditionalExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(EnclosedExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(FieldAccessExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(InstanceOfExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(StringLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(IntegerLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(LongLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(IntegerLiteralMinValueExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(LongLiteralMinValueExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(CharLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(DoubleLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(BooleanLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(NullLiteralExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(MethodCallExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(NameExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ObjectCreationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(QualifiedNameExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ThisExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(SuperExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(UnaryExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(VariableDeclarationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(NormalAnnotationExpr n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(MemberValuePair n, Generator arg) {
        throw new TransformException("don't know how to linearize");
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Generator arg) {
        arg.addStatement(n);
    }

    @Override
    public void visit(TypeDeclarationStmt n, Generator arg) {
        arg.addStatement(n);
    }

    @Override
    public void visit(AssertStmt n, Generator arg) {
        arg.addStatement(n);
    }

    @Override
    public void visit(EmptyStmt n, Generator arg) {
        arg.addStatement(n);
    }

    @Override
    public void visit(ExpressionStmt n, Generator arg) {
        arg.addStatement(n);
    }

    @Override
    public void visit(SwitchStmt n, Generator arg) {
        // Create a list of entries to populate later.
        List<SwitchEntryStmt> entries = new ArrayList<SwitchEntryStmt>();

        // Switch jump.
        arg.addStatement(new SwitchStmt(n.getSelector(), entries));

        SwitchEntryStmt entryStateNode = arg.getCurrentStateNode();
        
        boolean hasDefault = false;

        // Generate all the switch cases.
        arg.enterSwitch();
        for (SwitchEntryStmt case_ : n.getEntries()) {
            if (case_.getLabel() == null) {
                hasDefault = true;
            }

            arg.newState();

            // Generate the body of the linearized switch block.
            List<Statement> stmts = new ArrayList<Statement>();

            stmts.add(Generator.generateJump(arg.getCurrentState()));
            SwitchEntryStmt entry = new SwitchEntryStmt(case_.getLabel(), stmts);
            entries.add(entry);

            // Generate the actual body.
            case_.accept(this, arg);
        }
        arg.exitLabel();

        // If no default entry was found, we make one that just jumps to the
        // block after the switch.
        if (!hasDefault) {
            List<Statement> stmts = new ArrayList<Statement>();
            stmts.add(Generator.generateJump(arg.getCurrentState()));
            SwitchEntryStmt entry = new SwitchEntryStmt(null, stmts);
            entries.add(entry);
        }
        
        entryStateNode.getStmts().add(new BreakStmt());
    }

    @Override
    public void visit(SwitchEntryStmt n, Generator arg) {
        if (n.getStmts() == null) return;
        for (Statement stmt : n.getStmts()) {
           stmt.accept(this, arg);
       }
    }

    @Override
    public void visit(BreakStmt n, Generator arg) {
        if (n.getId() == null) {
            arg.addStatement(new BreakStmt(arg.label.name));
            return;
        }
        if (!arg.canBreakTo(n.getId())) {
            throw new TransformException("can't break to: " + n.getId());
        }
        arg.addStatement(n);
    }

    @Override
    public void visit(ReturnStmt n, Generator arg) {
        if (n.getExpr() != null) {
            throw new TransformException("don't know how to linearize");
        }

        arg.addStatement(Generator.generateJump(-1));
    }

    @Override
    public void visit(ContinueStmt n, Generator arg) {
        if (n.getId() == null) {
            arg.addStatement(new ContinueStmt(arg.label.name));
            return;
        }
        if (!arg.canContinueTo(n.getId())) {
            throw new TransformException("can't continue to: " + n.getId());
        }
        arg.addStatement(n);
    }

    @Override
    public void visit(DoStmt n, Generator arg) {
        throw new TransformException("loop not desugared");
    }

    @Override
    public void visit(ForeachStmt n, Generator arg) {
        throw new TransformException("loop not desugared");
    }

    @Override
    public void visit(ThrowStmt n, Generator arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SynchronizedStmt n, Generator arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(TryStmt n, Generator arg) {
        int oldTryState = arg.currentTryState;
        arg.currentTryState = arg.getCurrentState();

        n.getTryBlock().accept(this, arg);
        SwitchEntryStmt entryStateNode = arg.getCurrentStateNode();
        for (CatchClause c : n.getCatchs()) {
            c.accept(this, arg);
        }
        entryStateNode.getStmts().add(Generator.generateJump(arg.getCurrentState()));

        arg.currentTryState = oldTryState;
    }

    @Override
    public void visit(CatchClause n, Generator arg) {
        arg.newState();
        int catchPoint = arg.getCurrentState();
        arg.addStatement(new ExpressionStmt(new AssignExpr(new NameExpr(n.getExcept().getId().getName()), new CastExpr(n.getExcept().getType(), Generator.EXCEPTION_VAR), AssignExpr.Operator.assign)));
        n.getCatchBlock().accept(this, arg);
        arg.addExceptionHandler(n.getExcept().getType(), arg.currentTryState,
                                new ExceptionHandler(n.getExcept().getId().getName(),
                                                     catchPoint));
    }

    @Override
    public void visit(BlockStmt n, Generator s) {
        if (n.getStmts() == null) return;

        // Either add statements from the block into the transformer state, or process them because
        // we have yields in them.
        for (Statement stmt : n.getStmts()) {
            if (stmt.getData() != null && ((NodeAnnotation) stmt.getData()).needsProcessing) {
                stmt.accept(this, s);
            } else {
                s.addStatement(stmt);
            }
        }

    }

    @Override
    public void visit(LabeledStmt n, Generator s) {
        // Create a new state so we can jump back here, if necessary.
        s.newState();
        s.enterLabel(n.getLabel());

        // Lookahead -- if the loop body is a canonical loop, the label is also
        // a loop marker.
        if (n.getStmt().getClass().equals(ForStmt.class)) {
            s.label.loop = true;
        }
        n.getStmt().accept(this, s);
        s.newState();
        s.exitLabel();
    }

    @Override
    public void visit(IfStmt n, Generator s) {
        // Remember the state if should be executed in.
        SwitchEntryStmt entryStateNode = s.getCurrentStateNode();

        // Create a node for the consequent.
        s.newState();
        SwitchEntryStmt consequentNode = s.getCurrentStateNode();

        // Create the consequent jump.
        Statement consequentJump = Generator.generateJump(s.getCurrentState());
        n.getThenStmt().accept(this, s);

        // Check if we have an alternate.
        Statement alternateJump = null;
        if (n.getElseStmt() != null) {
            // We have an alternate jump, so let's make a state for it.
            s.newState();
            n.getElseStmt().accept(this, s);
            alternateJump = Generator.generateJump(s.getCurrentState());
        }

        s.newState();
        consequentNode.getStmts().add(Generator.generateJump(s.getCurrentState()));

        // Add the if into the node we remembered.
        entryStateNode.getStmts().add(new IfStmt(n.getCondition(),
                                                 consequentJump,
                                                 alternateJump == null ? null : alternateJump));

        // Add a jump at the end of the if block to skip all bodies.
        entryStateNode.getStmts().add(Generator.generateJump(s.getCurrentState()));
    }

    @Override
    public void visit(WhileStmt n, Generator s) {
        throw new TransformException("loop not desugared");
    }

    @Override
    public void visit(ForStmt n, Generator s) {
        if (n.getInit() != null || n.getCompare() != null || n.getUpdate() != null) {
            throw new TransformException("loop not desugared");
        }

        // We have an infinite loop, which is in the correct form for us to transform.
        s.enterLoop();
        s.newState();
        int startPoint = s.getCurrentState();
        n.getBody().accept(this, s);
        s.addStatement(Generator.generateJump(startPoint));
        s.exitLabel();
    }
}
