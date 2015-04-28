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
    static int MAX_PAGES_TO_CRAWL; // = 10000000;
    static String output_dir;
    static Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    List<String> pagesToBeVisited = new ArrayList<>();
    private static Random random = new Random();
    CRC32 crc = new CRC32();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) " +
            "Chrome/13.0.782.112 Safari/535.1";
    List<String> links = new ArrayList<>();
    static Set<Long> crclib = new ConcurrentSkipListSet<>();

    public Crawler(String startUrl, int max_page_number, String outputDirectory ) {
        this.startUrl = startUrl;
        MAX_PAGES_TO_CRAWL = max_page_number;
        output_dir = outputDirectory;
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
                    && link.contains(".edu")) {
                pagesToBeVisited.add(link);
            }
        }
    }

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
                        writeToFile(currentUrl, htmlDocument);
                    }
                    endTime = System.currentTimeMillis();
//                    System.out.println(currentUrl + " time elapsed in connection "
//                            + totalTime + " time elapsed in writing " + (endTime - startTime)
//                            + " " + Thread.currentThread().getName());

                    //System.out.println("Found (" + linksOnPage.size() + ") links" + " Thread " + Thread.currentThread().getName());
                }
            }
        } catch (IOException ioe) {
//            System.out.println("Error in out HTTP request " + ioe);
        }

    }

    private void writeToFile(String url, Document htmlDocument) throws IOException {
        //String title = "".equals(htmlDocument.title().trim()) ? htmlDocument.head().text().replaceAll("\\W+", "")
        //        : htmlDocument.title().trim().replaceAll("\\W+", "");

        //File file = new File("output/" + random.nextLong() + ".txt");
        File file = new File(Crawler.output_dir +"/" + random.nextLong() + ".txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        } else {
            System.out.println("********************************* " + file.getName());
        }

        //Get description from document object.
        String description = "";

        Elements linksOnPage = htmlDocument.select("a[href]");
        links.clear();
        for (Element link : linksOnPage) {
            this.links.add(link.absUrl("href"));
        }

        StringBuilder builder = new StringBuilder();
        for (String link : links) {
            builder.append(link).append(System.getProperty("line.separator"));
        }

        if (htmlDocument.select("meta[name=description]") != null && htmlDocument.select("meta[name=description]").size() > 0) {
            description = htmlDocument.select("meta[name=description]").get(0).attr("content");
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.append(htmlDocument.title());
        bw.newLine();
        bw.append("Description : ").append(description);
        bw.newLine();
        bw.append("URL : ").append(url);
        bw.newLine();
        bw.append("Links : ");
        bw.newLine();
        bw.append(builder.toString());
        bw.append("Text : ");
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
