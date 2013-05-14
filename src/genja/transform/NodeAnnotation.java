package genja.transform;

class NodeAnnotation {
    /**
     * The node has a yield somewhere in its children (or is a yield itself),
     * or is a break, or is a continue.
     */
    boolean needsProcessing;

    public NodeAnnotation(boolean needsProcessing) {
        this.needsProcessing = needsProcessing;
    }
}