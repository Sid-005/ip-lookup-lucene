package com.mongolucene;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConfig {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBConfig(String mongoConnectionUrl, String dbName) {
        this.mongoClient = MongoClients.create(mongoConnectionUrl);
        this.database = mongoClient.getDatabase(dbName);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
