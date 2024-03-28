package sd_projeto;

import java.io.*;
import java.util.List;

public class Urls_list implements Serializable {
    List<String> Vals;

    public Urls_list(List<String> Vals) {
        this.Vals = Vals;
    }

    public void addUrl(String value) {
        Vals.add(value);
    }

    public boolean hasValues() {
        return Vals != null && !Vals.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Vals.size(); i++) {
            sb.append(i + 1).append(" -> ").append(Vals.get(i)).append("\n");
        }
        return sb.toString();
    }

    public String wordtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Vals);
        return sb.toString();
    }
}
