package org.crawler.repo;

/**
 *
 * @param <T>
 */
public abstract class AbstractQueueRepository<T> implements QueueRepository<T> {

    protected final Object lock = new Object();

    protected abstract T getNextElement();

    protected abstract boolean insertElement(T object);

    @Override
    public T getNext() {
        T domain;
        synchronized (lock) {
            domain = getNextElement();
        }
        return domain;
    }

    @Override
    public boolean insert(T object) {
        synchronized (lock) {
            return insertElement(object);
        }
    }

}
