package com.example.demo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BoredService {

    private static final String BORED_API_URL = "https://www.boredapi.com/api/activity/";

    private final RestTemplate restTemplate = new RestTemplate();


    public BoredActivity getBoredActivity(String participants) {
        String apiUrl = BORED_API_URL + "?participants=" + participants;
        BoredActivity boredActivity = restTemplate.getForObject(apiUrl, BoredActivity.class);
        assert boredActivity != null;
        System.out.println(boredActivity.getActivity() + " " + boredActivity.getType() + " " + boredActivity.getPrice());
        return boredActivity;
    }
}