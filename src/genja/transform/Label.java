package genja.transform;

class Label {
    Label back;
    String name;
    int breakPoint;
    int continuePoint;

    public Label(String name, int breakPoint, int continuePoint, Label back) {
        this.name = name;
        this.breakPoint = breakPoint;
        this.continuePoint = continuePoint;
        this.back = back;
    }
}
