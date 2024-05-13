package SearchEngine;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.Doc;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.Vector;
import SearchEngine.QueryProcessor;
public class SearchEngineServer {
    // for mohamed ashraf for testing not in main code
    // ---------------------------------------------------------------------
    public static class doc {
        String url;
        double TF;// normalized
        double TF_IDF;
        double Tf_IDF_total;// if there is more than one word
        double PageRank;
    }

    private static class word {
        String wrd;
        doc[] docs;
        double DF;// norm
    }

    // ---------------------------------------------------------------------
    private static Vector<doc> ResuledDocs;
    private static Vector<word> queryWords;
    // private static boolean waitTillCalc;
    // private static boolean waitTillFillingDocsAndWrds;
    private static int querySize;
    private static QueryProcessor obj3;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/search", new SearchHandler());
        server.createContext("/getPara", new getParaHandler());
        server.createContext("/getSuggestions", new getSuggestionsHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
        // for (int i = 0; i < totalSystemDocs.length; i++) {
        // totalSystemDocs[i] = new doc();
        // }
        // // mohamed tests
        // // here--------------------------------------------------------------
        // // my temp database
        // try (BufferedReader br = new BufferedReader(new
        // FileReader("LinksToTestRanker.txt"))) {
        // String line;
        // int i = 0;
        // while ((line = br.readLine()) != null) {
        // // Process each line here
        // totalSystemDocs[i].url = line;
        // // System.out.println(line); // For example, print each line to the console
        // i++;
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        // waitTillFillingDocsAndWrds = true;
        // while (waitTillFillingDocsAndWrds) {
        // try {
        // TimeUnit.SECONDS.sleep(1); // Delay for 5 seconds
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }

        // waitTillCalc = true;
        // for (int i = 0; i < querySize; i++) {
        // wrds[i] = new word(); // Create a new word object
        // wrds[i].docs = new doc[10]; // Initialize the docs array with a size of 10

