package genja.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.type.Type;

/**
 * A scope.
 */
class BlockScope {
    public final String SCOPE_MANGLE_PREFIX = "s%d$";

    static class TypedVariableDeclarator {
        Type type;
        VariableDeclarator variable;
        
        public TypedVariableDeclarator(Type type, VariableDeclarator variable) {
            this.type = type;
            this.variable = variable;
        }
    }

    /**
     * The scope number. This is used for mangling variables.
     */
    int num;

    /**
     * The parent of this block. This may be null if we're at the top.
     */
    BlockScope back;

    /**
     * Variables in this scope.
     */
    Map<String, TypedVariableDeclarator> vars;

    public BlockScope(int num, BlockScope back) {
        this.num = num;
        this.back = back;
        this.vars = new HashMap<String, BlockScope.TypedVariableDeclarator>();
    }
    
    /**
     * Get the scope-mangled prefix for the current scope.
     */
    String getScopePrefix() {
        List<String> prefix = new ArrayList<String>();

        BlockScope c = this;
        while (c.back != null) {
            prefix.add(String.format(SCOPE_MANGLE_PREFIX, c.num));
            c = c.back;
        }

        Collections.reverse(prefix);
        StringBuilder sb = new StringBuilder();
        for (String s : prefix) {
            sb.append(s);
        }

        return sb.toString();
    }

    BlockScope findScopeForVariable(String s) {
        if (this.vars.containsKey(s)) return this;
        if (this.back == null) return null;
        return this.back.findScopeForVariable(s);
    }
}
