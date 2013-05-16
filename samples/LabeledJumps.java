import java.util.Iterator;

public class LabeledJumps {

    public static genja.rt.Generator<Integer> makeStupidLoop() {
        return new genja.rt.Generator<Integer>() {

            protected boolean moveNext() {
                for (; ; ) switch($state) {
                    case 0:
                    case 1:
                    case 2:
                        if (!true) if (true) {
                            $state = 3;
                            break;
                        }
                        if (true) {
                            $state = 4;
                            break;
                        }
                    case 3:
                        if (true) {
                            $state = 6;
                            break;
                        }
                        if (true) {
                            $state = 4;
                            break;
                        }
                    case 4:
                        $state = 5;
                        $current = 1;
                        return true;
                    case 5:
                        if (true) {
                            $state = 1;
                            break;
                        }
                        if (true) {
                            $state = 2;
                            break;
                        }
                    case 6:
                    case 7:
                    case 8:
                    case -1:
                        $state = -1;
                        return false;
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        Iterator<Integer> s = makeStupidLoop();
        for (int i = 0; i < 10; ++i) {
            System.out.println(s.next());
        }
    }
}

