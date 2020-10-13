import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static final String BASE_URL = "https://skillbox.ru/";
    private static final String FILE_PATH = "data/file.txt";
    public static Set<String> urlList = new TreeSet<>();

    public static void main(String[] args) throws InterruptedException {

        SiteMap siteMap = new SiteMap(BASE_URL);
        ForkJoinPool pool = new ForkJoinPool();
        long start = System.currentTimeMillis();
        pool.invoke(siteMap);
        System.out.println("Duration of task " + (System.currentTimeMillis() - start));
        Set<String> result = new TreeSet<>(SiteMap.getUrlList());
        saveFile(result, FILE_PATH);
        System.out.println("End");
    }

    private static void getDataFromFile(String filePath) {
        try {
            List<String> links = Files.readAllLines(Paths.get(filePath));
            makeMap(links);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void makeMap(List<String> links) {
        List<String> newList = new ArrayList<>();
        for (String item : links) {
            item = item.replaceAll("\t", "");
            int count = item.split("/").length;
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < count - 3; i++) {
                str.append("\t");
            }
            str.append(item);
            newList.add(str.toString());
        }
        newList.forEach(System.out::println);
    }


    private static void saveFile(Set<String> data, String path) {
        try {
            if (!Files.exists(Paths.get("data/"))) {
                Files.createDirectory(Paths.get("data/"));
                System.out.println("Файл создан");
            }
            StringBuilder fileString = new StringBuilder();
            data.forEach(item -> {
                int count = item.split("/").length;
                for (int i = 0; i < count - 3; i++) {
                    fileString.append("\t");
                }
                fileString.append(item).append("\n");
            });
            Files.writeString(Paths.get(path), fileString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getLinks(String url) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
            Elements links = doc.select("a");

            if (links.isEmpty()) {
                return;
            }

            links.stream().map((link) -> link.attr("abs:href")).forEachOrdered((this_url) -> {
                boolean add = false;
                if (this_url.matches("https://skillbox.ru/(.+[^/])?/$")) {
                    add = urlList.add(this_url);
                }
                if (add && this_url.contains(BASE_URL)) {
                    getLinks(this_url);
                }
            });

        } catch (IOException ex) {
            System.err.println(ex.getMessage() + " '" + url);
        }

    }

    private void getLinksThread(String url) {
        try {
            Connection.Response response = Jsoup.connect(url).userAgent("Mozilla").execute();
            if (response.statusCode() == 200) {
                Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Elements links = doc.select("a");

                if (links.isEmpty()) {
                    return;
                }

                links.stream().map((link) -> link.attr("abs:href")).forEachOrdered((this_url) -> {
                    boolean add = false;
                    if (this_url.matches("https://skillbox.ru/(.+[^/])?/$")) {
                        add = urlList.add(this_url);
                    }
                    if (add && this_url.contains(Main.BASE_URL)) {
                        try {
                            getLinksThread(this_url);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage() + " " + url);
                        }
                    }
                });
            } else {
                return;
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage() + " IO " + url);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " " + url);
        }
    }
}
