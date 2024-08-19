package com.mongolucene;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bson.Document;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MongoLuceneIndexer {

    private IndexWriter writer;
    private Directory indexDirectory;
    private MongoDBConfig mongoDBConfig;

    public MongoLuceneIndexer(String indexPath, MongoDBConfig mongoDBConfig) throws IOException {
        this.indexDirectory = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(indexDirectory, config);
        this.mongoDBConfig = mongoDBConfig;
    }

    public void indexDocuments(String collectionName1) throws IOException {
        for (Document doc : mongoDBConfig.getCollection(collectionName1).find()) {
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
            luceneDoc.add(new TextField("username", doc.getString("username"), Field.Store.YES));
            luceneDoc.add(new TextField("cidr", doc.getString("cidr"), Field.Store.YES));
            luceneDoc.add(new TextField("cidripv4", doc.getString("cidripv4"), Field.Store.YES));
            luceneDoc.add(new TextField("ip", doc.getString("ip"), Field.Store.YES)); // may cause a problem
            luceneDoc.add(new TextField("ipv4", doc.getString("ipv4"), Field.Store.YES));
            luceneDoc.add(new TextField("site", doc.getString("site"), Field.Store.YES));
            luceneDoc.add(new TextField("description", doc.getString("description"), Field.Store.YES));
            luceneDoc.add(new TextField("timestamp", doc.getString("timestamp"), Field.Store.YES));
            luceneDoc.add(new StringField("id", doc.getObjectId("_id").toString(), Field.Store.YES));
            writer.updateDocument(new Term("id", doc.getObjectId("_id").toString()), luceneDoc);
        }
        writer.close();
    }

    public void searchIndex(String searchString) throws Exception {
        DirectoryReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        String escapedString = QueryParser.escape(searchString); // to escape special charaters

        // adding pre-fix queries to handle differences in indexing ips and sites
        Query prefix1 = new PrefixQuery(new Term("ip", escapedString));
        Query prefix2 = new PrefixQuery(new Term("site", escapedString));
        Query prefix3 = new PrefixQuery(new Term("ipv4", escapedString));

        // querying logic needs to be modded a bit
        Query query1 = new QueryParser("username", new StandardAnalyzer()).parse(escapedString);
        Query query2 = new QueryParser("cidr", new StandardAnalyzer()).parse(escapedString);
        Query query3 = new QueryParser("cidripv4", new StandardAnalyzer()).parse(escapedString);
        Query query4 = new QueryParser("ip", new StandardAnalyzer()).parse(escapedString);
        Query query5= new QueryParser("ipv4", new StandardAnalyzer()).parse(escapedString);
        Query query6= new QueryParser("site", new StandardAnalyzer()).parse(escapedString);

        BooleanQuery comboQuery = new BooleanQuery.Builder()
                .add(prefix1, BooleanClause.Occur.SHOULD)
                .add(prefix2, BooleanClause.Occur.SHOULD)
                .add(prefix3, BooleanClause.Occur.SHOULD)
                .add(query1, BooleanClause.Occur.SHOULD)
                .add(query6, BooleanClause.Occur.SHOULD)
                .add(query2, BooleanClause.Occur.SHOULD)
                .add(query3, BooleanClause.Occur.SHOULD)
                .add(query4, BooleanClause.Occur.SHOULD)
                .add(query5, BooleanClause.Occur.SHOULD)
                .build();

        TopDocs results = searcher.search(comboQuery, 100); // change the second argument to scale up/down results
        TotalHits count = results.totalHits;
        System.out.println ("Found " + count.toString() + " matching search\n");
        int flag = 0;
        HashMap<String, List<org.apache.lucene.document.Document>> filterMap = new HashMap<>();
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            org.apache.lucene.document.Document foundDoc = searcher.doc(scoreDoc.doc);
            System.out.println("Document ID: " + foundDoc.get("id"));
            System.out.println("cidr: " + foundDoc.get("cidr"));
            System.out.println("cidripv4: " + foundDoc.get("cidripv4"));
            System.out.println("ip: " + foundDoc.get("ip"));
            System.out.println("ipv4: " + foundDoc.get("ipv4"));
            System.out.println("username: " + foundDoc.get("username"));
            System.out.println("site: " + foundDoc.get("site"));
            System.out.println("description: " + foundDoc.get("description"));
            System.out.println("timestamp: " + foundDoc.get("timestamp"));
            System.out.println();
            String[] arr = {foundDoc.get("id"), foundDoc.get("cidr"), foundDoc.get("cidripv4"), foundDoc.get("ip"), foundDoc.get("ipv4"), foundDoc.get("username"), foundDoc.get("site"), foundDoc.get("description")};

            String keyField = foundDoc.get("cidr");
            if (!filterMap.containsKey(keyField)){
                filterMap.put(keyField, new ArrayList<>());
            }
            filterMap.get(keyField).add(foundDoc);

            if (foundDoc.get("username").equals(searchString) || foundDoc.get("cidr").equals(searchString) || foundDoc.get("cidripv4").equals(searchString) || foundDoc.get("ip").equals(searchString) || foundDoc.get("ipv4").equals(searchString) || foundDoc.get("site").equals(searchString)) {
                flag+=1;
            }
        }
        if (flag>0){System.out.println(flag + "/" + count.toString() + " are perfect matches");}
        else{System.out.println("No Perfect Match. Similar Results have been displayed");}
        System.out.println ("--------------------------------END--------------------------------------");

        //Testing the map (optional map hence commented out)
//        System.out.println ("Testing the map which stores all the information sorted by cidr values");
//        for (String key : filterMap.keySet()){
//            List<org.apache.lucene.document.Document> hashKey = filterMap.get(key);
//            System.out.println (hashKey.get(0)); // just printing the first element
//        }

        reader.close();
    }
}