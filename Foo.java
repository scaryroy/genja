import genja.rt.Generator;
import java.util.Iterator;

public class Foo {

    public java.util.Iterator<Integer> counter() {
        return new genja.rt.Generator<Integer>() {

            private int s0$s0$s0$s0$j = 0;

            private int s0$i = 0;

            private int s0$s0$s0$s0$s0$s0$s0$k = 0;

            protected boolean moveNext() {
                for (; ; ) switch($state) {
                    case 0:
                        s0$i = 0;
                    case 1:
                    case 2:
                        if (!(s0$i < 10)) if (true) {
                            $state = 3;
                            break;
                        }
                        if (true) {
                            $state = 4;
                            break;
                        }
                    case 3:
                        if (true) {
                            $state = 19;
                            break;
                        }
                        if (true) {
                            $state = 4;
                            break;
                        }
                    case 4:
                        s0$s0$s0$s0$j = 0;
                    case 5:
                    case 6:
                        if (!(s0$s0$s0$s0$j < 10)) if (true) {
                            $state = 7;
                            break;
                        }
                        if (true) {
                            $state = 8;
                            break;
                        }
                    case 7:
                        if (true) {
                            $state = 17;
                            break;
                        }
                        if (true) {
                            $state = 8;
                            break;
                        }
                    case 8:
                        s0$s0$s0$s0$s0$s0$s0$k = 0;
                    case 9:
                    case 10:
                        if (!(s0$s0$s0$s0$s0$s0$s0$k < 10)) if (true) {
                            $state = 11;
                            break;
                        }
                        if (true) {
                            $state = 12;
                            break;
                        }
                    case 11:
                        if (true) {
                            $state = 15;
                            break;
                        }
                        if (true) {
                            $state = 12;
                            break;
                        }
                    case 12:
                        $state = 13;
                        $current = s0$i * s0$s0$s0$s0$j * s0$s0$s0$s0$s0$s0$s0$k;
                        return true;
                    case 13:
                        ++s0$s0$s0$s0$s0$s0$s0$k;
                        if (true) {
                            $state = 9;
                            break;
                        }
                    case 14:
                    case 15:
                        ++s0$s0$s0$s0$j;
                        if (true) {
                            $state = 5;
                            break;
                        }
                    case 16:
                    case 17:
                        ++s0$i;
                        if (true) {
                            $state = 1;
                            break;
                        }
                    case 18:
                    case 19:
                        return false;
                }
            }
        };
    }
}

