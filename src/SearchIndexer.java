import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchIndexer {
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private float averageLength = 1.0f;

    public SearchIndexer(String indexDir) {
        analyzer = new SmartChineseAnalyzer();
        try{
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            indexWriter = new IndexWriter(dir,iwc);
        }catch(IOException e){
            e.printStackTrace();
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
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(fileList[i]), "UTF-8"));

                // TODO: need to negotiate with gzp / field.setboost
                String temp = null;
                String content = "";
                while ((temp = reader.readLine()) != null) {
                    content += temp + " ";
                }
                System.out.println(content);

                Document document = new Document();
                Field contentField = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
                averageLength += content.length();
                document.add(contentField);
                // TODO: document.setboost
                indexWriter.addDocument(document);
                if (i % 10000 == 0) {
                    System.out.println("process " + i);
                }
            }
            averageLength /= indexWriter.numDocs();
            System.out.println("average length = " + averageLength);
            System.out.println("total " + indexWriter.numDocs() + " documents");
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SearchIndexer indexer = new SearchIndexer("forIndex/index");
        indexer.indexSpecialFile("input");
        indexer.saveGlobals("forIndex/global.txt");
    }
}
