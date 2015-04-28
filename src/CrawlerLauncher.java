import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pritom on 4/20/2015.
 */
public class CrawlerLauncher {

    public static void main(String[] args) throws IOException {
        // #0 - SeedFile with full path
        // #1 - number of pages to crawl
        // #2 - output directory
        try (BufferedReader br = new BufferedReader(new FileReader(new File(args[0])))) {
            String line;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            //get current date time with Date()
            Date date = new Date();
            System.out.println("start time : " + dateFormat.format(date));
            while ((line = br.readLine()) != null) {
                Thread thread = new Crawler(line, Integer.parseInt(args[1]), args[2]);
                thread.start();
            }

            /*Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("----------------" + Thread.activeCount());
                }
            }, 45000);*/

        }
    }
}
