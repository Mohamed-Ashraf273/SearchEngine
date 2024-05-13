package SearchEngine;
import java.nio.file.Files;
import org.jsoup.Jsoup;
import java.util.TreeMap;
//import java.util.TreeSet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Vector;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import SearchEngine.SearchEngineServer.doc;
class Locations{
	String type;
	Vector<Integer> locations;
	Locations(String kind){
		type = kind;
		locations=new Vector<Integer>();
	}
};
public class Indexer {
	static mysqlServer obj;
	static SearchEngineServer obj2;
	static TreeMap<String,TreeMap<String,Locations>> invertedfile;
	public static void main(String[] args) {
		obj= new mysqlServer();
		invertedfile = new TreeMap<String,TreeMap<String,Locations>>();
		Vector<String> urls=new Vector<String>();
		boolean b = obj.LoadDocuments(urls);
		if(b) {
			for(int i=0;i<urls.size();i++) {
				indexDocument(urls.get(i));
				obj.UpdateDocument(urls.get(i), true);
				System.out.printf("document %d is indexed",i+1);
			}
		}
		insertIntoDatabase();
		obj.CloseConnection();
		/*String path = "D://loadedDocument//index3.html";
		String html = LoadDocument(path);
		String[] types=new String[8];
		String[] words = ParseDocument(html,types);
		for(int i=0;i<words.length;i++) {
			System.out.println(words[i]);
		}*/
		/*for(int i=0;i<types.length;i++) {
			if(types[i]!=null) {
				System.out.println(types[i]);	
			}
		}*/
	}
	public static String LoadDocument(String filepath) {
		String content =null;
		try {
			content = new String(Files.readAllBytes(Paths.get(filepath)));	
		} catch (IOException e) {
			e.getStackTrace();
		}
		return content;
	}
    public static String DownloadDocument(String documenturl) {
    	String htmlContent = null;
        try {
            @SuppressWarnings("deprecation")
			URL url = new URL(documenturl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                
                htmlContent = content.toString();
                
            } else {
                System.out.println("Failed to fetch the HTML document. Response Code: " + responseCode);
            }
            
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlContent;
    }
	public static boolean IsStopWord(String word) {
		String[] stopWords1 = { "a", "and", "the","an","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","such","that","their","then","there","these","they","this","to","was","will","with"};
		String[] stopWords2 = {"0o", "0s", "3a", "3b", "3d", "6b", "6o", "a", "a1", "a2", "a3", "a4", "ab", "able", "about", "above", "abst", "ac", "accordance", "according", "accordingly", "across", "act", "actually", "ad", "added", "adj", "ae", "af", "affected", "affecting", "affects", "after", "afterwards", "ag", "again", "against", "ah", "ain", "ain't", "aj", "al", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "announce", "another", "any", "anybody", "anyhow", "anymore", "anyone", "anything", "anyway", "anyways", "anywhere", "ao", "ap", "apart", "apparently", "appear", "appreciate", "appropriate", "approximately", "ar", "are", "aren", "arent", "aren't", "arise", "around", "as", "a's", "aside", "ask", "asking", "associated", "at", "au", "auth", "av", "available", "aw", "away", "awfully", "ax", "ay", "az", "b", "b1", "b2", "b3", "ba", "back", "bc", "bd", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "begin", "beginning", "beginnings", "begins", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "bi", "bill", "biol", "bj", "bk", "bl", "bn", "both", "bottom", "bp", "br", "brief", "briefly", "bs", "bt", "bu", "but", "bx", "by", "c", "c1", "c2", "c3", "ca", "call", "came", "can", "cannot", "cant", "can't", "cause", "causes", "cc", "cd", "ce", "certain", "certainly", "cf", "cg", "ch", "changes", "ci", "cit", "cj", "cl", "clearly", "cm", "c'mon", "cn", "co", "com", "come", "comes", "con", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn", "couldnt", "couldn't", "course", "cp", "cq", "cr", "cry", "cs", "c's", "ct", "cu", "currently", "cv", "cx", "cy", "cz", "d", "d2", "da", "date", "dc", "dd", "de", "definitely", "describe", "described", "despite", "detail", "df", "di", "did", "didn", "didn't", "different", "dj", "dk", "dl", "do", "does", "doesn", "doesn't", "doing", "don", "done", "don't", "down", "downwards", "dp", "dr", "ds", "dt", "du", "due", "during", "dx", "dy", "e", "e2", "e3", "ea", "each", "ec", "ed", "edu", "ee", "ef", "effect", "eg", "ei", "eight", "eighty", "either", "ej", "el", "eleven", "else", "elsewhere", "em", "empty", "en", "end", "ending", "enough", "entirely", "eo", "ep", "eq", "er", "es", "especially", "est", "et", "et-al", "etc", "eu", "ev", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "ey", "f", "f2", "fa", "far", "fc", "few", "ff", "fi", "fifteen", "fifth", "fify", "fill", "find", "fire", "first", "five", "fix", "fj", "fl", "fn", "fo", "followed", "following", "follows", "for", "former", "formerly", "forth", "forty", "found", "four", "fr", "from", "front", "fs", "ft", "fu", "full", "further", "furthermore", "fy", "g", "ga", "gave", "ge", "get", "gets", "getting", "gi", "give", "given", "gives", "giving", "gj", "gl", "go", "goes", "going", "gone", "got", "gotten", "gr", "greetings", "gs", "gy", "h", "h2", "h3", "had", "hadn", "hadn't", "happens", "hardly", "has", "hasn", "hasnt", "hasn't", "have", "haven", "haven't", "having", "he", "hed", "he'd", "he'll", "hello", "help", "hence", "her", "here", "hereafter", "hereby", "herein", "heres", "here's", "hereupon", "hers", "herself", "hes", "he's", "hh", "hi", "hid", "him", "himself", "his", "hither", "hj", "ho", "home", "hopefully", "how", "howbeit", "however", "how's", "hr", "hs", "http", "hu", "hundred", "hy", "i", "i2", "i3", "i4", "i6", "i7", "i8", "ia", "ib", "ibid", "ic", "id", "i'd", "ie", "if", "ig", "ignored", "ih", "ii", "ij", "il", "i'll", "im", "i'm", "immediate", "immediately", "importance", "important", "in", "inasmuch", "inc", "indeed", "index", "indicate", "indicated", "indicates", "information", "inner", "insofar", "instead", "interest", "into", "invention", "inward", "io", "ip", "iq", "ir", "is", "isn", "isn't", "it", "itd", "it'd", "it'll", "its", "it's", "itself", "iv", "i've", "ix", "iy", "iz", "j", "jj", "jr", "js", "jt", "ju", "just", "k", "ke", "keep", "keeps", "kept", "kg", "kj", "km", "know", "known", "knows", "ko", "l", "l2", "la", "largely", "last", "lately", "later", "latter", "latterly", "lb", "lc", "le", "least", "les", "less", "lest", "let", "lets", "let's", "lf", "like", "liked", "likely", "line", "little", "lj", "ll", "ll", "ln", "lo", "look", "looking", "looks", "los", "lr", "ls", "lt", "ltd", "m", "m2", "ma", "made", "mainly", "make", "makes", "many", "may", "maybe", "me", "mean", "means", "meantime", "meanwhile", "merely", "mg", "might", "mightn", "mightn't", "mill", "million", "mine", "miss", "ml", "mn", "mo", "more", "moreover", "most", "mostly", "move", "mr", "mrs", "ms", "mt", "mu", "much", "mug", "must", "mustn", "mustn't", "my", "myself", "n", "n2", "na", "name", "namely", "nay", "nc", "nd", "ne", "near", "nearly", "necessarily", "necessary", "need", "needn", "needn't", "needs", "neither", "never", "nevertheless", "new", "next", "ng", "ni", "nine", "ninety", "nj", "nl", "nn", "no", "nobody", "non", "none", "nonetheless", "noone", "nor", "normally", "nos", "not", "noted", "nothing", "novel", "now", "nowhere", "nr", "ns", "nt", "ny", "o", "oa", "ob", "obtain", "obtained", "obviously", "oc", "od", "of", "off", "often", "og", "oh", "oi", "oj", "ok", "okay", "ol", "old", "om", "omitted", "on", "once", "one", "ones", "only", "onto", "oo", "op", "oq", "or", "ord", "os", "ot", "other", "others", "otherwise", "ou", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "ow", "owing", "own", "ox", "oz", "p", "p1", "p2", "p3", "page", "pagecount", "pages", "par", "part", "particular", "particularly", "pas", "past", "pc", "pd", "pe", "per", "perhaps", "pf", "ph", "pi", "pj", "pk", "pl", "placed", "please", "plus", "pm", "pn", "po", "poorly", "possible", "possibly", "potentially", "pp", "pq", "pr", "predominantly", "present", "presumably", "previously", "primarily", "probably", "promptly", "proud", "provides", "ps", "pt", "pu", "put", "py", "q", "qj", "qu", "que", "quickly", "quite", "qv", "r", "r2", "ra", "ran", "rather", "rc", "rd", "re", "readily", "really", "reasonably", "recent", "recently", "ref", "refs", "regarding", "regardless", "regards", "related", "relatively", "research", "research-articl", "respectively", "resulted", "resulting", "results", "rf", "rh", "ri", "right", "rj", "rl", "rm", "rn", "ro", "rq", "rr", "rs", "rt", "ru", "run", "rv", "ry", "s", "s2", "sa", "said", "same", "saw", "say", "saying", "says", "sc", "sd", "se", "sec", "second", "secondly", "section", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "sf", "shall", "shan", "shan't", "she", "shed", "she'd", "she'll", "shes", "she's", "should", "shouldn", "shouldn't", "should've", "show", "showed", "shown", "showns", "shows", "si", "side", "significant", "significantly", "similar", "similarly", "since", "sincere", "six", "sixty", "sj", "sl", "slightly", "sm", "sn", "so", "some", "somebody", "somehow", "someone", "somethan", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "sp", "specifically", "specified", "specify", "specifying", "sq", "sr", "ss", "st", "still", "stop", "strongly", "sub", "substantially", "successfully", "such", "sufficiently", "suggest", "sup", "sure", "sy", "system", "sz", "t", "t1", "t2", "t3", "take", "taken", "taking", "tb", "tc", "td", "te", "tell", "ten", "tends", "tf", "th", "than", "thank", "thanks", "thanx", "that", "that'll", "thats", "that's", "that've", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "thered", "therefore", "therein", "there'll", "thereof", "therere", "theres", "there's", "thereto", "thereupon", "there've", "these", "they", "theyd", "they'd", "they'll", "theyre", "they're", "they've", "thickv", "thin", "think", "third", "this", "thorough", "thoroughly", "those", "thou", "though", "thoughh", "thousand", "three", "throug", "through", "throughout", "thru", "thus", "ti", "til", "tip", "tj", "tl", "tm", "tn", "to", "together", "too", "took", "top", "toward", "towards", "tp", "tq", "tr", "tried", "tries", "truly", "try", "trying", "ts", "t's", "tt", "tv", "twelve", "twenty", "twice", "two", "tx", "u", "u201d", "ue", "ui", "uj", "uk", "um", "un", "under", "unfortunately", "unless", "unlike", "unlikely", "until", "unto", "uo", "up", "upon", "ups", "ur", "us", "use", "used", "useful", "usefully", "usefulness", "uses", "using", "usually", "ut", "v", "va", "value", "various", "vd", "ve", "ve", "very", "via", "viz", "vj", "vo", "vol", "vols", "volumtype", "vq", "vs", "vt", "vu", "w", "wa", "want", "wants", "was", "wasn", "wasnt", "wasn't", "way", "we", "wed", "we'd", "welcome", "well", "we'll", "well-b", "went", "were", "we're", "weren", "werent", "weren't", "we've", "what", "whatever", "what'll", "whats", "what's", "when", "whence", "whenever", "when's", "where", "whereafter", "whereas", "whereby", "wherein", "wheres", "where's", "whereupon", "wherever", "whether", "which", "while", "whim", "whither", "who", "whod", "whoever", "whole", "who'll", "whom", "whomever", "whos", "who's", "whose", "why", "why's", "wi", "widely", "will", "willing", "wish", "with", "within", "without", "wo", "won", "wonder", "wont", "won't", "words", "world", "would", "wouldn", "wouldnt", "wouldn't", "www", "x", "x1", "x2", "x3", "xf", "xi", "xj", "xk", "xl", "xn", "xo", "xs", "xt", "xv", "xx", "y", "y2", "yes", "yet", "yj", "yl", "you", "youd", "you'd", "you'll", "your", "youre", "you're", "yours", "yourself", "yourselves", "you've", "yr", "ys", "yt", "z", "zero", "zi", "zz"};
		HashSet<String> stopWordsSet1 = new HashSet<>(Arrays.asList(stopWords1));
		HashSet<String> stopWordsSet2 = new HashSet<>(Arrays.asList(stopWords2));
		if (stopWordsSet1.contains(word.toLowerCase())||stopWordsSet2.contains(word.toLowerCase())){
			return true;
		}
        return false;		
	}
	static String[] ParseDocument(String html,String[] Types) { // Types contains title and headings
		Document doc = Jsoup.parse(html);
		Types[0] = doc.body().getElementsByTag("title").text().toLowerCase();
		Types[1] = doc.body().getElementsByTag("h1").text().toLowerCase();
		Types[2] = doc.body().getElementsByTag("h2").text().toLowerCase();
		Types[3] = doc.body().getElementsByTag("h3").text().toLowerCase();
		Types[4] = doc.body().getElementsByTag("h4").text().toLowerCase();
		Types[5] = doc.body().getElementsByTag("h5").text().toLowerCase();
		Types[6] = doc.body().getElementsByTag("h6").text().toLowerCase();
		Types[7] = doc.body().getElementsByTag("a").text().toLowerCase();
		String content = doc.text().toLowerCase().replaceAll("[-_?!.,;':(){}+*/1234567890]", " ");
		String[] words = content.split("\\s+");
		return words;
	}
	static void printUrls(String html) {
        Document doc = Jsoup.parse(html);

        // Select all anchor elements
        Elements anchorElements = doc.select("a");

        // Iterate over the anchor elements and print the URL and text
        for (Element anchor : anchorElements) {
            String url = anchor.attr("href");
            String text = anchor.text();
            System.out.println("URL: " + url);
            System.out.println("Text: " + text);
        }
	}
	@SuppressWarnings("static-access")
	static void indexDocument(String url) {
		String html = DownloadDocument(url);
		String[] types = new String[8];
		String[] textplain = ParseDocument(html,types);
		//HashSet<String> insertedWords =new HashSet<>();
		for(int i=0;i<textplain.length;i++) {
			if(invertedfile.containsKey(textplain[i])) {
                //obj.InsertPosition(textplain[i], url, i);
				invertedfile.get(textplain[i]).get(url).locations.add(i);
			}
			else {
				String type=FindType(textplain[i],types);
				invertedfile.put(textplain[i],new TreeMap<String,Locations>());
				invertedfile.get(textplain[i]).put(url,new Locations(type));
				invertedfile.get(textplain[i]).get(url).locations.add(i);
				//double tf=obj2.calc_tf(textplain[i], url);
				//double tf_idf=obj2.calc_tfIdf(url, 5000.0, tf);
				//obj.InsertWordinDocument(textplain[i],url,tf_idf,type);
				//obj.InsertPosition(textplain[i], url, i);
			}
		}
	}
	@SuppressWarnings("static-access")
	static void insertIntoDatabase() {
        for (Map.Entry<String, TreeMap<String,Locations>> entry : invertedfile.entrySet()) {
            String word = entry.getKey();
            TreeMap<String,Locations> documents = entry.getValue();
            double df = documents.size();
            for (Map.Entry<String, Locations> entry2 : documents.entrySet()) {
                String url = entry2.getKey();
                Locations locs = entry2.getValue();
                double tf=obj2.calc_tf(word, url);
                double tf_idf=obj2.calc_tfIdf(url, df, tf);
                obj.InsertWordinDocument(word,url,tf_idf,locs.type);
                for (int i=0;i<locs.locations.size();i++) {
                	obj.InsertPosition(word, url, i);
                }
            }
        }
	}
	static String FindType(String word,String[] Types) {
		if(Types[0]!=null &&Types[0].contains(word)) {
			return "title";
		} else if(Types[1]!=null &&Types[1].contains(word)) {
			return "h1"; 
		} else if(Types[2]!=null &&Types[2].contains(word)) {
			return "h2";
		} else if(Types[3]!=null &&Types[3].contains(word)) {
			return "h3";
		} else if(Types[4]!=null &&Types[4].contains(word)) {
			return "h4";
		} else if(Types[5]!=null &&Types[5].contains(word)) {
			return "h5";
		} else if(Types[6]!=null &&Types[6].contains(word)) {
			return "h6";
		} else if(Types[7]!=null &&Types[7].contains(word)) {
			return "hyperlink";
		} else {
			return "textplain";
		}
	}
	static String Stemmer(String word) {
		String stem = word.replaceFirst("ing$", "");
		String stem2=stem.replaceFirst("ed$", "");
		return stem2;
	}
}
class mysqlServer {
	Connection connection;
	mysqlServer(){
		String url ="jdbc:mysql://localhost:3306/mysqlServer";
		String username="root";
		String password="BSA#365#eaif";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(url, username, password);		
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	void CloseConnection() {
		try {
			connection.close();
		}catch (Exception e) {
			
		}
	}
	boolean InsertDocument(String url) {
		String insertQuery = "INSERT INTO URLs (document) VALUES (?)";
		int rowsInserted=0;
		try {
			PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
			insertStatement.setString(1,url);
			rowsInserted = insertStatement.executeUpdate();	
			insertStatement.close();
		} catch (Exception e) {
			return false;
		}
		if (rowsInserted > 0) {
		    return true;
		}
		return false;
	}// insert document to be indexed later	
	boolean InsertWordinDocument(String word,String url,double tf_idf,String type) {
		String insertQuery = "INSERT INTO Documents (word,document,tf_idf,type) VALUES (?,?,?,?)";
		int rowsInserted=0;
		try {
			PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
			insertStatement.setString(1,word);
			insertStatement.setString(2,url);
			insertStatement.setDouble(3,tf_idf);
			insertStatement.setString(4,type);
			rowsInserted = insertStatement.executeUpdate();	
			insertStatement.close();
		} catch (Exception e) {
			return false;
		}
		if (rowsInserted > 0) {
		    return true;
		}
		return false;
	}// insert word with the document and type and tf
	boolean InsertPosition(String word,String url,int position) {
		String insertQuery = "INSERT INTO Positions (word,document,position) VALUES (?,?,?)";
		int rowsInserted=0;
		try {
			PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
			insertStatement.setString(1,word);
			insertStatement.setString(2,url);
			insertStatement.setInt(3,position);
			rowsInserted = insertStatement.executeUpdate();
			insertStatement.close();
		} catch (Exception e) {
			return false;
		}
		if (rowsInserted > 0) {
		    return true;
		}
		return false;
	}// insert position of a word in a document
	boolean UpdateDocument(String url,boolean ind) {
		String updateQuery = "UPDATE URLs SET indexed = ? WHERE document = ?";
		int rowsUpdated = 0;
        try {
    		PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
    		updateStatement.setBoolean(1, ind);
    		updateStatement.setString(2, url);
    		rowsUpdated = updateStatement.executeUpdate();
    		updateStatement.close();
        } catch(Exception e) {
        	return false;
        }

		if (rowsUpdated > 0) {
		    return true;
		}
		return false;
	}// make document indexed or not
	boolean RetrieveDocuments(String word,Vector<doc>vec) {
		String selectQuery = "SELECT document,tf_idf FROM Documents WHERE word = "+word;
        try {
    		Statement selectStatement = connection.createStatement();
    		ResultSet resultSet = selectStatement.executeQuery(selectQuery);
    		while (resultSet.next()) {
    			doc tmp = new doc();
    		    tmp.url=resultSet.getString("document");
    		    tmp.Tf_IDF_total=resultSet.getDouble("tf_idf");
    		    vec.add(tmp);
    		}
    		resultSet.close();
    		selectStatement.close();	
        } catch (Exception e) {
        	return false;
        }
        return true;
	}// retrieve documents where word appears with its type and tf-idf
	boolean LoadDocuments(Vector<String> documents) {
		String selectQuery = "SELECT document FROM URLs WHERE indexed = false";
        try {
    		Statement selectStatement = connection.createStatement();
    		ResultSet resultSet = selectStatement.executeQuery(selectQuery);
    		while (resultSet.next()) {
    		    documents.add(resultSet.getString("document"));
    		}
    		resultSet.close();
    		selectStatement.close();	
        } catch (Exception e) {
        	return false;
        }
        return true;
	}// load non indexed documents
	boolean RetrievePosition(String word,String url,Vector<Integer> positions) {
		String selectQuery = "SELECT position FROM Positions WHERE word = "+word+" AND document = "+url;
        try {
    		Statement selectStatement = connection.createStatement();
    		ResultSet resultSet = selectStatement.executeQuery(selectQuery);
    		while (resultSet.next()) {
    		    positions.add(resultSet.getInt("position"));
    		}
    		resultSet.close();
    		selectStatement.close();	
        } catch (Exception e) {
        	return false;
        }
        return true;
	}// retrieve the positions of the word in a document
	
};
class QueryProcessor{
	mysqlServer obj;
	void OpenConnection(){
		obj = new mysqlServer();
	}
	void CloseConnection(){
		obj.CloseConnection();
	}
	Vector<doc> GetSearchQuery(String[] words ) {
		Vector<doc> result=new Vector<doc>();
		Vector<doc> tmp = new Vector<doc>();
		int i=0;
		while(IsStopWord(words[i])&&i<words.length) {
			i++;
		}
		if(i<words.length) {
			GetDocumentsForWord(words[i],result);
			i++;
			for(;i<words.length;i++) {
				if(!IsStopWord(words[i])) {
					GetDocumentsForWord(words[i].toLowerCase(),tmp);
					result=getcommon(result,tmp);	
				}
			}	
		}
		return result;
	}// it will find documents and send them to ranker 
	void GetDocumentsForWord(String word,Vector<doc> vec) {
		
	    obj.RetrieveDocuments(word.toLowerCase(),vec);
	}
	Vector<doc> getcommon(Vector<doc>first,Vector<doc>second) {
		Vector<doc> result = new Vector<doc>();
		for(int i=0;i<first.size();i++) {
			for(int j=0;j<second.size();j++) {
				if(first.get(i).url==second.get(j).url) {
					doc tmp = new doc();
					tmp.url=first.get(i).url;
					tmp.Tf_IDF_total=first.get(i).Tf_IDF_total+second.get(j).Tf_IDF_total;
					result.add(tmp);
					j=second.size();
				}
			}
		}
		return result;
	}
	public boolean IsStopWord(String word) {
		String[] stopWords1 = { "a", "and", "the","an","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","such","that","their","then","there","these","they","this","to","was","will","with"};
		String[] stopWords2 = {"0o", "0s", "3a", "3b", "3d", "6b", "6o", "a", "a1", "a2", "a3", "a4", "ab", "able", "about", "above", "abst", "ac", "accordance", "according", "accordingly", "across", "act", "actually", "ad", "added", "adj", "ae", "af", "affected", "affecting", "affects", "after", "afterwards", "ag", "again", "against", "ah", "ain", "ain't", "aj", "al", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "announce", "another", "any", "anybody", "anyhow", "anymore", "anyone", "anything", "anyway", "anyways", "anywhere", "ao", "ap", "apart", "apparently", "appear", "appreciate", "appropriate", "approximately", "ar", "are", "aren", "arent", "aren't", "arise", "around", "as", "a's", "aside", "ask", "asking", "associated", "at", "au", "auth", "av", "available", "aw", "away", "awfully", "ax", "ay", "az", "b", "b1", "b2", "b3", "ba", "back", "bc", "bd", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "begin", "beginning", "beginnings", "begins", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "bi", "bill", "biol", "bj", "bk", "bl", "bn", "both", "bottom", "bp", "br", "brief", "briefly", "bs", "bt", "bu", "but", "bx", "by", "c", "c1", "c2", "c3", "ca", "call", "came", "can", "cannot", "cant", "can't", "cause", "causes", "cc", "cd", "ce", "certain", "certainly", "cf", "cg", "ch", "changes", "ci", "cit", "cj", "cl", "clearly", "cm", "c'mon", "cn", "co", "com", "come", "comes", "con", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn", "couldnt", "couldn't", "course", "cp", "cq", "cr", "cry", "cs", "c's", "ct", "cu", "currently", "cv", "cx", "cy", "cz", "d", "d2", "da", "date", "dc", "dd", "de", "definitely", "describe", "described", "despite", "detail", "df", "di", "did", "didn", "didn't", "different", "dj", "dk", "dl", "do", "does", "doesn", "doesn't", "doing", "don", "done", "don't", "down", "downwards", "dp", "dr", "ds", "dt", "du", "due", "during", "dx", "dy", "e", "e2", "e3", "ea", "each", "ec", "ed", "edu", "ee", "ef", "effect", "eg", "ei", "eight", "eighty", "either", "ej", "el", "eleven", "else", "elsewhere", "em", "empty", "en", "end", "ending", "enough", "entirely", "eo", "ep", "eq", "er", "es", "especially", "est", "et", "et-al", "etc", "eu", "ev", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "ey", "f", "f2", "fa", "far", "fc", "few", "ff", "fi", "fifteen", "fifth", "fify", "fill", "find", "fire", "first", "five", "fix", "fj", "fl", "fn", "fo", "followed", "following", "follows", "for", "former", "formerly", "forth", "forty", "found", "four", "fr", "from", "front", "fs", "ft", "fu", "full", "further", "furthermore", "fy", "g", "ga", "gave", "ge", "get", "gets", "getting", "gi", "give", "given", "gives", "giving", "gj", "gl", "go", "goes", "going", "gone", "got", "gotten", "gr", "greetings", "gs", "gy", "h", "h2", "h3", "had", "hadn", "hadn't", "happens", "hardly", "has", "hasn", "hasnt", "hasn't", "have", "haven", "haven't", "having", "he", "hed", "he'd", "he'll", "hello", "help", "hence", "her", "here", "hereafter", "hereby", "herein", "heres", "here's", "hereupon", "hers", "herself", "hes", "he's", "hh", "hi", "hid", "him", "himself", "his", "hither", "hj", "ho", "home", "hopefully", "how", "howbeit", "however", "how's", "hr", "hs", "http", "hu", "hundred", "hy", "i", "i2", "i3", "i4", "i6", "i7", "i8", "ia", "ib", "ibid", "ic", "id", "i'd", "ie", "if", "ig", "ignored", "ih", "ii", "ij", "il", "i'll", "im", "i'm", "immediate", "immediately", "importance", "important", "in", "inasmuch", "inc", "indeed", "index", "indicate", "indicated", "indicates", "information", "inner", "insofar", "instead", "interest", "into", "invention", "inward", "io", "ip", "iq", "ir", "is", "isn", "isn't", "it", "itd", "it'd", "it'll", "its", "it's", "itself", "iv", "i've", "ix", "iy", "iz", "j", "jj", "jr", "js", "jt", "ju", "just", "k", "ke", "keep", "keeps", "kept", "kg", "kj", "km", "know", "known", "knows", "ko", "l", "l2", "la", "largely", "last", "lately", "later", "latter", "latterly", "lb", "lc", "le", "least", "les", "less", "lest", "let", "lets", "let's", "lf", "like", "liked", "likely", "line", "little", "lj", "ll", "ll", "ln", "lo", "look", "looking", "looks", "los", "lr", "ls", "lt", "ltd", "m", "m2", "ma", "made", "mainly", "make", "makes", "many", "may", "maybe", "me", "mean", "means", "meantime", "meanwhile", "merely", "mg", "might", "mightn", "mightn't", "mill", "million", "mine", "miss", "ml", "mn", "mo", "more", "moreover", "most", "mostly", "move", "mr", "mrs", "ms", "mt", "mu", "much", "mug", "must", "mustn", "mustn't", "my", "myself", "n", "n2", "na", "name", "namely", "nay", "nc", "nd", "ne", "near", "nearly", "necessarily", "necessary", "need", "needn", "needn't", "needs", "neither", "never", "nevertheless", "new", "next", "ng", "ni", "nine", "ninety", "nj", "nl", "nn", "no", "nobody", "non", "none", "nonetheless", "noone", "nor", "normally", "nos", "not", "noted", "nothing", "novel", "now", "nowhere", "nr", "ns", "nt", "ny", "o", "oa", "ob", "obtain", "obtained", "obviously", "oc", "od", "of", "off", "often", "og", "oh", "oi", "oj", "ok", "okay", "ol", "old", "om", "omitted", "on", "once", "one", "ones", "only", "onto", "oo", "op", "oq", "or", "ord", "os", "ot", "other", "others", "otherwise", "ou", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "ow", "owing", "own", "ox", "oz", "p", "p1", "p2", "p3", "page", "pagecount", "pages", "par", "part", "particular", "particularly", "pas", "past", "pc", "pd", "pe", "per", "perhaps", "pf", "ph", "pi", "pj", "pk", "pl", "placed", "please", "plus", "pm", "pn", "po", "poorly", "possible", "possibly", "potentially", "pp", "pq", "pr", "predominantly", "present", "presumably", "previously", "primarily", "probably", "promptly", "proud", "provides", "ps", "pt", "pu", "put", "py", "q", "qj", "qu", "que", "quickly", "quite", "qv", "r", "r2", "ra", "ran", "rather", "rc", "rd", "re", "readily", "really", "reasonably", "recent", "recently", "ref", "refs", "regarding", "regardless", "regards", "related", "relatively", "research", "research-articl", "respectively", "resulted", "resulting", "results", "rf", "rh", "ri", "right", "rj", "rl", "rm", "rn", "ro", "rq", "rr", "rs", "rt", "ru", "run", "rv", "ry", "s", "s2", "sa", "said", "same", "saw", "say", "saying", "says", "sc", "sd", "se", "sec", "second", "secondly", "section", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "sf", "shall", "shan", "shan't", "she", "shed", "she'd", "she'll", "shes", "she's", "should", "shouldn", "shouldn't", "should've", "show", "showed", "shown", "showns", "shows", "si", "side", "significant", "significantly", "similar", "similarly", "since", "sincere", "six", "sixty", "sj", "sl", "slightly", "sm", "sn", "so", "some", "somebody", "somehow", "someone", "somethan", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "sp", "specifically", "specified", "specify", "specifying", "sq", "sr", "ss", "st", "still", "stop", "strongly", "sub", "substantially", "successfully", "such", "sufficiently", "suggest", "sup", "sure", "sy", "system", "sz", "t", "t1", "t2", "t3", "take", "taken", "taking", "tb", "tc", "td", "te", "tell", "ten", "tends", "tf", "th", "than", "thank", "thanks", "thanx", "that", "that'll", "thats", "that's", "that've", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "thered", "therefore", "therein", "there'll", "thereof", "therere", "theres", "there's", "thereto", "thereupon", "there've", "these", "they", "theyd", "they'd", "they'll", "theyre", "they're", "they've", "thickv", "thin", "think", "third", "this", "thorough", "thoroughly", "those", "thou", "though", "thoughh", "thousand", "three", "throug", "through", "throughout", "thru", "thus", "ti", "til", "tip", "tj", "tl", "tm", "tn", "to", "together", "too", "took", "top", "toward", "towards", "tp", "tq", "tr", "tried", "tries", "truly", "try", "trying", "ts", "t's", "tt", "tv", "twelve", "twenty", "twice", "two", "tx", "u", "u201d", "ue", "ui", "uj", "uk", "um", "un", "under", "unfortunately", "unless", "unlike", "unlikely", "until", "unto", "uo", "up", "upon", "ups", "ur", "us", "use", "used", "useful", "usefully", "usefulness", "uses", "using", "usually", "ut", "v", "va", "value", "various", "vd", "ve", "ve", "very", "via", "viz", "vj", "vo", "vol", "vols", "volumtype", "vq", "vs", "vt", "vu", "w", "wa", "want", "wants", "was", "wasn", "wasnt", "wasn't", "way", "we", "wed", "we'd", "welcome", "well", "we'll", "well-b", "went", "were", "we're", "weren", "werent", "weren't", "we've", "what", "whatever", "what'll", "whats", "what's", "when", "whence", "whenever", "when's", "where", "whereafter", "whereas", "whereby", "wherein", "wheres", "where's", "whereupon", "wherever", "whether", "which", "while", "whim", "whither", "who", "whod", "whoever", "whole", "who'll", "whom", "whomever", "whos", "who's", "whose", "why", "why's", "wi", "widely", "will", "willing", "wish", "with", "within", "without", "wo", "won", "wonder", "wont", "won't", "words", "world", "would", "wouldn", "wouldnt", "wouldn't", "www", "x", "x1", "x2", "x3", "xf", "xi", "xj", "xk", "xl", "xn", "xo", "xs", "xt", "xv", "xx", "y", "y2", "yes", "yet", "yj", "yl", "you", "youd", "you'd", "you'll", "your", "youre", "you're", "yours", "yourself", "yourselves", "you've", "yr", "ys", "yt", "z", "zero", "zi", "zz"};
		HashSet<String> stopWordsSet1 = new HashSet<>(Arrays.asList(stopWords1));
		HashSet<String> stopWordsSet2 = new HashSet<>(Arrays.asList(stopWords2));
		if (stopWordsSet1.contains(word.toLowerCase())||stopWordsSet2.contains(word.toLowerCase())){
			return true;
		}
        return false;		
	}
};