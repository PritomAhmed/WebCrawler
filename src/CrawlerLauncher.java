import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by pritom on 4/20/2015.
 */
public class CrawlerLauncher {

    public static void main(String[] args) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("seedlist.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                Thread thread = new Crawler(line);
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
