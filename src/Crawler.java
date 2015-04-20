/**
 * Created by pritom on 4/19/2015.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Crawler {
    public static void main(String[] args) throws IOException {
        Set<String> pagesVisited = new HashSet<String>();
        List<String> pagesToBeVisited = new LinkedList<String>();
        Document doc = Jsoup.connect("http://*.edu/").get();
        String title = doc.title();
    }

    public static void processPage(String URL) throws IOException, IOException {
        //check if the given URL is already in database
        String sql = "select * from Record where URL = '" + URL + "'";

        //store the URL to database to avoid parsing again
        sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";

        //get useful information
        Document doc = Jsoup.connect("http://www.mit.edu/").get();

        if (doc.text().contains("research")) {
            System.out.println(URL);
        }

        //get all links and recursively call the processPage method
        Elements questions = doc.select("a[href]");
        for (Element link : questions) {
            if (link.attr("href").contains("mit.edu"))
                processPage(link.attr("abs:href"));
        }
    }

}
