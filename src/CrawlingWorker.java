import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.CRC32;

/**
 * Created by pritom on 4/19/2015.
 */
public class CrawlingWorker {
    private static Random random = new Random();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) " +
            "Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();
    static Set<Long> crclib = new ConcurrentSkipListSet<>();

    CRC32 crc = new CRC32();

    public List<String> getLinks() {
        return links;
    }

    public void crawl(String currentUrl) throws IOException {
        try {
            if (RobotExclusionUtil.robotsShouldFollow(currentUrl)) {
                Crawler.pagesVisited.add(currentUrl);
                long startTime = System.currentTimeMillis();

                Connection connection = Jsoup.connect(currentUrl).userAgent(USER_AGENT);
                Document htmlDocument = connection.get();
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                if (htmlDocument.text() != null) {
                    startTime = System.currentTimeMillis();
                    writeToFile(htmlDocument);
                    endTime = System.currentTimeMillis();
//                    System.out.println(currentUrl + " time elapsed in connection "
//                            + totalTime + " time elapsed in writing " + (endTime - startTime)
//                            + " " + Thread.currentThread().getName());

                    Elements linksOnPage = htmlDocument.select("a[href]");
                    //System.out.println("Found (" + linksOnPage.size() + ") links" + " Thread " + Thread.currentThread().getName());
                    for (Element link : linksOnPage) {
                        this.links.add(link.absUrl("href"));
                    }
                }
            }
        } catch (IOException ioe) {
//            System.out.println("Error in out HTTP request " + ioe);
        }

    }

    private void writeToFile(Document htmlDocument) throws IOException {
        //String title = "".equals(htmlDocument.title().trim()) ? htmlDocument.head().text().replaceAll("\\W+", "")
        //        : htmlDocument.title().trim().replaceAll("\\W+", "");
        File file = new File("output/" + random.nextLong() + ".txt");

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        } else {
            System.out.println("********************************* " + file.getName());
        }

        crc.update(htmlDocument.text().getBytes());
        long cval = crc.getValue();
        crc.reset();
        if (!crclib.contains(cval)) {
            crclib.add(cval);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(htmlDocument.title());
            bw.newLine();
            bw.append(htmlDocument.text());
            bw.newLine();
            bw.close();
        }
    }
}
