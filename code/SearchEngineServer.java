import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class SearchEngineServer {
    // for mohamed ashraf for testing not in main code
    // ---------------------------------------------------------------------
    private static class doc {
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
    private static word[] wrds;
    private static boolean waitTillCalc;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/search", new SearchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");

        // mohamed tests
        // here--------------------------------------------------------------
        // my temp database
        wrds = new word[3];
        waitTillCalc = true;
        for (int i = 0; i < wrds.length; i++) {
            wrds[i] = new word(); // Create a new word object
            wrds[i].docs = new doc[10]; // Initialize the docs array with a size of 10

            // Initialize each doc object within the docs array
            for (int j = 0; j < wrds[i].docs.length; j++) {
                wrds[i].docs[j] = new doc(); // Create a new doc object
                wrds[i].docs[j].Tf_IDF_total = 0;
                wrds[i].docs[j].TF_IDF = 0;
                wrds[i].docs[j].TF = 0;
                wrds[i].docs[j].PageRank = 0;
            }
        }
        wrds[0].DF = 10.0 / 30.0;
        wrds[0].wrd = "football";
        wrds[0].docs[0].url = "https://www.crazygames.com/t/soccer";
        wrds[0].docs[1].url = "https://playfootball.games/";
        wrds[0].docs[2].url = "https://poki.com/en/american-football";
        wrds[0].docs[3].url = "https://www.theguardian.com/football";
        wrds[0].docs[4].url = "https://www.britannica.com/sports/football-soccer";
        wrds[0].docs[5].url = "https://www.aljazeera.com/sports/liveblog/2024/4/23/live-arsenal-vs-chelsea-premier-league-football";
        wrds[0].docs[6].url = "https://en.wikipedia.org/wiki/Association_football";
        wrds[0].docs[7].url = "https://www.bbc.com/sport/football";
        wrds[0].docs[8].url = "https://en.wikipedia.org/wiki/Football";
        wrds[0].docs[9].url = "https://www.skysports.com/football";
        for (int i = 0; i < 10; i++) {
            wrds[0].docs[i].TF = calc_tf(wrds[0].wrd, wrds[0].docs[i].url);
            System.out.println(wrds[0].docs[i].TF);
            wrds[0].docs[i].TF_IDF = calc_tfIdf(wrds[0].docs[i].url, wrds[0].DF, wrds[0].docs[i].TF);
        }

        wrds[1].DF = 10 / 30;
        wrds[1].wrd = "food";
        wrds[1].docs[0].url = "https://en.wikipedia.org/wiki/Food";
        wrds[1].docs[1].url = "https://www.fao.org/home/en";
        wrds[1].docs[2].url = "https://www.food.com/";
        wrds[1].docs[3].url = "https://www.nationalgeographic.org/article/food/";
        wrds[1].docs[4].url = "https://www.foodnetwork.com/";
        wrds[1].docs[5].url = "https://dictionary.cambridge.org/dictionary/english/food";
        wrds[1].docs[6].url = "https://www.merriam-webster.com/dictionary/food";
        wrds[1].docs[7].url = "https://www.reddit.com/r/food/";
        wrds[1].docs[8].url = "https://www.buzzfeed.com/food";
        wrds[1].docs[9].url = "https://www.fda.gov/food";
        // for (int i = 0; i < 10; i++) {
        // wrds[1].docs[i].TF = calc_tf(wrds[1].wrd, wrds[1].docs[i].url);
        // wrds[1].docs[i].TF_IDF = calc_tfIdf(wrds[1].docs[i].url, wrds[1].DF,
        // wrds[1].docs[i].TF);
        // }

        wrds[2].DF = 10 / 30;
        wrds[2].wrd = "games";
        wrds[2].docs[0].url = "https://playfootball.games/";
        wrds[2].docs[1].url = "https://www.crazygames.com/t/soccer";
        wrds[2].docs[2].url = "https://poki.com/en/american-football";
        wrds[2].docs[3].url = "https://www.gamespot.com/";
        wrds[2].docs[4].url = "https://www.wired.com/tag/video-games/";
        wrds[2].docs[5].url = "https://www.metacritic.com/browse/game/all/all/current-year/";
        wrds[2].docs[6].url = "https://www.circana.com/industry-expertise/video-games/";
        wrds[2].docs[7].url = "https://videogamesplus.ca/";
        wrds[2].docs[8].url = "https://www.target.com/c/video-games/-/N-5xtg5";
        wrds[2].docs[9].url = "https://en.wikipedia.org/wiki/Video_game";
        for (int i = 0; i < 10; i++) {
            wrds[2].docs[i].TF = calc_tf(wrds[2].wrd, wrds[2].docs[i].url);
            wrds[2].docs[i].TF_IDF = calc_tfIdf(wrds[2].docs[i].url, wrds[2].DF,
                    wrds[2].docs[i].TF);
        }
        System.out.println("finished");
        waitTillCalc = false;
        // --------------------------------------------------------------------------------
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();// query words from the user in a string form
                String[] finalList = null;
                while (waitTillCalc) {
                    try {
                        TimeUnit.SECONDS.sleep(1); // Delay for 5 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Perform operations based on the query
                word[] words_have_common_docs;
                if ("football".equals(query)) {
                    words_have_common_docs = new word[1];
                    words_have_common_docs[0] = new word();
                    words_have_common_docs[0] = wrds[0];
                    doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                    for (int i = 0; i < Docs.length; i++) {
                        Docs[i] = new doc();
                    }
                    Ranker(words_have_common_docs, Docs);
                    finalList = new String[Docs.length];// modify the size to be the final list size
                    for (int i = 0; i < Docs.length; i++) {
                        finalList[i] = Docs[i].url;
                    }
                    // for (String url : finalList) {
                    // System.out.println(url);
                    // }
                } else if ("food".equals(query)) {
                    words_have_common_docs = new word[1];
                    words_have_common_docs[0] = new word();
                    words_have_common_docs[0] = wrds[1];
                    // doc[] Docs = Ranker(words_have_common_docs);
                    // for (int i = 0; i < Docs.length; i++) {
                    // finalList[i] = Docs[i].url;
                    // }
                } else {
                    if ("Games".equals(query)) {
                        words_have_common_docs = new word[1];
                        words_have_common_docs[0] = new word();
                        words_have_common_docs[0] = wrds[2];
                        doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                        for (int i = 0; i < Docs.length; i++) {
                            Docs[i] = new doc();
                        }
                        Ranker(words_have_common_docs, Docs);
                        finalList = new String[Docs.length];// modify the size to be the final list size
                        for (int i = 0; i < Docs.length; i++) {
                            finalList[i] = Docs[i].url;
                        }
                    } else {
                        words_have_common_docs = new word[2];
                        words_have_common_docs[0] = new word();
                        words_have_common_docs[0].DF = 3;
                        words_have_common_docs[0].wrd = "football";
                        words_have_common_docs[0].docs = new doc[3];
                        words_have_common_docs[0].docs[0] = new doc();
                        words_have_common_docs[0].docs[0] = wrds[0].docs[0];
                        words_have_common_docs[0].docs[1] = new doc();
                        words_have_common_docs[0].docs[1] = wrds[0].docs[1];
                        words_have_common_docs[0].docs[2] = new doc();
                        words_have_common_docs[0].docs[2] = wrds[0].docs[2];

                        words_have_common_docs[1] = new word();
                        words_have_common_docs[1].DF = 3;
                        words_have_common_docs[1].wrd = "Games";
                        words_have_common_docs[1].docs = new doc[3];
                        words_have_common_docs[1].docs[0] = new doc();
                        words_have_common_docs[1].docs[0] = wrds[2].docs[0];
                        words_have_common_docs[1].docs[1] = new doc();
                        words_have_common_docs[1].docs[1] = wrds[2].docs[1];
                        words_have_common_docs[1].docs[2] = new doc();
                        words_have_common_docs[1].docs[2] = wrds[2].docs[2];

                        doc[] Docs = new doc[words_have_common_docs[0].docs.length];
                        for (int i = 0; i < Docs.length; i++) {
                            Docs[i] = new doc();
                        }
                        Ranker(words_have_common_docs, Docs);
                        finalList = new String[Docs.length];// modify the size to be the final list size
                        for (int i = 0; i < Docs.length; i++) {
                            finalList[i] = Docs[i].url;
                        }
                    }
                }

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

    // calc pagerank of a url
    public static int calc_pageRank(String urlLink) {
        int pr = 0;
        return pr;
    }

    // Ranker function
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
                if (ds[j + 1].Tf_IDF_total > ds[j].Tf_IDF_total) {
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
