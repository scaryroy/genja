package genja.rt;

import java.util.Iterator;

public abstract class Generator<T> implements Iterator<T> {
    @Override
    public boolean hasNext() {
        if (this.stale) {
            this.last = this.moveNext();
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

    protected int $state;
    protected T $current;

    private boolean stale = true;
    private boolean last = true;
}
