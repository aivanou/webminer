package org.crawler.repo.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.crawler.ClientPage;

/**
 *
 */
public class PageDataLayer extends DataLayer<ClientPage> {

    public PageDataLayer(MongoClient mclient, String dbName, String collectionName, int bSize) {
        super(mclient, dbName, collectionName, bSize);
    }

    @Override
    public void insertOrUpdate(ClientPage object) {
        insertOrUpdate(object, "uniqueId", object.getUniqueId());
    }

    @Override
    public void insertOrUpdate(List<ClientPage> objects) {
        DBCollection coll = db.getCollection(collectionName);
        BulkWriteOperation bulk = coll.initializeUnorderedBulkOperation();
        for (ClientPage cpage : objects) {
            bulk.find(new BasicDBObject("uniqueId", cpage.getUniqueId())).upsert().updateOne(new BasicDBObject("$set", convert(cpage)));
        }
        bulk.execute();
    }

    @Override
    ClientPage convert(DBObject obj) {
        Map<String, String> data = new HashMap<>();
        DBObject dataObject = (DBObject) obj.get("data");
        for (String key : dataObject.keySet()) {
            data.put(key, dataObject.get(key).toString());
        }
        return new ClientPage(obj.get("domain").toString(), obj.get("url").toString(), obj.get("clientId").toString(), obj.get("uniqueId").toString(), data);
    }

    @Override
    DBObject convert(ClientPage obj) {
        BasicDBObject dbObj = new BasicDBObject().append("url", obj.getUrl()).append("domain", obj.getDomain())
                .append("clientId", obj.getClientId()).append("uniqueId", obj.getUniqueId());
        BasicDBObject dataObject = new BasicDBObject();
        for (String key : obj.getData().keySet()) {
            dataObject.put(key, obj.getData().get(key));
        }
        dbObj.put("data", dataObject);
        return dbObj;
    }

    @Override
    List<ClientPage> convert(DBCursor cursor) {
        List<ClientPage> pages = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject dbObject = cursor.next();
            pages.add(convert(dbObject));
        }
        return pages;
    }

}
