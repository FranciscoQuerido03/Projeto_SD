package sd_projeto;

import java.io.Serializable;

/**
 * Classe que representa o conteúdo de um URL.
 */
public class URL_Content implements Serializable {
    public String title; // Título da URL
    public String url; // URL
    public String Pub_date; // Data de publicação

    public int priority;    // Prioridade para cliente

    /**
     * Construtor da classe `URL_Content`.
     * @param title O título do URL.
     * @param url O URL.
     * @param Pub_date A data de publicação.
     */
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
