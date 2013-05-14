package genja.transform;

/**
 * A scope.
 */
class BlockScope {
    /**
     * The scope number. This is used for mangling variables.
     */
    int num;

    /**
     * The parent of this block. This may be null if we're at the top.
     */
    BlockScope back;

    public BlockScope(int num, BlockScope back) {
        this.num = num;
        this.back = back;
    }
}
