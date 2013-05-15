package genja.rt;

import java.util.Iterator;

public abstract class Generator<T> implements Iterator<T> {
    @Override
    public boolean hasNext() {
        if (stale) {
            this.moveNext();
            stale = false;
        }
        return false;
    }

    @Override
    public T next() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("cannot remove from generator");
    }

    protected abstract boolean moveNext();

    protected int $state;
    protected T $current;

    private boolean stale = true;
}
