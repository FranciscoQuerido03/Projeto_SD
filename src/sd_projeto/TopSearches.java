package sd_projeto;

import java.util.*;

public class TopSearches {
    private Map<String, Integer> searchCounts = new HashMap<>();
    private PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());

    public TopSearches() {
        searchCounts = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
    }

    public void updateSearchCount(String searchTerm) {
        searchCounts.put(searchTerm, searchCounts.getOrDefault(searchTerm, 0) + 1);
    }

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
