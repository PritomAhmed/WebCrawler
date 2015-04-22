/**
 * Created by pritom on 4/19/2015.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Crawler extends Thread {

    String startUrl;
    static final int MAX_PAGES_TO_CRAWL = 100000;
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

            pagesToBeVisited.addAll(worker.getLinks());
        }
        System.out.println(String.format("**Done** Visited %s web page(s)", pagesVisited.size()));
    }

    private String nextUrl() {
        String nextUrl;
        do {
            /*Todo: Can be made random*/
            nextUrl = pagesToBeVisited.remove(0);
        } while(pagesVisited.contains(nextUrl));
        pagesVisited.add(nextUrl);
        return nextUrl;
    }
}
