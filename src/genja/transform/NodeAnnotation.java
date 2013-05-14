package genja.transform;

class NodeAnnotation {
    /**
     * The node has a yield somewhere in its children (or is a yield itself).
     */
    boolean hasYield;

    public NodeAnnotation(boolean hasYield) {
        this.hasYield = hasYield;
    }
}