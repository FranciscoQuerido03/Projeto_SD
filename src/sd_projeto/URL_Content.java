package sd_projeto;

import java.io.Serializable;

public class URL_Content implements Serializable {
    public String title;
    public String url;
    public String Pub_date;

    public int priority;

    public URL_Content(String title, String url, String Pub_date) {
        this.title = title;
        this.url = url;
        this.Pub_date = Pub_date;
    }

    public void addUrl(URL_Content u) {
        this.title = u.title;
        this.url = u.url;
        this.Pub_date = u.Pub_date;
    }

    public boolean hasValues() {
        return this.url != null;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n" +
            "URL: " + url + "\n" +
            "Prio: " + priority  + "\n" +
            "Publication Date: " + Pub_date + "\n\n";
    }

}
