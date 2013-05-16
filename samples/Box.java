public class Box {

    public static genja.rt.Generator<Void> makeBox(final int rows, final int cols) {
        return new genja.rt.Generator<Void>() {

            private int s0$s0$s0$s0$j = 0;

            private int s0$i = 0;

            protected boolean moveNext() {
                for (; ; ) switch($state) {
                    case 0:
                        s0$i = 0;
                    case 1:
                        if (!(s0$i < rows)) if (true) {
                            $state = 2;
                            break;
                        }
                        if (true) {
                            $state = 3;
                            break;
                        }
                    case 2:
                        if (true) {
                            $state = 9;
                            break;
                        }
                        if (true) {
                            $state = 3;
                            break;
                        }
                    case 3:
                        s0$s0$s0$s0$j = 0;
                    case 4:
                        if (!(s0$s0$s0$s0$j < rows)) if (true) {
                            $state = 5;
                            break;
                        }
                        if (true) {
                            $state = 6;
                            break;
                        }
                    case 5:
                        if (true) {
                            $state = 8;
                            break;
                        }
                        if (true) {
                            $state = 6;
                            break;
                        }
                    case 6:
                        System.out.printf("[%2d]", s0$i + s0$s0$s0$s0$j);
                        $state = 7;
                        $current = null;
                        return true;
                    case 7:
                        ++s0$s0$s0$s0$j;
                        if (true) {
                            $state = 4;
                            break;
                        }
                    case 8:
                        System.out.println();
                        ++s0$i;
                        if (true) {
                            $state = 1;
                            break;
                        }
                    case 9:
                    case -1:
                        $state = -1;
                        return false;
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        for (Void _ : makeBox(10, 10)) {
            Thread.sleep(100);
        }
    }
}

