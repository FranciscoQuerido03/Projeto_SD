package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class HackerNewsService {

    private static final String TOP_STORIES_URL = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String STORY_BASE_URL = "https://hacker-news.firebaseio.com/v0/item/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executor = Executors.newFixedThreadPool(100); // Pool de threads para paralelismo

    public List<HackerNewsItemRecord> fetchTopStoriesWithKeywords(List<String> keywords) {
        // Obter os IDs das top stories
        JsonNode topStoryIdsNode = restTemplate.getForObject(TOP_STORIES_URL, JsonNode.class);
        List<Integer> topStoryIds = new ArrayList<>();
        topStoryIdsNode.forEach(id -> topStoryIds.add(id.asInt()));

        // Iniciar tarefas assíncronas para ir buscar cada história
        List<CompletableFuture<HackerNewsItemRecord>> futures = topStoryIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> fetchStory(id), executor))
                .collect(Collectors.toList());

        // Aguardar a conclusão de todas as tarefas
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Filtrar histórias por palavras-chave
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(story -> story != null && story.title() != null &&
                        keywords.stream().anyMatch(story.title()::contains))
                .collect(Collectors.toList());
    }

    private HackerNewsItemRecord fetchStory(int id) {
        String storyUrl = STORY_BASE_URL + id + ".json";
        return restTemplate.getForObject(storyUrl, HackerNewsItemRecord.class);
    }
}
