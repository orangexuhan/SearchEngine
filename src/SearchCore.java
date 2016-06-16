import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchCore {
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private float avgLength = 1.0f;

    public SearchCore(String indexdir) {
        analyzer = new SmartChineseAnalyzer();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
            searcher = new IndexSearcher(reader);
            // searcher.setSimilarity(new MySimilarity());
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

        String[] field = new String[1];
        field[0] = "content";
        TopDocs results = search.searchQuery("徐王白邈", field, 10);
        ScoreDoc[] hits = results.scoreDocs;
        for (int i = 0; i < hits.length; i++) { // output raw format
            Document doc = search.getDoc(hits[i].doc);
            System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " content= " + doc.get("content"));
        }
    }
}
