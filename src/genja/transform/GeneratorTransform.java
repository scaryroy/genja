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
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.visitor.GenericVisitorAdapter;

public class GeneratorTransform extends GenericVisitorAdapter<MethodDeclaration, Void> {
    @Override
    public MethodDeclaration visit(MethodDeclaration n, Void arg) {
        if (!n.isGenerator()) {
            throw new UnsupportedOperationException("cannot transform non-generator");
        }

        // Desugar ALL the loops!
        n.accept(new LoopDesugarTransform(), null);

        Generator g = new Generator();
        Map<String, TypedVariableDeclarator> vars = new HashMap<String, TypedVariableDeclarator>();

        // Mangle all our scope variables.
        n.accept(new ScopeMangleTransform(), vars);

        // Generate the body.
        n.accept(new LinearizeTransform(), g);

        List<BodyDeclaration> generatorBody = new ArrayList<BodyDeclaration>();

        // Add the fields to the generator body.
        for (Map.Entry<String, TypedVariableDeclarator> kv : vars.entrySet()) {
            VariableDeclarator decl = new VariableDeclarator(new VariableDeclaratorId(kv.getKey()), kv.getValue().variable.getInit());
            generatorBody.add(new FieldDeclaration(ModifierSet.PRIVATE, kv.getValue().type, decl));
        }

        // Add the moveNext method to the generator body.
        generatorBody.add(new MethodDeclaration(0, 0, 0, 0, null, ModifierSet.PROTECTED,
                                                null, null, new PrimitiveType(Primitive.Boolean),
                                                "moveNext", null, 0, null, false,
                                                new BlockStmt(g.generate())));

        ObjectCreationExpr o = new ObjectCreationExpr(0, 0, 0, 0, null, makeRtGeneratorType(n.getType()), null, null, generatorBody);
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
                makeIteratorType(n.getType()),
                n.getName(),
                n.getParameters(),
                n.getArrayCount(),
                n.getThrows(),
                false,
                new BlockStmt(methodBody));

        return n2;
    }

    private ClassOrInterfaceType makeIteratorType(Type type) {
        List<Type> typeArgs = new ArrayList<Type>();
        typeArgs.add(type);
        return new ClassOrInterfaceType(0, 0, 0, 0, new ClassOrInterfaceType(new ClassOrInterfaceType("java"), "util"), "Iterator", typeArgs);
    }

    private ClassOrInterfaceType makeRtGeneratorType(Type type) {
        List<Type> typeArgs = new ArrayList<Type>();
        typeArgs.add(type);
        return new ClassOrInterfaceType(0, 0, 0, 0, new ClassOrInterfaceType(new ClassOrInterfaceType("genja"), "rt"), "Generator", typeArgs);
    }
}
