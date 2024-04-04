package sd_projeto;

/**
 * Classe que representa um pacote UDP para comunicação multicast.
 */
public class Udp_Mc_Packet {
    private String type; // Tipo do pacote
    private String packet; // Conteúdo do pacote

    /**
     * Construtor da classe `Udp_Mc_Packet`.
     * @param type O tipo do pacote.
     * @param content O conteúdo do pacote.
     */
    public Udp_Mc_Packet(String type, String content) {
        this.type = type;
        this.packet = content;
    }

    /**
     * Retorna uma representação do pacote como uma string.
     * @return Uma string que represetna o pacote no formato "tipo conteúdo".
     */
    @Override
    public String toString() {
        return this.type + "\n" + this.packet;
    }
}
