import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class PageRank {
    public double alpha = 0.15f;
    public int TN = 25;
    double[] pageRank = null;

    public void setTN(int TN) {
        this.TN = TN;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void CalPR() throws IOException {
        String fileName = "input/node.txt";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
        HashMap<Long, String> name = new HashMap<Long, String>();
        HashMap<Long, Integer> outDegree = new HashMap<Long, Integer>();
        HashMap<Long, Long> compare = new HashMap<Long, Long>();
        int total = 0;
        while (true)
        {
            String attribute = reader.readLine();
            if (attribute == null) break;
            String[] name_number = attribute.split("-->");
            int length = name_number.length;
            System.out.println((long) total + "+++++");
            name.put(Long.parseLong(name_number[length - 1]), name_number[0]);
            outDegree.put(Long.parseLong(name_number[length - 1]), total);
            compare.put((long) total, Long.parseLong(name_number[length - 1]));
            total++;
        }
        reader.close();

        pageRank = new double[total];
        double[] I = new double[total];
        double[] outD = new double[total];
        double[] inD = new double[total];
        double S = 0;
        for (int i = 0; i < total; i++)
        {
            pageRank[i] = 1.0 / total;
            I[i] = alpha / total;
            outD[i] = 0;
            inD[i] = 0;
        }

        String fileD = "input/map.txt";
        BufferedReader readerD = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileD)), "UTF-8"));
        while (true)
        {
            String line = readerD.readLine();
            if (line == null) break;
            String[] source = line.split(":");
            String[] destination = source[1].split(",");
            if (source[1].equals(""))
            {
                outD[outDegree.get(Long.parseLong(source[0]))] += 0;
            }
            else
            {
                outD[outDegree.get(Long.parseLong(source[0]))] += destination.length;
                for (int i = 0; i < destination.length; i++)
                {
                    inD[outDegree.get(Long.parseLong(destination[i]))] += 1;
                }
            }
        }
        readerD.close();
        for (int i = 0; i < total; i++)
        {
            if (outD[i] == 0)
            {
                S += pageRank[i];
            }
        }

        for (int k = 0; k < TN; k++)
        {
            for (int i = 0; i < total; i++)
            {
                I[i] = alpha / total;
            }
            readerD = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileD)), "UTF-8"));
            while (true)
            {
                String line = readerD.readLine();
                if (line == null) break;
                String[] source = line.split(":");
                if (source[1].equals(""))
                    continue;
                String[] destination = source[1].split(",");
                if (!destination[0].equals(""))
                {
                    for (int j = 0; j < destination.length; j++)
                    {
                        I[outDegree.get(Long.parseLong(destination[j]))] += (1.0 - alpha)
                                * pageRank[outDegree.get(Long.parseLong(source[0]))]
                                / outD[outDegree.get(Long.parseLong(source[0]))];
                    }
                }
            }
            readerD.close();
            for (int n = 0; n < total; n++)
            {
                pageRank[n] = I[n] + (1.0 - alpha) * S / total;
            }

            S = 0;
            for (int i = 0; i < total; i++)
            {
                if (outD[i] == 0)
                {
                    S += pageRank[i];
                }
            }
        }
        
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File("forIndex/pageRank.txt"))));
        for (int i = 0; i < total; i++) {
            writer.write(name.get(compare.get((long) i)) + "-->" + pageRank[i] + "\r\n");
        }
        writer.close();

//        StreamWriter writer = new StreamWriter("result.txt");
//        StreamWriter inWriter = new StreamWriter("wiki.in");
//        StreamWriter outWriter = new StreamWriter("wiki.out");
//        StreamWriter csvWriter = new StreamWriter("result.csv");
//        csvWriter.WriteLine("sequence,pagerank,in_degree,out_degree");
//        for (int i = 0; i < total; i++)
//        {
//            writer.WriteLine(name[compare[i]] + "-->" + pageRank[i]);
//            inWriter.WriteLine(name[compare[i]] + "-->" + inD[i]);
//            outWriter.WriteLine(name[compare[i]] + "-->" + outD[i]);
//            csvWriter.WriteLine(i + "," + pageRank[i] + "," + inD[i] + "," + outD[i]);
//        }
//        writer.Close();
//        inWriter.Close();
//        outWriter.Close();
//        csvWriter.Close();
    }

    public static void main(String[] args) throws IOException {
        PageRank pRank = new PageRank();
        pRank.CalPR();
    }
}
