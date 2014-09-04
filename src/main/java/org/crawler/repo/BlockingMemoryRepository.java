package org.crawler.repo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @param <T>
 */
public class BlockingMemoryRepository<T> extends AbstractQueueRepository<T> implements QueueRepository<T> {

    private final BlockingQueue<T> cache;
    private final Set<Integer> objectsInCache;
    private final AtomicInteger cacheSize;

    public BlockingMemoryRepository(int capacity) {
        this.cache = new ArrayBlockingQueue<>(capacity);
        this.objectsInCache = new HashSet<>(capacity);
        this.cacheSize = new AtomicInteger(0);
    }

    @Override
    protected T getNextElement() {
        try {
            T obj = cache.take();
            if (obj != null) {
                objectsInCache.remove(obj.hashCode());
            }
            cacheSize.decrementAndGet();
            return obj;
        } catch (InterruptedException ex) {
            Logger.getLogger(BlockingMemoryRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    protected boolean insertElement(T object) {
        if (objectsInCache.contains(object.hashCode())) {
            return false;
        }
        if (cache.offer(object)) {
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
