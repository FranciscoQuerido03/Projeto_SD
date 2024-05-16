package sd_projeto;

import java.io.*;
import java.util.List;

/**
 * Classe que representa uma lista de URLs.
 */
public class Urls_list implements Serializable {
    private List<String> Vals; // Lista de URLs

    /**
     * Construtor da classe `Urls_list`.
     * @param Vals A lista de URLs.
     */
    public Urls_list(List<String> Vals) {
        this.Vals = Vals;
    }

    /**
     * Adiciona uma nova URL à lista.
     * @param value A URL a ser adicionada.
     */
    public void addUrl(String value) {
        Vals.add(value);
    }

    /**
     * Verifica se a lista de URLs está vazia.
     * @return true se a lista de URLs não estiver vazia, false caso contrário.
     */
    public boolean hasValues() {
        return Vals != null && !Vals.isEmpty();
    }

    /**
     * Retorna uma representaçãov da lista de URLs como string.
     * @return Uma string com os URLs formatados com os seus índices.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Vals.size(); i++) {
            sb.append(i + 1).append(" -> ").append(Vals.get(i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Retorna uma representação da lista de URLs como string.
     * @return Uma string com os URLs.
     */
    public String wordtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Vals);
        return sb.toString();
    }
}
