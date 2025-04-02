package mod.bitsnblocks.utils;

import com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.function.Supplier;

public class SimpleMaxSizedList<E> extends AbstractList<E>
{
    private final Supplier<Integer>      maxSize;
    private final LinkedList<E> delegate = Lists.newLinkedList();

    public SimpleMaxSizedList(final Supplier<Integer> maxSize)
    {
        this.maxSize = maxSize;
    }

    @Override
    public void add(final int index, final E element)
    {
        synchronized (delegate) {
            if (!delegate.contains(element))
            {
                delegate.add(index, element);
            }

            while(size() > maxSize.get()) {
                delegate.removeFirst();
            }
        }
    }

    @Override
    public E get(final int index)
    {
        synchronized (delegate) {
            return delegate.get(index);
        }
    }

    @Override
    public int size()
    {
        synchronized (delegate) {
            return delegate.size();
        }
    }

    @Override
    public E remove(int index) {
        synchronized (delegate) {
            return delegate.remove(index);
        }
    }
}
