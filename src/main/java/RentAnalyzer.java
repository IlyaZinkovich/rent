import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class RentAnalyzer {

    private static final String delimiter = "-";
    private static final String endline = "\n";
    private static final String flatsSelector = "#search-filter-results > div.classifieds > div > div.classifieds-list > a > " +
            "span.classified__caption > span.classified__caption-item.classified__caption-item_adress";
    private static final String FLATS_PATH = "flats.txt";
    private static final String CHROME_DRIVER_PATH = "C:/tools/chromedriver.exe";
    private static final int TIMEOUT = 300000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        while (true) {
            WebDriver webDriver = new ChromeDriver();
            Map<String, Set<String>> lastFlats = readLastFlats();

            PrintWriter printWriter = new PrintWriter(new File(FLATS_PATH));

            Set<String> newFlats = lastFlats.keySet().stream()
                    .flatMap(url -> compareWithLatestFlats(webDriver, lastFlats, printWriter, url))
                    .collect(toSet());
            webDriver.close();
            printWriter.flush();
            printWriter.close();
            newFlats.forEach(System.err::println);
            Thread.sleep(TIMEOUT);
        }
    }

    private static Stream<String> compareWithLatestFlats(WebDriver webDriver, Map<String, Set<String>> lastFlats, PrintWriter printWriter, String url) {
        webDriver.navigate().to(url);
        webDriver.navigate().refresh();
        List<WebElement> links = webDriver.findElements(By.cssSelector(flatsSelector));
        Set<String> flats = links.stream().map(WebElement::getText).collect(toSet());
        HashSet<String> buffer = new HashSet<>(flats);
        buffer.removeAll(lastFlats.get(url));
        printWriter.write(url);
        printWriter.write(endline);
        flats.stream().map(flat -> flat + endline).forEach(printWriter::write);
        printWriter.write(delimiter);
        printWriter.write(endline);
        return buffer.stream();
    }

    private static Map<String, Set<String>> readLastFlats() throws FileNotFoundException {
        Map<String, Set<String>> flatsByLocation = new HashMap<>();
        Scanner scanner = new Scanner(new File(FLATS_PATH));
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (delimiter.equals(line)) break;
            String url = line;
            Set<String> flats = new HashSet<>();
            while (!delimiter.equals(line)) {
                if (scanner.hasNext()) {
                    line = scanner.nextLine();
                    flats.add(line);
                } else {
                    break;
                }
            }
            flatsByLocation.put(url, flats);
        }
        return flatsByLocation;
    }
}