        // // Initialize each doc object within the docs array
        // for (int j = 0; j < wrds[i].docs.length; j++) {
        // wrds[i].docs[j] = new doc(); // Create a new doc object
        // wrds[i].docs[j].Tf_IDF_total = 0;
        // wrds[i].docs[j].TF_IDF = 0;
        // wrds[i].docs[j].TF = 0;
        // wrds[i].docs[j].PageRank = 0;
        // }
        // }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // calc pageRank here
        // System.out.println("NowCalculating Pageranks");
        // doc[] Docs = ResuledDocs.toArray(new doc[ResuledDocs.size()]);
        // calc_pageRank(Docs);
        // // assign each url in words with its page rank
        // // for (int i = 0; i < 3; i++) {
        // // for (int j = 0; j < wrds[i].docs.length; j++) {
        // // for (int k = 0; k < totalSystemDocs.length; k++) {
        // // if (wrds[i].docs[j].url.equals(totalSystemDocs[k].url)) {
        // // wrds[i].docs[j].PageRank = totalSystemDocs[k].PageRank;
        // // }
        // // }
        // // }
        // // }
        // System.out.println("finished");
        // waitTillCalc = false;
        // --------------------------------------------------------------------------------
    }

    static class getSuggestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery(); // query words from the user in a string form
                String[] suggestionsList = null;
                String temp = "";
                try (BufferedReader br = new BufferedReader(new FileReader(query))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        // Process each line here
                        temp += line;
                        temp += "\n";
                        // System.out.println(line); // For example, print each line to the console
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // System.out.println(temp);
                suggestionsList = temp.split("\n");
                // Send response to the frontend
                try {
                    // Enable CORS by setting appropriate headers
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                            "Content-Type, Authorization");

                    // Set the response status code to 200 (OK)
                    exchange.sendResponseHeaders(200, 0);

                    // Get the response body OutputStream
                    OutputStream os = exchange.getResponseBody();

                    // Write each URL in the array as a separate line to the response body
                    for (String str : suggestionsList) {
                        os.write(str.getBytes());
                        os.write("\n".getBytes()); // Add a newline character between each URL
                    }

                    // Close the OutputStream
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class getParaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery(); // query words from the user in a string form
                URL url = new URL(query);
                // Open a connection to the URL
                URLConnection urlcon = url.openConnection();

                InputStream stream = urlcon.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));

                // Read the HTML content line by line
                StringBuilder htmlContent = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    htmlContent.append(inputLine);
                }
                // Close the input stream
                in.close();
                // Getting paragraphs in the page
                String html = htmlContent.toString();
                Pattern pattern = Pattern.compile("<p>(.*?)</p>");
                Matcher matcher = pattern.matcher(html);
                int max = 0;
                String maxPara = "";
                while (matcher.find()) {
                    String paragraph = matcher.group(1);
                    // Remove any HTML tags from the paragraph
                    paragraph = paragraph.replaceAll("<[^>]*>", "");
                    // Ensure the paragraph contains words
                    if (!paragraph.trim().isEmpty()) {
                        // do something with para
                        if (paragraph.length() > max) {
                            max = paragraph.length();
                            maxPara = paragraph;
                        }
                    }
                }

                // Send response to the frontend
                try {
                    // Set the response status code to 200 (OK)
                    // Enable CORS by setting appropriate headers
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                            "Content-Type, Authorization");
                    exchange.sendResponseHeaders(200, 0);

                    // Get the response body OutputStream
                    OutputStream os = exchange.getResponseBody();

                    // Write the max paragraph to the response body
                    os.write(maxPara.getBytes());

                    // Close the OutputStream
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }

        }
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();// query words from the user in a string form
                String[] finalList = null;
                ResuledDocs = new Vector<>();
                queryWords = new Vector<>();

                // while (waitTillCalc) {
                // try {
                // TimeUnit.SECONDS.sleep(1); // Delay for 5 seconds
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                // }
                String[] qWrds = query.split(" ");
                querySize = qWrds.length;

                // get resuled List and fill the vector
                obj3.OpenConnection();
                ResuledDocs = obj3.GetSearchQuery(qWrds);
                // filling queryWords
                for (int i = 0; i < querySize; i++) {
                    word w = new word();
                    w.wrd = qWrds[i];
                    Vector<doc> d = new Vector<>();
                    // fill each word with its docs(get docs related to each word)
                    // ...............
                    obj3.GetDocumentsForWord(qWrds[i], d);
                    ///////////////////////////////////////////////////////////
                    doc[] dArr = d.toArray(new doc[d.size()]);
                    w.docs = dArr;
                    // assign to each word its df
                    w.DF = d.size();
                    queryWords.add(w);
                }
                obj3.CloseConnection();
                // waitTillFillingDocsAndWrds = false;

                // Getting Common Docs:
                // Initialize with the first vector of strings
                Vector<doc> vec = new Vector<>();
                // Add all elements from the array to the Vector
                Collections.addAll(vec, queryWords.get(0).docs);
                Vector<doc> commonDcs = new Vector<>(vec);

                // Iterate over remaining vectors
                for (int i = 1; i < queryWords.size(); i++) {
                    Vector<doc> vect = new Vector<>();
                    // Add all elements from the array to the Vector
                    Collections.addAll(vect, queryWords.get(i).docs);
                    Vector<doc> currentDcs = vect;
                    // Retain only the common strings between the current vector and the
                    // commonStrings
                    commonDcs.retainAll(currentDcs);
                }

                // calc pagearank
                System.out.println("NowCalculating Pageranks");
                doc[] Docs = ResuledDocs.toArray(new doc[ResuledDocs.size()]);
                calc_pageRank(Docs);
                // assign each url in words with its page rank
                // for (int i = 0; i < 3; i++) {
                // for (int j = 0; j < wrds[i].docs.length; j++) {
                // for (int k = 0; k < totalSystemDocs.length; k++) {
                // if (wrds[i].docs[j].url.equals(totalSystemDocs[k].url)) {
                // wrds[i].docs[j].PageRank = totalSystemDocs[k].PageRank;
                // }
                // }
                // }
                // }
                System.out.println("finished");
                word[] wordsArray = queryWords.toArray(new word[queryWords.size()]);

                // Convert the second vector of docs to an array
                doc[] docsArray = commonDcs.toArray(new doc[commonDcs.size()]);
                Ranker(wordsArray, docsArray);
                finalList = new String[Docs.length];// modify the size to be the final list size
                for (int i = 0; i < Docs.length; i++) {
                    finalList[i] = docsArray[i].url;
                }
                // Perform operations based on the query
                // if ("football".equals(query)) {
                // words_have_common_docs = new word[1];
                // words_have_common_docs[0] = new word();
                // words_have_common_docs[0] = wrds[0];
                // doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                // for (int i = 0; i < Docs.length; i++) {
                // Docs[i] = new doc();
                // }
                // Ranker(words_have_common_docs, Docs);
                // finalList = new String[Docs.length];// modify the size to be the final list
                // size
                // for (int i = 0; i < Docs.length; i++) {
                // finalList[i] = Docs[i].url;
                // }
                // // for (String url : finalList) {
                // // System.out.println(url);
                // // }
                // } else if ("food".equals(query)) {
                // words_have_common_docs = new word[1];
                // words_have_common_docs[0] = new word();
                // words_have_common_docs[0] = wrds[1];
                // doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                // for (int i = 0; i < Docs.length; i++) {
                // Docs[i] = new doc();
                // }
                // Ranker(words_have_common_docs, Docs);
                // finalList = new String[Docs.length];// modify the size to be the final list
                // size
                // for (int i = 0; i < Docs.length; i++) {
                // finalList[i] = Docs[i].url;
                // }
                // } else {
                // if ("Games".equals(query)) {
                // words_have_common_docs = new word[1];
                // words_have_common_docs[0] = new word();
                // words_have_common_docs[0] = wrds[2];
                // doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                // for (int i = 0; i < Docs.length; i++) {
                // Docs[i] = new doc();
                // }
                // Ranker(words_have_common_docs, Docs);
                // finalList = new String[Docs.length];// modify the size to be the final list
                // size
                // for (int i = 0; i < Docs.length; i++) {
                // finalList[i] = Docs[i].url;
                // }
                // } else {
                // words_have_common_docs = new word[2];
                // words_have_common_docs[0] = new word();
                // words_have_common_docs[0].DF = 3;
                // words_have_common_docs[0].wrd = "football";
                // words_have_common_docs[0].docs = new doc[3];
                // words_have_common_docs[0].docs[0] = new doc();
                // words_have_common_docs[0].docs[0] = wrds[0].docs[0];
                // words_have_common_docs[0].docs[1] = new doc();
                // words_have_common_docs[0].docs[1] = wrds[0].docs[1];
                // words_have_common_docs[0].docs[2] = new doc();
                // words_have_common_docs[0].docs[2] = wrds[0].docs[2];

                // words_have_common_docs[1] = new word();
                // words_have_common_docs[1].DF = 3;
                // words_have_common_docs[1].wrd = "Games";
                // words_have_common_docs[1].docs = new doc[3];
                // words_have_common_docs[1].docs[0] = new doc();
                // words_have_common_docs[1].docs[0] = wrds[2].docs[0];
                // words_have_common_docs[1].docs[1] = new doc();
                // words_have_common_docs[1].docs[1] = wrds[2].docs[1];
                // words_have_common_docs[1].docs[2] = new doc();
                // words_have_common_docs[1].docs[2] = wrds[2].docs[2];

                // doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                // for (int i = 0; i < Docs.length; i++) {
                // Docs[i] = new doc();
                // }
                // Ranker(words_have_common_docs, Docs);
                // finalList = new String[Docs.length];// modify the size to be the final list
                // size
                // for (int i = 0; i < Docs.length; i++) {
                // finalList[i] = Docs[i].url;
                // }
                // }
                // }

                // Send response to the frontend
                try {
                    // Enable CORS by setting appropriate headers
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                            "Content-Type, Authorization");

                    // Set the response status code to 200 (OK)
                    exchange.sendResponseHeaders(200, 0);

                    // Get the response body OutputStream
                    OutputStream os = exchange.getResponseBody();

                    // Write each URL in the array as a separate line to the response body
                    for (String url : finalList) {
                        os.write(url.getBytes());
                        os.write("\n".getBytes()); // Add a newline character between each URL
                    }

                    // Close the OutputStream
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    // calc tf given a url and a word
    public static double calc_tf(String wrd, String urlLink) {
        int tf = 0;
        double totalWrds = 0;
        try {
            URL url = new URL(urlLink);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
                totalWrds += line.split("\\s+").length;
            }
            reader.close();

            // Count occurrences of the word in the content
            Pattern pattern = Pattern.compile("\\b" + wrd + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content.toString());

            while (matcher.find()) {
                tf++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0;
        }
        return tf / totalWrds;
    }

    // function calcualtes tf-idf of a url
    public static double calc_tfIdf(String urlLink, double df, double tf) {
        return tf * (1 / df);
    }

    // calc pagerank of a urls
    public static void calc_pageRank(doc[] urls) {
        double[][] Lmatrix = new double[urls.length][urls.length];
        double[][] Rmatrix = new double[urls.length][1];
        int ILinksToCount = 0;
        double epsilon = 0.0002;

        for (int i = 0; i < urls.length; i++) {
            Rmatrix[i][0] = 1.0 / urls.length;
        }
        for (int i = 0; i < urls.length; i++) {
            try {
                URL url = new URL(urls[i].url);

                // Open a connection to the URL
                URLConnection urlcon = url.openConnection();

                InputStream stream = urlcon.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));

                // Read the HTML content line by line
                StringBuilder htmlContent = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    htmlContent.append(inputLine);
                }
                // Close the input stream
                in.close();
                // getting urls in the page
                // String html = htmlContent.toString();
                // Pattern pattern =
                // Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
                // Matcher matcher = pattern.matcher(html);
                // while (matcher.find()) {
                // String link = matcher.group(2);
                // // Ensure the link is not empty and not a fragment identifier
                // if (!link.isEmpty() && !link.startsWith("#")) {
                // System.out.println(link);
                // }
                // }
                // System.out.println(htmlContent);
                for (int j = 0; j < urls.length; j++) {
                    if (j != i) {
                        if (htmlContent.toString().contains(urls[j].url)) {
                            // fill Lmatrix
                            // System.out.println("found one");
                            Lmatrix[j][i] = 1.0;
                            ILinksToCount++;
                        } else {
                            Lmatrix[j][i] = 0.0;
                        }
                    } else {
                        Lmatrix[j][i] = 0.0;
                    }
                }
                // normalizing Lmatrix
                if (ILinksToCount != 0) {
                    for (int j = 0; j < urls.length; j++) {
                        Lmatrix[j][i] /= ILinksToCount;
                    }
                }
                // for (int p = 0; p < urls.length; p++) {
                // System.out.println(Lmatrix[i][p]);
                // }
                ILinksToCount = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // matrix multiplication till certian accuracy
        double prevCheck;
        double sum = 0.0;
        do {
            prevCheck = Rmatrix[0][0];
            for (int m = 0; m < urls.length; m++) {
                for (int j = 0; j < urls.length; j++) {
                    sum += Lmatrix[m][j] * Rmatrix[j][0];
                }
                // System.out.println(sum);
                Rmatrix[m][0] = sum;
                sum = 0.0;
            }
        } while (Math.abs(Rmatrix[0][0] - prevCheck) >= epsilon);

        for (int i = 0; i < urls.length; i++) {
            urls[i].PageRank = Rmatrix[i][0];
        }
    }

    // Ranker function
    // note this function must take all the docs common between all words in query,
    // but why??
    // Answer: because each doc related to a word, so for example:
    // given a query of two words word1 and word2 and a doc related to word1 but not
    // to word2
    // so doc has a tf_idf to word1, but has no tf_idf related to word2
    // (if we assume the tf_idf = 0 ----> this also makes a problem which is we
    // can't get a tf_idf from a doc doesn't related to a word)
    public static void Ranker(word[] words, doc[] ds) {
        for (int i = 0; i < ds.length; i++) {
            ds[i].Tf_IDF_total = words[0].docs[i].TF_IDF;
            ds[i].url = words[0].docs[i].url;
        }
        // for (int i = 0; i < ds.length; i++) {
        // System.out.println(ds[i].url);
        // }
        // note i use array of docs to attach each url to its data
        // note if there is more than 1 element in the array it means that the query has
        // many words then they all must have the same number of docs as they will store
        // the common docs between them
        for (int k = 0; k < words[0].docs.length; k++) {// for each url in words[0]
            for (int i = 1; i < words.length; i++) {// search in all words docs about this url and add all tf_idf and
                                                    // store it in a doc
                for (int j = 0; j < words[i].docs.length; j++) {// iterate over the docs in words[i]
                    if (words[0].docs[k].url.equals(words[i].docs[j].url)) {
                        ds[k].Tf_IDF_total += words[i].docs[j].TF_IDF;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < ds.length; i++) {
            for (int j = 0; j < ds.length - 1; j++) {
                if ((ds[j + 1].Tf_IDF_total + ds[j + 1].PageRank) > (ds[j].Tf_IDF_total + ds[j].PageRank)) {
                    doc temp = ds[j + 1];
                    ds[j + 1] = ds[j];
                    ds[j] = temp;
                }
            }
        }
        // for (int i = 0; i < ds.length; i++) {
        // System.out.println(ds[i].url);
        // }
        return;
    }
}
