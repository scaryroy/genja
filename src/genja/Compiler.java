package genja;

import genja.transform.Generator;
import genja.transform.GeneratorTransform;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.DumpVisitor;
import japa.parser.ast.visitor.GenericVisitorAdapter;

public class Compiler extends GenericVisitorAdapter<Void, Void> {

    @Override
    public Void visit(MethodDeclaration n, Void arg) {
        if (!n.isGenerator()) {
            super.visit(n, arg);
        }

        Generator gen = new Generator();
        GeneratorTransform gt = new GeneratorTransform();
        n.accept(gt, gen);
        
        System.out.println(gen.generate().get(0).toString());
        
        return null;
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = JavaParser.parse(System.in);
        DumpVisitor d = new DumpVisitor();
        cu.accept(d, null);
        cu.accept(new Compiler(), null);
        System.out.println(d.getSource());
    }
}
