import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class MapOfTheSite extends RecursiveTask<Set<String>> {
    private String url;
    private Set<String> urlList = new LinkedHashSet<>();


    public MapOfTheSite(String url){
        this.url = url;
    }

    public Set<String> getUrlList() {
        return urlList;
    }

    @Override
    protected Set<String> compute() {
        List<MapOfTheSite> subTasks = new LinkedList<>();
        try {
            Connection.Response response = Jsoup.connect(url).userAgent("Mozilla").execute();
            if (response.statusCode() == 200) {
                Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Elements links = doc.select("a");
                if (links.isEmpty()) {
                    return null;
                }

                links.stream().map((link) -> link.attr("abs:href")).forEachOrdered((this_url) -> {
                    boolean add = false;
                    if (this_url.matches("https://skillbox.ru(.+[^/])?/$")) {
                        add = urlList.add(this_url);
                    }
                    if (add && this_url.contains(Main.BASE_URL)) {
                        try {
                            MapOfTheSite task = new MapOfTheSite(this_url);
                            task.fork();
                            subTasks.add(task);
                        } catch (Exception ex){
                            System.out.println(ex.getMessage() + " " + url);
                        }
                    }
                });

                subTasks.forEach(task -> urlList.addAll(task.join()));

            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage() + " IO " + url);
        } catch (Exception ex){
            System.out.println(ex.getMessage() + " " + url);
        }
        return urlList;
    }
}
