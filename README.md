# ip-lookup-lucene
 A search engine powered on Apache lucene. Optimized for relevant data retrieval and output storage. (currently only supports a single collection)
 In case of naming issues, former repo name: mongo-lucene-2024

## Initial Configuration
 1. Visit Main.java
 2. Under the main method, modify the following variables to suit your implementation
    - indexPath: (String) Indicates the path where index files for searching are created and stored [[HIGHLY IMPORTANT]]
    - mongoConnectionUrl: (String) The url for the mongo db host
    - dbName: (String) Name of the database in mongodb from which information is retrieved
    - collectionName: (String) Name of the collection to consider
   
 3. Visit MongoLuceneIndexer.java
 4. Under the method indexDocuments() and searchIndex() ensure all fields exist in your collection
    - Here, you can modify the fields that you would like to consider for the search. More on this below

 Now, running the Main.java file should automatically configure all the required dependencies. If there is a naming error for repository, use the former name: mongo-lucene-2024

## Searching Behaviour
 Refer to the documentation found here - https://www.tutorialspoint.com/lucene/lucene_query_programming.htm
 Highlights the different searching operations that can be performed to further refine searches

## Storing results
 All search results that meet the scoring criteria are stored in the hashmap "filterMap". 
 Key: cidr; Value: List<org.apache.lucene.document.Document> (index file location)

 NOTE: the values can be further modified in the for loop of searchIndex. Loop = for (ScoreDoc scoreDoc : results.scoreDocs) {}

## Modifying maximum retrieved queries
 Find the line
     TopDocs results = searcher.search(comboQuery, 100);
 under the searchIndex() method of MogoLuceneIndexer.java
 
 Here, the second argument passed indicates the maximum allowed retrievals for a query. This number may be modified if you want more than 100 fields to be retrieved
 With current configuration: minimum fields = 0, maximum fields = 100

## Cleaning Lucene Index File
 Lucene Index files are permanently stored in the system memory. Tampering with the file is not adivsable as it will directly affect search outputs
 However, as time progresses some documents in the mongoDB collection may be deleted and this is not reflected in lucene.
 
 In the event of such a situation, refer to the method = forceMergeDeletes() 
 https://lucene.apache.org/core/7_3_0/core/org/apache/lucene/index/IndexWriter.html
 
 It will allow you to clean the deleted files. Any other relevant methods (such as deleting the index directory and starting the code again) will also work, but the initial build time may be high.

## Further Assistance
 For further support, visit an Apache Lucene forum for more tips 
