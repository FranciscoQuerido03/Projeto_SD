package com.example.demo.sd_projeto;

import java.util.*;

/**
 * Classe que mantém o controlo das pesquisas mais frequentes.
 * Mantem um HashMap para contabilizar as pesquisas e uma PriorityQueue para saber quais as pesquisas mais frequentes.
 */
public class TopSearches {
    private Map<String, Integer> searchCounts;
    private PriorityQueue<Map.Entry<String, Integer>> pq;

    /**
     * Construtor da classe 'TopSearches'.
     * Inicializa o HashMap e a PriorityQueue.
     */
    public TopSearches() {
        searchCounts = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
    }

    /**
     * Atualiza a contagem de pesquisas para um termo de pesquisa específico.
     * Se o termo de pesquisa já estiver no mapa é incrementada em 1 a sua contagem.
     * Se não, o termo é adicionado ao mapa com uma contagem de 1.
     * @param searchTerm O termo de pesquisa a ser atualizado.
     */
    public void updateSearchCount(String searchTerm) {
        searchCounts.put(searchTerm, searchCounts.getOrDefault(searchTerm, 0) + 1);
    }

    /**
     * Obtém as 10 principais pesquisas com base nas contagens.
     * @return Uma string com as 10 principais pesquisas formatadas como `[posição] => termo de pesquisa: contagem`.
     */
    public String getTop10() {
        StringBuilder s = new StringBuilder();

        for (Map.Entry<String, Integer> entry : searchCounts.entrySet()) {
            pq.offer(entry);
        }

        int n = 10;
        for (int i = 0; i < n && !pq.isEmpty(); i++) {
            Map.Entry<String, Integer> entry = pq.poll();
            s.append("[" + (i + 1) + "] => " + entry.getKey() + ": " + entry.getValue() + "\n");
        }

        return s.toString();
    }
}
