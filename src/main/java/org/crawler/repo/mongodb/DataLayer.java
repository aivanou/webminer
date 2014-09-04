package org.crawler.repo.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.crawler.ClientPage;

/**
 *
 * @param <T>
 */
public abstract class DataLayer<T> {

    protected final MongoClient mclient;
    protected final DB db;
    protected final String collectionName;

    protected final List<T> upsertCache;
    protected final int batchSize;
    private boolean batchAcquired = false;

    public DataLayer(MongoClient mclient, String dbName, String collectionName, int bSize) {
        this.mclient = mclient;
        this.db = mclient.getDB(dbName);
        this.collectionName = collectionName;
        this.batchSize = bSize;
        this.upsertCache = new CopyOnWriteArrayList<>();
    }

    public void insert(T obj) {
        DBCollection coll = db.getCollection(collectionName);
        DBObject dbobj = convert(obj);
        try {
            coll.insert(dbobj);
        } catch (Throwable th) {
            throw th;
        }
    }

    public void insert(List<T> domains) {
        DBCollection coll = db.getCollection(collectionName);
        BulkWriteOperation builder = coll.initializeOrderedBulkOperation();
        for (T obj : domains) {
            builder.insert(convert(obj));
        }
        BulkWriteResult res = builder.execute();
//do something with res 
    }

    protected void insertOrUpdate(T obj, String idField, String idValue) {
        DBCollection coll = db.getCollection(collectionName);
        DBObject dbobj = convert(obj);
        coll.update(new BasicDBObject(idField, idValue), dbobj, true, false);
    }

    public abstract void insertOrUpdate(T object);

    public abstract void insertOrUpdate(List<T> objects);

    public void delayedInsertOrUpdate(Collection<T> object) {
        for (T t : object) {
            delayedInsertOrUpdate(t);
        }
    }

    public void delayedInsertOrUpdate(T object) {
        List<T> updateData;

        synchronized (this) {
            upsertCache.add(object);
            if (upsertCache.size() < batchSize) {
                return;
            }
            if (batchAcquired) {
                return;
            }
            batchAcquired = true;
            updateData = new ArrayList<>(upsertCache);
            upsertCache.clear();
        }
        insertOrUpdate(updateData);
        updateData.clear();

        synchronized (this) {
            batchAcquired = false;
        }

    }

    public void clear() {
        if (!upsertCache.isEmpty()) {
            insertOrUpdate(upsertCache);
        }
        upsertCache.clear();
    }

    abstract T convert(DBObject obj);

    abstract DBObject convert(T obj);

    abstract List<T> convert(DBCursor cursor
    );

}
