public class StupidFactors {

    public static genja.rt.Generator<Integer> makeStupidFactors(final int n, final int m, final int x) {
        return new genja.rt.Generator<Integer>() {

            private int s0$s0$s0$s0$j = 0;

            private int s0$i = 0;

            protected boolean moveNext() {
                for (; ; ) switch($state) {
                    case 0:
                    case 1:
                        s0$i = 0;
                    case 2:
                    case 3:
                        if (!(s0$i < n)) if (true) {
                            $state = 4;
                            break;
                        }
                        if (true) {
                            $state = 5;
                            break;
                        }
                    case 4:
                        if (true) {
                            $state = 16;
                            break;
                        }
                        if (true) {
                            $state = 5;
                            break;
                        }
                    case 5:
                        s0$s0$s0$s0$j = 0;
                    case 6:
                    case 7:
                        if (!(s0$s0$s0$s0$j < m)) if (true) {
                            $state = 8;
                            break;
                        }
                        if (true) {
                            $state = 9;
                            break;
                        }
                    case 8:
                        if (true) {
                            $state = 14;
                            break;
                        }
                        if (true) {
                            $state = 9;
                            break;
                        }
                    case 9:
                        if (s0$i * s0$s0$s0$s0$j == x) if (true) {
                            $state = 10;
                            break;
                        }
                        if (true) {
                            $state = 11;
                            break;
                        }
                    case 10:
                        if (true) {
                            $state = -1;
                            break;
                        }
                        if (true) {
                            $state = 11;
                            break;
                        }
                    case 11:
                        $state = 12;
                        $current = s0$i * s0$s0$s0$s0$j;
                        return true;
                    case 12:
                        ++s0$s0$s0$s0$j;
                        if (true) {
                            $state = 6;
                            break;
                        }
                    case 13:
                    case 14:
                        ++s0$i;
                        if (true) {
                            $state = 2;
                            break;
                        }
                    case 15:
                    case 16:
                    case 17:
                    case -1:
                        $state = -1;
                        return false;
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        for (int x : makeStupidFactors(10, 10, 45)) {
            System.out.println(x);
        }
    }
}

