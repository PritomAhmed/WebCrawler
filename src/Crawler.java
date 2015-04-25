/**
 * Created by pritom on 4/19/2015.
 */

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.CRC32;

public class Crawler extends Thread {

    String startUrl;
    static final int MAX_PAGES_TO_CRAWL = 10000000;
    static Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    List<String> pagesToBeVisited = new ArrayList<>();

    private static Random random = new Random();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) " +
            "Chrome/13.0.782.112 Safari/535.1";
    List<String> links = new ArrayList<>();
    static Set<Long> crclib = new ConcurrentSkipListSet<>();

    public Crawler(String startUrl) {
        this.startUrl = startUrl;
    }

    public void run() {
        try {
            while (pagesVisited.size() < MAX_PAGES_TO_CRAWL) {
                String currentUrl;
                if (pagesToBeVisited.isEmpty()) {
                    currentUrl = startUrl;
                    pagesVisited.add(startUrl);
                } else {
                    currentUrl = this.nextUrl();
                }
                try {
                    crawl(currentUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (pagesToBeVisited.size() < 1000 || pagesToBeVisited.size() + links.size() < 100000) {
                    addUrls(links);
                }
            }
            System.out.println(String.format("**Done** Visited %s web page(s)", pagesVisited.size()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println(e.getMessage() + " " + e.getLocalizedMessage()
                    + " size : " + pagesToBeVisited.size()
                    + " Threads alive : " + Thread.activeCount());
        }
    }

    private void addUrls(List<String> links) {
        UrlValidator urlValidator = new UrlValidator();
        for (String link : links) {
            if (!StringUtil.isBlank(link)
                    && urlValidator.isValid(link)
                    && !link.contains("#")) {
                pagesToBeVisited.add(link);
            }
        }
    }

    CRC32 crc = new CRC32();

    public void crawl(String currentUrl) throws IOException {
        try {
            if (RobotExclusionUtil.robotsShouldFollow(currentUrl)) {
                pagesVisited.add(currentUrl);
                long startTime = System.currentTimeMillis();

                Document htmlDocument = null;
                try {
                    htmlDocument = Jsoup.connect(currentUrl).userAgent(USER_AGENT).get();
                } catch (OutOfMemoryError ignored) {
                    ignored.printStackTrace();
                    System.out.println(currentUrl);
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                if (htmlDocument != null && htmlDocument.text() != null) {
                    startTime = System.currentTimeMillis();
                    crc.update(htmlDocument.text().getBytes());
                    long cval = crc.getValue();
                    crc.reset();
                    if (!crclib.contains(cval)) {
                        crclib.add(cval);
                        writeToFile(htmlDocument);
                    }
                    endTime = System.currentTimeMillis();
//                    System.out.println(currentUrl + " time elapsed in connection "
//                            + totalTime + " time elapsed in writing " + (endTime - startTime)
//                            + " " + Thread.currentThread().getName());

                    Elements linksOnPage = htmlDocument.select("a[href]");
                    //System.out.println("Found (" + linksOnPage.size() + ") links" + " Thread " + Thread.currentThread().getName());
                    links.clear();
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
        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.append(htmlDocument.title());
        bw.newLine();
        bw.append(htmlDocument.text());
        bw.newLine();
        bw.close();
        fw.close();
    }

    private String nextUrl() {
        String nextUrl;
        do {
            /*Todo: Can be made random*/
            nextUrl = pagesToBeVisited.remove(0);
        } while (pagesVisited.contains(nextUrl));

        return nextUrl;
    }
}
