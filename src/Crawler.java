/**
 * Created by pritom on 4/19/2015.
 */

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Crawler extends Thread {

    String startUrl;
    static final int MAX_PAGES_TO_CRAWL = 100000;
    Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    ConcurrentLinkedQueue<String> pagesToBeVisited = new ConcurrentLinkedQueue<>();

    public Crawler(String startUrl) {
        this.startUrl = startUrl;
    }

    public void run() {
        while(this.pagesVisited.size() < MAX_PAGES_TO_CRAWL) {
            String currentUrl;
            CrawlingWorker worker = new CrawlingWorker();
            if (this.pagesToBeVisited.isEmpty()) {
                currentUrl = startUrl;
                this.pagesVisited.add(startUrl);
            } else {
                currentUrl = this.nextUrl();
            }
            try {
                worker.crawl(currentUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.pagesToBeVisited.addAll(worker.getLinks());
        }
        System.out.println(String.format("**Done** Visited %s web page(s)", this.pagesVisited.size()));
    }

    private String nextUrl() {
        String nextUrl;
        do {
            nextUrl = this.pagesToBeVisited.poll();
        } while(this.pagesVisited.contains(nextUrl));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }
}
