package com.mongolucene;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            String indexPath = "index";
            String mongoConnectionUrl = "mongodb://localhost:27017";
            String dbName = "exampleDB";
            String collection1 = "usedips";

            MongoDBConfig mongoDBConfig = new MongoDBConfig(mongoConnectionUrl, dbName);
            MongoLuceneIndexer indexer = new MongoLuceneIndexer(indexPath, mongoDBConfig);

            Scanner reader = new Scanner (System.in);
            System.out.println("Enter query for searching: ");
            String searchingQuery = reader.nextLine();

            indexer.indexDocuments(collection1);
            indexer.searchIndex(searchingQuery);

            mongoDBConfig.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}