package genja.rt;

import java.util.Iterator;

public abstract class Generator<T> implements Iterator<T>, Iterable<T> {
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (this.stale) {
            try {
                this.last = this.moveNext();
            } catch (RuntimeException e) {
                this.$state = -1;
                throw e;
            }
            this.stale = false;
        }
        return this.last;
    }

    @Override
    public T next() {
        if (!this.last) {
            throw new UnsupportedOperationException("end of generator");
        }
        if (this.stale) {
            this.hasNext();
        }
        this.stale = true;
        return this.$current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("cannot remove from generator");
    }

    protected abstract boolean moveNext();

    protected int $state = 0;
    protected T $current = null;

    private boolean stale = true;
    private boolean last = true;
}
