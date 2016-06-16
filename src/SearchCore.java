import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchCore {
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private float avgLength = 1.0f;
    private TopDocs results = null;

    private String temporalQuery = null;

    public SearchCore(String indexdir) {
        analyzer = new IKAnalyzer();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TopDocs searchQuery(String queryString, String[] field, int maxnum) {
        try {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(field, analyzer);

            TopDocs results = searcher.search(parser.parse(queryString), maxnum);
            System.out.println(results);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject returnNewQuery(String query, int queryNum) {
        JSONObject json = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        temporalQuery = query;

        String[] field = new String[2];
        field[0] = "title";
        field[1] = "content";
        SearchCore search = new SearchCore("forIndex/index");

        results = search.searchQuery(query, field, 10000);
        ScoreDoc[] hits = results.scoreDocs;
        for (int i = 0; i < Math.min(hits.length, queryNum); i++) {
            Document document = search.getDoc(hits[i].doc);
            temp.put("ID", document.get("ID"));
            temp.put("title", lengthRestriction(document.get("title"), "title"));
            temp.put("content", lengthRestriction(document.get("content"), "content"));
            jsonArray.put(temp);
        }
        json.put("result", jsonArray);
        json.put("sum", hits.length);
        return json;
    }

    public JSONObject returnOldQuery(int lowerBound, int upperBound) {
        ScoreDoc[] hits = results.scoreDocs;
        JSONObject json = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        SearchCore search = new SearchCore(temporalQuery);

        for (int i = Math.min(hits.length, lowerBound); i < Math.min(hits.length, upperBound); i++) {
            Document document = search.getDoc(hits[i].doc);
            temp.put("ID", document.get("ID"));
            temp.put("title", lengthRestriction(document.get("title"), "title"));
            temp.put("content", lengthRestriction(document.get("content"), "content"));
            jsonArray.put(temp);
        }
        json.put("result", jsonArray);
        json.put("sum", hits.length);
        return json;
    }

    private String lengthRestriction(String content, String type) {
        if (type.equals("title")) {
            if (content.length() > 30) {
                String res = content.substring(0, 30);
                res = res + "...";
                return res;
            } else {
                return content;
            }
        }
        else if (type.equals("content")) {
            if (content.length() > 75) {
                String res = content.substring(0, 75);
                res = res + "...";
                return res;
            }
            else {
                return content;
            }
        }
        return content;
    }

    public Document getDoc(int docID) {
        try {
            return searcher.doc(docID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadGlobals(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String line = reader.readLine();
            avgLength = Float.parseFloat(line);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getAvg() {
        return avgLength;
    }

    public static void main(String[] args) {
        SearchCore search = new SearchCore("forIndex/index");
        File directory = new File("");
        System.out.println(directory.getAbsolutePath());
        search.loadGlobals("forIndex/global.txt");
        System.out.println("avg length = " + search.getAvg());

        String[] field = new String[2];
        field[0] = "title";
        field[1] = "content";
        TopDocs result = search.searchQuery("史宗恺", field, 10000);
        ScoreDoc[] hits = result.scoreDocs;
        for (int i = 0; i < hits.length; i++) { // output raw format
            Document doc = search.getDoc(hits[i].doc);
            System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " ID= " + doc.get("ID"));
        }
        System.out.println("total = " + hits.length);
    }
}
