import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class SiteMap extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    private String url;
    private static Set<String> urlList = ConcurrentHashMap.newKeySet();

    public SiteMap(String url) {
        this.url = url;
    }

    public static Set<String> getUrlList() {
        return urlList;
    }

    @Override
    protected void compute() {
        List<SiteMap> subTasks = new LinkedList<>();
        try {
            Connection.Response response = Jsoup.connect(url).userAgent("Mozilla").execute();
            if (response.statusCode() == 200) {
                Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                List<String> links = doc.select("a")
                        .stream()
                        .filter(element -> {
                            String el = element.attr("abs:href");
                            return !urlList.contains(el) && el.matches("https://skillbox.ru(.+[^/])?/$");
                        })
                        .map(element -> element.attr("abs:href"))
                        .collect(Collectors.toList());

                if (links.size() > 1) {

                    for (String urlLink : links) {
                        urlList.add(urlLink);
                        SiteMap submap = new SiteMap(urlLink);
                        subTasks.add(submap);
                    }
                } else if (links.size() > 0) {
                    urlList.add(links.get(0));
                }

                ForkJoinTask.invokeAll(subTasks);

            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage() + " IO " + url);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " " + url);
        }
    }
}
