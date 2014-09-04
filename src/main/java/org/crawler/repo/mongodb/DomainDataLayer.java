package org.crawler.repo.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.crawler.Domain;
import org.crawler.DomainMetadata;
import org.crawler.ClientDomain;
import org.crawler.PatternStorage;

/**
 *
 */
public class DomainDataLayer extends DataLayer<ClientDomain> {

    public DomainDataLayer(MongoClient mclient, String dbName, String collectionName, int bSize) {
        super(mclient, dbName, collectionName, bSize);
    }

    @Override
    public void insertOrUpdate(ClientDomain cdomain) {
        insertOrUpdate(cdomain, "base_url", cdomain.getDomain().getStartUrl());
    }

    @Override
    public void insertOrUpdate(List<ClientDomain> objects) {
        DBCollection coll = db.getCollection(collectionName);
        BulkWriteOperation bulk = coll.initializeUnorderedBulkOperation();
        for (ClientDomain cdomain : objects) {
            bulk.find(new BasicDBObject("uniqueId", cdomain.getUniqueId())).updateOne(convert(cdomain));
        }
        bulk.execute();
    }

    public List<ClientDomain> getDomainsToCrawl(int limit, long time) {
        BasicDBList orConditionList = new BasicDBList();
        orConditionList.add(new BasicDBObject("nextCrawlTime", new BasicDBObject("$lt", time)));
        orConditionList.add(new BasicDBObject("newDomain", true));
        BasicDBObject orCondition = new BasicDBObject("$or", orConditionList);
        BasicDBList andConditionList = new BasicDBList();
        andConditionList.add(orCondition);
        andConditionList.add(new BasicDBObject("isBusy", false));
        BasicDBObject andCondition = new BasicDBObject("$and", andConditionList);
        return execureQuery(andCondition, limit);
    }

    public List<ClientDomain> getDomainsByUrl(String url, int limit) {
        BasicDBObject query = new BasicDBObject("base_url", url);
        return execureQuery(query, limit);
    }

    private List<ClientDomain> execureQuery(BasicDBObject query, int limit) {
        DBCollection coll = db.getCollection(collectionName);
        try (DBCursor cursor = coll.find(query).limit(limit)) {
            List<ClientDomain> cdomains = convert(cursor);
            return cdomains;
        }
    }

    @Override
    protected List<ClientDomain> convert(DBCursor cursor) {
        List<ClientDomain> cdomains = new ArrayList<>(cursor.size());
        while (cursor.hasNext()) {
            cdomains.add(convert(cursor.next()));
        }
        return cdomains;
    }

    @Override
    protected ClientDomain convert(DBObject obj) {
        Domain d = new Domain.DomainBuilder().setCrawlPeriod(Long.parseLong(obj.get("crawlPeriod").toString()))
                .setCreationDate(Long.parseLong(obj.get("creationDate").toString()))
                .setLastCrawledDate(Long.parseLong(obj.get("lastCrawlDate").toString()))
                .setDomainMetadata(new DomainMetadata())
                .setMaxDepth(Integer.parseInt(obj.get("maxDepth").toString()))
                .build(obj.get("base_url").toString());
        BasicDBObject patternsList = (BasicDBObject) obj.get("patterns");
        Map<String, PatternStorage> parsedPatterns = new HashMap<>();
        for (String clientId : patternsList.keySet()) {
            BasicDBObject clientPatterns = (BasicDBObject) ((DBObject) patternsList).get(clientId);
            PatternStorage pstorage = new PatternStorage();
            for (String patternName : clientPatterns.keySet()) {
                Collection<String> patterns = new ArrayList<>();
                for (String pattern : (Collection<String>) clientPatterns.get(patternName)) {
                    patterns.add(pattern);
                }
                pstorage.putPatterns(patternName, patterns);
            }
            parsedPatterns.put(clientId, pstorage);
        }
        ClientDomain cdomain = new ClientDomain(d, "",
                obj.get("name").toString(), obj.get("type").toString(),
                Boolean.getBoolean(obj.get("newDomain").toString()), parsedPatterns);
        cdomain.setStartCrawlingTime(System.currentTimeMillis());
        return cdomain;
    }

    @Override
    protected DBObject convert(ClientDomain cdomain) {
        BasicDBObject patternsObject = new BasicDBObject();
        for (String clientId : cdomain.getAllPatterns().keySet()) {
            BasicDBObject clientPaterns = new BasicDBObject();
            PatternStorage pstorage = cdomain.getAllPatterns().get(clientId);
            for (String patternName : pstorage.getKeySet()) {
                BasicDBList pObject = new BasicDBList();
                for (String p : pstorage.getPatterns(patternName)) {
                    pObject.add(p);
                }
                clientPaterns.put(patternName, pObject);
            }
            patternsObject.put(clientId, clientPaterns);
        }
        BasicDBObject obj = new BasicDBObject("name", cdomain.getName())
                .append("type", cdomain.getType()).append("base_url", cdomain.getDomain().toString())
                .append("creationDate", cdomain.getDomain().getCreationDate()).append("nextCrawlTime", cdomain.getDomain().getNextCrawlTime())
                .append("lastCrawlDate", cdomain.getDomain().getLastCrawledDate()).append("crawlPeriod", cdomain.getDomain().getCrawlPeriod())
                .append("newDomain", cdomain.isNewDomain()).append("isBusy", cdomain.isBusy()).append("maxDepth", cdomain.getDomain().getMaxDepth())
                .append("patterns", patternsObject);
        return obj;
    }

    public static void main(String[] args) throws UnknownHostException {
        MongoClient mclient = new MongoClient("localhost", 27017);
        DomainDataLayer dl = new DomainDataLayer(mclient, "cdomainTest", "domains", 100);
        Map<String, PatternStorage> patterns = new HashMap<>();

        Collection<String> p1List = new ArrayList<>(Arrays.asList("1", "2", "3"));
        Collection<String> p2List = new ArrayList<>(Arrays.asList("mama", "papa"));
        Collection<String> p3List = new ArrayList<>(Arrays.asList("d1", "d2", "d3", "d4"));

        PatternStorage p1St = new PatternStorage();
        p1St.putPatterns("name", p1List);
        p1St.putPatterns("age", p2List);
        PatternStorage p2St = new PatternStorage();
        p2St.putPatterns("someStuff", p3List);

        patterns.put("clientId1", p1St);
        patterns.put("clientId2", p2St);
//        dl.insert(new ClientDomain(new Domain.DomainBuilder().setMaxDepth(4).build("http://www.kalzumeus.com"), "uniId", "type", "come", true, patterns));
//        List<ClientDomain> domains = new ArrayList<>(dsize);
//        for (int i = 0; i < dsize; i++) {
//            domains.add(new ClientDomain(new Domain.DomainBuilder().build("http://url.com" + i), "clID", "uniID", "name", "type", false));
//        }
//        dl.insert(domains);
//        for (int i = 0; i < dsize; i++) {
//            Domain d = new Domain.DomainBuilder().setLastCrawledDate(1).setCrawlPeriod(1000000).build("http://en.wikipedia.org" + i);
//            ClientDomain cd = new ClientDomain(d, "testID", "u", "wikipedia", "trade");
//            dl.insert(cd);
//        }
//        Collection<ClientDomain> dtc = dl.getDomainsToCrawl(10, System.currentTimeMillis());
//        for (ClientDomain d : dtc) {
//            System.out.println(d.getDomain().toString());
//        }
        mclient.close();
    }

}
