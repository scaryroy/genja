package genja;

import java.util.ArrayList;
import java.util.List;

import genja.transform.GeneratorTransform;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.DumpVisitor;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

public class Compiler extends ModifierVisitorAdapter<Void> {
    @Override
    public Node visit(CompilationUnit n, Void arg) {
        super.visit(n, arg);
        if (n.getImports() == null) {
            n.setImports(new ArrayList<ImportDeclaration>());
        }
        List<ImportDeclaration> imports = n.getImports();
        boolean hasGenjaRt = false;
        boolean hasJavaUtil = false;

        for (ImportDeclaration decl : imports) {
            if (decl.getName().toString().equals("genja.rt.Generator")) {
                hasGenjaRt = true;
                continue;
            }
            if (decl.getName().toString().equals("java.util.Iterator")) {
                hasJavaUtil = true;
                continue;
            }
        }
        
        if (!hasGenjaRt) {
            imports.add(new ImportDeclaration(new NameExpr("genja.rt.Generator"), false, false));
        }
        if (!hasJavaUtil) {
            imports.add(new ImportDeclaration(new NameExpr("java.util.Iterator"), false, false));
        }
        return n;
    }

    @Override
    public Node visit(MethodDeclaration n, Void arg) {
        if (!n.isGenerator()) {
            return super.visit(n, arg);
        }
        return n.accept(new GeneratorTransform(), null);
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = JavaParser.parse(System.in);
        cu.accept(new Compiler(), null);
        DumpVisitor d = new DumpVisitor();
        cu.accept(d, null);
        System.out.println(d.getSource());
    }
}
