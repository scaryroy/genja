package genja.transform;

import japa.parser.ast.stmt.Statement;

class LoopMarker {
    /**
     * The start point of this loop -- the point to jump to on either entry or continue.
     */
    int startState;

    /**
     * The end point of the loop -- the point to jump to break.
     */
    int endState;

    /**
     * The parent of this loop. This may be null if we aren't actually in a loop.
     */
    LoopMarker back;

    /**
     * Make a break for this loop. This should only be called during exitLoop, when we have both
     * the start and end states.
     */
    public Statement generateBreakJump() {
        return Generator.generateJump(this.endState);
    }

    /**
     * Make a continue for this loop. This should only be called during exitLoop, when we have both
     * the start and end states.
     */
    public Statement generateContinueJump() {
        return Generator.generateJump(this.startState);
    }

    public LoopMarker(int startState, LoopMarker back) {
        this.startState = startState;
        this.back = back;
    }
}
