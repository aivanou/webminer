package org.crawler.repo;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @param <T>
 */
public class MemoryRepository<T> extends AbstractQueueRepository<T> implements QueueRepository<T> {

    private final Queue<T> cache;
    private final Set<Integer> objectsInCache;
    private final AtomicInteger cacheSize;
    private final int capacity;

    public MemoryRepository(int capacity) {
        this.cache = new ArrayDeque<>(capacity);
        this.objectsInCache = new HashSet<>(capacity);
        this.cacheSize = new AtomicInteger(0);
        this.capacity = capacity;
    }

    @Override
    protected T getNextElement() {
        T obj = cache.poll();
        if (obj != null) {
            objectsInCache.remove(obj.hashCode());
            cacheSize.decrementAndGet();
        }
        return obj;
    }

    @Override
    protected boolean insertElement(T object) {
        if (objectsInCache.contains(object.hashCode())) {
            return false;
        }
        if (capacity >= cacheSize.get() && cache.offer(object)) {
            objectsInCache.add(object.hashCode());
            cacheSize.incrementAndGet();
            return true;
        }
        return false;
    }

    @Override
    public T getNext() {
        return getNextElement();
    }

    @Override
    public boolean insert(T object) {
        return insertElement(object);
    }

    @Override
    public int getSize() {
        return cacheSize.get();
    }

    @Override
    public void clear() {
        cache.clear();
        cacheSize.set(0);
        objectsInCache.clear();
    }

}
