package sd_projeto;

public class Query {
    private String content;
    private String clientId;

    public Query(){
        this.content = new String();
        this.clientId = new String();
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
