import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchIndexer {
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private float averageLength = 1.0f;
    HashMap<String, Double> pageRank = new HashMap<String, Double>();

    public SearchIndexer(String indexDir) {
        analyzer = new IKAnalyzer();
        try{
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            indexWriter = new IndexWriter(dir,iwc);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void ReadPageRank(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] temp = line.split("-->");
            pageRank.put(temp[0], Double.parseDouble(temp[1]));
        }
    }

    public void saveGlobals(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            pw.println(averageLength);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void indexSpecialFile(String filename) {
        try {
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"));

            // TODO: need to negotiate with gzp / field.setboost
            String temp = null;
            String title = null;
            String content = null;
            String ID = null;
            while ((temp = reader.readLine()) != null) {
                String[] all = temp.split("==>");
                ID = all[0];
                title = all[1];
                content = all[2];
                Document document = new Document();
                Field contentField = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
                Field titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
                Field urltField = new Field("ID", ID, Field.Store.YES, Field.Index.NO);
                averageLength += content.length();
                if (pageRank.containsKey(ID)) {
                    double boost = pageRank.get(ID);
                    System.out.println(ID + "-->" + boost);
                    contentField.setBoost((float) boost * 0.8f);
                    titleField.setBoost((float) boost * 1.0f);
                } else {
                    contentField.setBoost(1.0f);
                    titleField.setBoost(1.0f);
                }
                document.add(contentField);
                document.add(titleField);
                document.add(urltField);
                // TODO: document.setboost
                indexWriter.addDocument(document);
            }

            averageLength /= indexWriter.numDocs();
            System.out.println("average length = " + averageLength);
            System.out.println("total " + indexWriter.numDocs() + " documents");
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SearchIndexer indexer = new SearchIndexer("forIndex/index");
        // indexer.ReadPageRank("forIndex/pageRank.txt");
        indexer.indexSpecialFile("input/list_new_new.txt");
        indexer.saveGlobals("forIndex/global.txt");
    }
}
