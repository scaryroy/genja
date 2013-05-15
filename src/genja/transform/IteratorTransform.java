package genja.transform;

import genja.transform.BlockScope.TypedVariableDeclarator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.visitor.GenericVisitorAdapter;

public class IteratorTransform extends GenericVisitorAdapter<MethodDeclaration, Void> {
    static final ClassOrInterfaceType RT_GENERATOR_TYPE =
            new ClassOrInterfaceType(new ClassOrInterfaceType(new ClassOrInterfaceType("genjava"), "rt"), "Generator");

    @Override
    public MethodDeclaration visit(MethodDeclaration n, Void arg) {
        if (!n.isGenerator()) {
            throw new UnsupportedOperationException("cannot transform non-generator");
        }

        // Desugar ALL the loops!
        n.accept(new LoopDesugaringTransform(), null);

        Generator g = new Generator();
        Map<String, TypedVariableDeclarator> vars = new HashMap<String, TypedVariableDeclarator>();

        // Mangle all our scope variables.
        n.accept(new ScopeManglingTransform(), vars);

        // Generate the body.
        n.accept(new GeneratorTransform(), g);

        List<BodyDeclaration> generatorBody = new ArrayList<BodyDeclaration>();

        // Add the fields to the generator body.
        for (Map.Entry<String, TypedVariableDeclarator> kv : vars.entrySet()) {
            VariableDeclarator decl = new VariableDeclarator(new VariableDeclaratorId(kv.getKey()), kv.getValue().variable.getInit());
            generatorBody.add(new FieldDeclaration(ModifierSet.PRIVATE, kv.getValue().type, decl));
        }

        // Add the moveNext method to the generator body.
        generatorBody.add(new MethodDeclaration(0, 0, 0, 0, null, ModifierSet.PROTECTED,
                                                null, null, new VoidType() /* TODO: correct type */,
                                                "moveNext", null, 0, null, false,
                                                new BlockStmt(g.generate())));

        ObjectCreationExpr o = new ObjectCreationExpr(0, 0, 0, 0, null, RT_GENERATOR_TYPE, null, null, generatorBody);
        List<Statement> methodBody = new ArrayList<Statement>();
        methodBody.add(new ReturnStmt(o));
        MethodDeclaration n2 = new MethodDeclaration(n.getBeginLine(),
                n.getBeginColumn(),
                n.getEndLine(),
                n.getEndColumn(),
                n.getJavaDoc(),
                n.getModifiers(),
                n.getAnnotations(),
                n.getTypeParameters(),
                n.getType(),
                n.getName(),
                n.getParameters(),
                n.getArrayCount(),
                n.getThrows(),
                false,
                new BlockStmt(methodBody));

        return n2;
    }
}
