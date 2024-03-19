package sd_projeto;

import java.io.*;
import java.util.List;

public class Urls_list implements Serializable {
	List<String> Vals;

	public Urls_list(List<String> Vals) {
		this.Vals = Vals;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Vals);
        return sb.toString();
    }
}