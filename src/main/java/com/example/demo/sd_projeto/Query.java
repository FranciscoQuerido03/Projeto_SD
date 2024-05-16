package com.example.demo.sd_projeto;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private String content;
    private String clientId;
    private List<String> urls;

    public Query(){
        this.content = "";
        this.clientId = "";
        this.urls = new ArrayList<>(); // Inicializa a lista de URLs vazia
    }

    public String getContent() {
        return content;
    }

    public String getClientId() {
        return clientId;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
