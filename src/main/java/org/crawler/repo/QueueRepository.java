package org.crawler.repo;

/**
 *
 * @param <T>
 */
public interface QueueRepository<T> {

    T getNext();

    boolean insert(T object);

    int getSize();

    void clear();

}
