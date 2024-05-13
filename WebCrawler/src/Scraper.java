import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Scraper {
    private static final int MAX_SCRAPE = 25;
    private int count =0;
    private DB db;

    String url;

    Queue<String> SharedQ;
    Map<String,Vector<String>>disallowedMap;
    Map<String,Vector<String>>allowedMap;



    public Scraper(Queue<String>q , String url,Map<String,Vector<String>>disallowedMap,Map<String,Vector<String>>allowedMap,DB db)
    {
        this.url = url;
        this.SharedQ = q;
        this.disallowedMap=disallowedMap;
        this.allowedMap=allowedMap;
        this.db = db;
    }

    public void Scrape()
    {
        URL url = null;
        try {
            url = new URL(this.url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String host = url.getHost();
        if(!disallowedMap.containsKey(host) || allowedMap.containsKey(host))
        {
            readRobottxt(this.url);
        }


        Document doc = getDoc(this.url);
        if(doc != null)
        {
            Elements links = doc.select("a[href]");
            for(Element link : links)
            {
                if(count >= MAX_SCRAPE)
                {
                    break;
                }
                String linkurl = link.absUrl("href");
                if(!isValidURL(linkurl))
                {
                    continue;
                }
                URL url2;
                URI uri;
                try {
                    url2 = new URL(linkurl);
                    uri = new URI(linkurl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                String linkhost = url2.getHost();
                if(!disallowedMap.containsKey(host) || allowedMap.containsKey(host))
                {
                    readRobottxt(linkurl);
                }
                if(isinMap(linkurl,url2,disallowedMap))
                {
                    if(!isinMap(linkurl,url2,allowedMap)) {
                        continue;
                    }
                }
                SharedQ.add(uri.normalize().toString());
                db.InsertURL(uri.normalize().toString(),"queue");
                count++;
            }
        }
    }


    private Document getDoc(String url)
    {
        try {
            Connection connection = Jsoup.connect(url);
            Document doc = connection.get();
            if(connection.response().statusCode() == 200)
            {
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isinMap(String strurl , URL url,Map<String,Vector<String>> map) {
        String urlpath = url.getPath();
        String host = url.getHost();
        if (!urlpath.endsWith("/")) {
            urlpath += "/";
        }
        for (int i = 0; i < map.get(host).size(); i++) {
            String regex = map.get(host).get(i);
            if(regex.equals("/"))
            {
                return true;
            }
            regex = regex.replace("*", ".*");
            if (urlpath.startsWith(regex)) {
                return true;
            }
            try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(urlpath);
                if (matcher.matches()) {
                    return true;
                }
            }
            catch(PatternSyntaxException e)
            {

            }
        }
        return false;
    }

    private void readRobottxt(String urlstr)
    {
        URL url;
        Vector<String> disallowedPaths = new Vector<String>();
        Vector<String> allowedPaths = new Vector<String>();
        boolean userAgentMatch = false;
        try {
            url = new URL(urlstr);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String base = url.getProtocol() + "://" + url.getHost();
        Document doc;
        try {
            doc = Jsoup.connect(base + "/robots.txt").get();
        } catch (IOException e) {
            disallowedMap.put(url.getHost(),disallowedPaths);
            allowedMap.put(url.getHost(),allowedPaths);
            return;
        }
        String[] lines = doc.text().split("\\s+");
        disallowedMap.put(url.getHost(),disallowedPaths);
        allowedMap.put(url.getHost(),allowedPaths);

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equalsIgnoreCase("User-agent:") && i + 1 < lines.length && lines[i + 1].equals("*")) {
                userAgentMatch = true;
            } else if (userAgentMatch && lines[i].equalsIgnoreCase("Disallow:") && i + 1 < lines.length) {
                disallowedPaths.add(lines[i + 1].trim());
            }
            else if(userAgentMatch && lines[i].equalsIgnoreCase("Allow:") && i + 1 < lines.length)
            {
                allowedPaths.add(lines[i+1].trim());
            }
            else if (lines[i].equalsIgnoreCase("User-agent:")) {
                userAgentMatch = false;
            }
        }



    }
    private static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
