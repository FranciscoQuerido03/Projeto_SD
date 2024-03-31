package sd_projeto;

import java.io.*;

public class Message implements Serializable {
	public StringBuilder text;

	public Message(String text) {
		this.text = new StringBuilder(text);
	}

	public void addText(String newText) {
        this.text.append(newText + "\n");
    }

	public String toString() {
		return text.toString();
	}
}