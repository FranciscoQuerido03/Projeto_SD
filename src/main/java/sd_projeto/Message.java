package sd_projeto;

import java.io.*;

/**
 * Classe que representa uma mensagem serializável.
 * Esta classe permite adicionar texto à mensagem e converter a mensagem para uma string.
 */
public class Message implements Serializable {
	public StringBuilder text;

	/**
	 * Construtor para criar uma mensagem com o texto fornecido.
	 * @param text O texto da mensagem.
	 */
	public Message(String text) {
		this.text = new StringBuilder(text);
	}

	/**
	 * Método para adicionar texto à mensagem.
	 * @param newText O texto a ser adicionado à mensagem.
	 */
	public void addText(String newText) {
		this.text.append(newText).append("\n");
	}

	/**
	 * Método para obter a representação da mensagem como uma string.
	 * @return A representação da mensagem como uma string.
	 */
	public String toString() {
		return text.toString();
	}
}
