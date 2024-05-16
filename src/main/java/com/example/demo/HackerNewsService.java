package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class HackerNewsService {

    private static final String TOP_STORIES_URL = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String STORY_BASE_URL = "https://hacker-news.firebaseio.com/v0/item/";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<HackerNewsItemRecord> fetchTopStoriesWithKeywords(List<String> keywords) throws IOException, InterruptedException, ExecutionException {
        // Obter os IDs das top stories
        HttpRequest topStoriesRequest = HttpRequest.newBuilder()
                .uri(URI.create(TOP_STORIES_URL))
                .build();

        HttpResponse<String> topStoriesResponse = client.send(topStoriesRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode topStoryIdsNode = objectMapper.readTree(topStoriesResponse.body());
        List<Integer> topStoryIds = new ArrayList<>();
        topStoryIdsNode.forEach(id -> topStoryIds.add(id.asInt()));

        // Filtrar histórias por palavras-chave
        List<CompletableFuture<HackerNewsItemRecord>> storyFutures = topStoryIds.stream()
                .map(this::fetchStory)
                .toList();

        List<HackerNewsItemRecord> stories = storyFutures.stream()
                .map(CompletableFuture::join)
                .filter(story -> story != null && story.title() != null &&
                        keywords.stream().anyMatch(story.title()::contains))
                .collect(Collectors.toList());

        return stories;
    }

    private CompletableFuture<HackerNewsItemRecord> fetchStory(int id) {
        HttpRequest storyRequest = HttpRequest.newBuilder()
                .uri(URI.create(STORY_BASE_URL + id + ".json"))
                .build();

        return client.sendAsync(storyRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return objectMapper.readValue(body, HackerNewsItemRecord.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }
}
