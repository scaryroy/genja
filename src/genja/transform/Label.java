package genja.transform;

class Label {
    Label back;
    String name;
    boolean loop;
    int breakPoint;
    int continuePoint;

    public Label(String name, int continuePoint, boolean loop, Label back) {
        this.name = name;
        this.continuePoint = continuePoint;
        this.loop = loop;
        this.back = back;
    }
}
