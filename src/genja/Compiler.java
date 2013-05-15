package genja;

import genja.transform.GeneratorTransform;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.DumpVisitor;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

public class Compiler extends ModifierVisitorAdapter<Void> {
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
