package genja.transform;

import japa.parser.ast.stmt.Statement;

class Label {
    Label back;
    String name;
    int breakPoint;
    int continuePoint;

    public Label(String name, int continuePoint, Label back) {
        this.name = name;
        this.continuePoint = continuePoint;
        this.back = back;
    }

    /**
     * Make a break for this loop. This should only be called during exitLoop, when we have both
     * the start and end states.
     */
    public Statement generateBreakJump() {
        return Generator.generateJump(this.breakPoint);
    }

    /**
     * Make a continue for this loop. This should only be called during exitLoop, when we have both
     * the start and end states.
     */
    public Statement generateContinueJump() {
        return Generator.generateJump(this.continuePoint);
    }

}
