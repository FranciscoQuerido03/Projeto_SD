package sd_projeto;

import java.io.Serializable;

/**
 * Classe que representa o conteúdo de um URL.
 */
public class URL_Content implements Serializable {
    public String title; // Título da URL
    public String url; // URL
    public String citacao; // Citação

    public int priority;    // Prioridade para cliente

    /**
     * Construtor da classe `URL_Content`.
     * @param title O título do URL.
     * @param url O URL.
     */
    public URL_Content(String title, String url) {
        this.title = title;
        this.url = url;
    }


    /**
     * Método que adiciona uma citação ao conteúdo do URL.
     * @param s A citação a ser adicionada.
     */
    public void add_citacao(String s) {
        this.citacao = s;
    }


    public void addUrl(URL_Content u) {
        this.title = u.title;
        this.url = u.url;
    }


    public boolean hasValues() {
        return this.url != null;
    }

    /**
     * Retorna uma representação do conteúdo do URL como uma string.
     * @return Uma string que representa o conteúdo do URL.
     */
    @Override
    public String toString() {
        return "Title: " + title + "\n" +
            "URL: " + url + "\n" +
            citacao + "\n";
            //"Prio: " + priority  + "\n\n";
    }

}