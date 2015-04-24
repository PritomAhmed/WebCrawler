/**
 * Created by pritom on 4/19/2015.
 */

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.helper.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Crawler extends Thread {

    String startUrl;
    static final int MAX_PAGES_TO_CRAWL = 10;
    static Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    List<String> pagesToBeVisited = new ArrayList<>();

    public Crawler(String startUrl) {
        this.startUrl = startUrl;
    }

    public void run() {
        while(pagesVisited.size() < MAX_PAGES_TO_CRAWL) {
            String currentUrl;
            CrawlingWorker worker = new CrawlingWorker();
            if (pagesToBeVisited.isEmpty()) {
                currentUrl = startUrl;
                pagesVisited.add(startUrl);
            } else {
                currentUrl = this.nextUrl();
            }
            try {
                worker.crawl(currentUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addUrls(worker.getLinks());
        }
        System.out.println(String.format("**Done** Visited %s web page(s)", pagesVisited.size()));
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

    private String nextUrl() {
        String nextUrl;
        do {
            /*Todo: Can be made random*/
            nextUrl = pagesToBeVisited.remove(0);
        } while(pagesVisited.contains(nextUrl));

        return nextUrl;
    }
}
