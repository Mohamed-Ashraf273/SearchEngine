
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Bot implements Runnable {
    private static final int MAX_LINKS = 10;
    private int count =6000;
    private DB db;
    private Queue<String> SharedQ;
    private Set<String> SharedSet;
    Map<String,Vector<String>> disallowedMap;
    Map<String,Vector<String>> allowedMap;
    private int id;


    public Bot(Queue<String>seed , Set<String>crawlset, Map<String, Vector<String>>disallowedMap,Map<String,Vector<String>>allowedMap, int id,DB db)
    {
        this.id = id;
        this.SharedQ = seed;
        this.SharedSet = crawlset;
        this.disallowedMap=disallowedMap;
        this.allowedMap=allowedMap;
        this.db = db;
    }

    public void run()
    {
        Crawl();
    }

    private void Crawl()
    {
        String url = null;
        while(true)
        {
            synchronized (SharedSet)
            {
                if(SharedSet.size() >= count) {
                    break;
                }
            }
            boolean found = false;
            synchronized (SharedQ)
            {
                synchronized (SharedSet)
                {
                    if(!SharedQ.isEmpty())
                    {
                        String front = SharedQ.remove();
                        if(!(SharedSet.contains(front)))
                        {
                            url = front;
                            found = true;
                        }
                        if(found)
                        {
                            System.out.println("Thread no " + id +"Crawled this page :" + url);
                            SharedSet.add(url);
                            db.InsertURL(url,"crawlset");
                        }
                    }
                }
            }
            if(found)
            {
                //scrape the added link
                Scraper s = new Scraper(this.SharedQ,url,disallowedMap,allowedMap,db);
                s.Scrape();
            }
        }
    }

}
