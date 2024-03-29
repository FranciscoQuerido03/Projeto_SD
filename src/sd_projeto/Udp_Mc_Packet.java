package sd_projeto;

public class Udp_Mc_Packet {
    String type; 
    String packet;

    public Udp_Mc_Packet(String type, String content) {
        this.type = type;
        this.packet = content;
    }

    @Override
    public String toString() {
        return this.type + " " + this.packet; 
    }

}
