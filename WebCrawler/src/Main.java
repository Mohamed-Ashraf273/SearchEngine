
import org.jsoup.Jsoup;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Main {

    private static final int NoThreads = 10;
    public static void main(String[] args) throws InterruptedException {
        DB db = new DB();
        Queue<String> Links = new LinkedList<String>();
        Set<String> Crawlset = new HashSet<String>();
        if(db.getCount("queue") > 0 && db.getCount("crawlset") > 0)
        {
            ResultSet Q = db.selectURLs("queue");
            ResultSet Set = db.selectURLs("crawlset");

            try {
                while(Q.next())
                {
                    Links.add(Q.getString("document"));
                }
                while(Set.next())
                {
                    Crawlset.add(Set.getString("document"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            readFile(Links);
        }

        Queue<Thread> Threads = new LinkedList<Thread>();
        Map<String,Vector<String>> disallowedMap = new HashMap<String,Vector<String>>();
        Map<String,Vector<String>> allowedMap = new HashMap<String,Vector<String>>();


        for(int i=0;i<NoThreads;i++)
        {
            Thread t = new Thread(new Bot(Links,Crawlset,disallowedMap,allowedMap,i,db));
            Threads.add(t);
            t.start();
        }
        for(int i=0;i<NoThreads;i++)
        {
            Thread t = Threads.remove();
            t.join();
        }

        writeToFile("crawled.txt",Crawlset);
        for(Map.Entry<String,Vector<String>> entry : disallowedMap.entrySet())
        {
            Vector<String> val = entry.getValue();
            System.out.println(entry.getKey() + " : ");
            for(String value : val)
            {
                System.out.println(value);
            }
        }
        db.clearTable("urls");
        db.clearTable("crawlset");
        db.clearTable("queue");
        for(String url : Crawlset)
        {
            db.InsertURL(url,"urls");
        }


    }
    private static void readFile(Queue<String> links)
    {
        File file = new File("src/seed.txt");
        try {
            Scanner reader = new Scanner(file);
            while(reader.hasNextLine())
            {
                String link = reader.nextLine();
                links.add(link);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private static void writeToFile(String filename, Set<String> crawledLinks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String link : crawledLinks) {
                writer.write(link);
                writer.newLine();
            }
            System.out.println("Crawled links written to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}