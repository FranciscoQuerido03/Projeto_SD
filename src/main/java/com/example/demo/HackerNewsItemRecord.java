package com.example.demo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HackerNewsItemRecord(
        Integer id,
        String type,
        String by,
        Long time,
        String text,
        Boolean dead,
        String parent,
        Integer poll,
        List<Integer> kids,
        String url,
        Integer score,
        String title,
        List<Integer> parts,
        Integer descendants
) {
}
