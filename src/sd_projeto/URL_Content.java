package sd_projeto;

import java.io.Serializable;

/**
 * Classe que representa o conteúdo de um URL.
 */
public class URL_Content implements Serializable {
    public String title; // Título da URL
    public String url; // URL

    public int priority;    // Prioridade para cliente

    /**
     * Construtor da classe `URL_Content`.
     * @param title O título do URL.
     * @param url O URL.
     * @param Pub_date A data de publicação.
     */
    public URL_Content(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public void addUrl(URL_Content u) {
        this.title = u.title;
        this.url = u.url;
    }

    public boolean hasValues() {
        return this.url != null;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n" +
            "URL: " + url + "\n" ;
            //"Prio: " + priority  + "\n\n";
    }

}
