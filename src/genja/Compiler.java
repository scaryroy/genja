package genja;

import genja.transform.GeneratorTransform;
import genja.transform.TransformException;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.stmt.YieldStmt;
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

    @Override
    public Node visit(YieldStmt n, Void arg) {
        throw new TransformException("yield outside of generator");
    }
    
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            if (arg.equals("-ddump-desugar")) {
                CompilerSettings.dumpDesugar = true;
            }

            if (arg.equals("-ddump-jump-free")) {
                CompilerSettings.dumpJumpFree = true;
            }

            if (arg.equals("-ddump-mangle")) {
                CompilerSettings.dumpMangle = true;
            }

            if (arg.equals("-ddump-states")) {
                CompilerSettings.dumpStates = true;
            }
        }

        CompilationUnit cu = JavaParser.parse(System.in);
        cu.accept(new Compiler(), null);
        DumpVisitor d = new DumpVisitor();
        cu.accept(d, null);
        System.out.println(d.getSource());
    }
}
