/**
 * Created by pritom on 4/20/2015.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotExclusionUtil {
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> map
            = new ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>>();

    /**
     * This method based on code in the WWW::RobotRules module in the
     * libwww-perl5 library, available from www.cpan.org.
     *
     * @param robotsFile
     *            The robots.txt file represented as a string. Based on public
     *            code made available by:
     * @author Ted Wild & Ray Mooney
     */
    private static void parseRobotsFileString(String site, String robotsFile) {
        int currentIndex = 0;
        // Regex Pattern matchers for finding user-agent, disallow, and blank
        // lines in file
        Matcher userAgentLine = Pattern.compile("(?i)User-Agent:\\s*(.*)")
                .matcher(robotsFile);
        Matcher disallowLine = Pattern.compile("(?i)Disallow:\\s*(.*)")
                .matcher(robotsFile);
        Matcher blankLine = Pattern.compile("\n\\s*\n").matcher(robotsFile);


        ConcurrentHashMap<String, Boolean> m = map.get(site);
        if (m == null) {
            m = new ConcurrentHashMap<String, Boolean>();
            map.put(site, m);
        }
        // Find each user-agent portion of file
        while (userAgentLine.find()) {
            if (userAgentLine.group(1).indexOf('*') != -1) {
                // this User-Agent line applies to this robot
                // find next blank line after this user-agent line
                currentIndex = userAgentLine.end();
                blankLine.region(currentIndex, robotsFile.length());
                // Index of next blank line
                int blankLineIndex = robotsFile.length();
                if (blankLine.find())
                    blankLineIndex = blankLine.start();
                // Find disallow lines before next blank line (or end of file)
                disallowLine.region(currentIndex, blankLineIndex);
                while (disallowLine.find()) {
                    // For each disallow line, add its path to the disallowed
                    // set
                    String disallowed = disallowLine.group(1).trim();
                    if (disallowed.length() > 0) {
                        if (disallowed.endsWith("/"))
                            disallowed = disallowed.substring(0,
                                    disallowed.lastIndexOf('/'));
                    }
                    //System.out.println("Disallowed: " + disallowed);
                    m.put(disallowed, true);
                }
            }
        }
    }

    public static boolean robotsShouldFollow(String url) {
        try {
            URL u = url(url);
            String site = u.getHost();
            //System.out.println("Site: " + site);
            if (! map.containsKey(site)) {
                String robotText = readRobotsFile(url("http://" + site + "/robots.txt"));
                if (robotText != null) {
                    //System.out.println(robotText);
                    parseRobotsFileString(site, robotText);
                }
            }

            ConcurrentHashMap<String, Boolean> m = map.get(site);
            if (m == null) {
                return true;
            } else {
                //System.out.println("u.getPath(): " + u.getPath());
                String path = u.getPath();
                if (path != null && path.length() > 0 && path.endsWith("/"))
                    path = path.substring(0, path.lastIndexOf('/'));
                return ! m.containsKey(path);
            }

        } catch (MalformedURLException e) {
            //System.out.println("WebPage.getWebPage(): " + e.toString());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return true;
    }

    private static URL url(String url) throws MalformedURLException {
        return new URL(url);
    }

    public static String readRobotsFile(URL urlObj) throws IOException {

        // using a StringBuffer instead of a String has huge
        // performance benefits.
        StringBuffer page = new StringBuffer();

        // Open an input stream of the URL contents
        BufferedReader x = new BufferedReader(new InputStreamReader(urlObj
                .openConnection().getInputStream()));

        // wait for sometime for buffer to be initialized
		/*
		int c = 0;
		while (!x.ready() || c < 10000) {
			c++;
		}
		*/

        // make up a name where you would store the
        // open a file to write in
        String line = "";
        while ((line=x.readLine())!=null) {
            page.append(line + "\n");
        }

        x.close();

        return page.toString();
    }

    /**
     * For testing only. Parses robosts.txt file for a particular site
     */
    public static void main(String[] args) {
        System.out.println(robotsShouldFollow("http://academicpersonnel.ucr.edu/employment/"));
    }
}
