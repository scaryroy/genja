public class Range {

    public static genja.rt.Generator<Integer> makeRange(final int start, final int stop, final int step) {
        return new genja.rt.Generator<Integer>() {

            private int s0$i = start;

            protected boolean moveNext() {
                for (; ; ) switch($state) {
                    case 0:
                        s0$i = start;
                    case 1:
                        if (!(s0$i < stop)) if (true) {
                            $state = 2;
                            break;
                        }
                        if (true) {
                            $state = 3;
                            break;
                        }
                    case 2:
                        if (true) {
                            $state = 5;
                            break;
                        }
                        if (true) {
                            $state = 3;
                            break;
                        }
                    case 3:
                        $state = 4;
                        $current = s0$i;
                        return true;
                    case 4:
                        s0$i += step;
                        if (true) {
                            $state = 1;
                            break;
                        }
                    case 5:
                    case -1:
                        $state = -1;
                        return false;
                }
            }
        };
    }

    public static void main(String[] args) {
        Iterable<Integer> range = makeRange(1, 10, 1);
        System.out.println("Counting to 10!");
        for (int i : range) {
            System.out.println(i + "...");
            if (i == 5) {
                System.out.println("Um, what's next?");
                break;
            }
        }
        for (int i : range) {
            System.out.println(i + "...");
        }
        System.out.println("10!");
    }
}

