package com.example.demo.sd_projeto;

public class Query {
    private String content;
    private String clientId;

    public Query(){
        this.content = "";
        this.clientId = "";
    }

    public String getContent() {
        return content;
    }

    public String getClientId() {
        return clientId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
