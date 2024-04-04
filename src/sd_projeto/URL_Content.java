package sd_projeto;

/**
 * Classe que representa o conteúdo de um URL.
 */
public class URL_Content {
    public String title; // Título da URL
    public String url; // URL
    public String Pub_date; // Data de publicação

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
}
