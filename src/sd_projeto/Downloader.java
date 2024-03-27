package sd_projeto;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class Downloader implements Runnable {
    private static final String MULTICAST_ADDRESS = "224.3.2.1"; //temos de escolher um endereco dps
    private static final int PORT = 4321; //temos de escolher uma porta dps
    private String url;
    private Thread t;

    public Downloader(String url) {
        this.url = url;
        t = new Thread(this, url);
        t.start();
    }

    public void run() {

        try (MulticastSocket socket = new MulticastSocket()) {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            print("\nLinks: (%d)", links.size());
            /*
                Aqui nao sei se junte todas as mensagens e depois envio ou se envio uma por uma
             */
            QueueInterface queue = (QueueInterface) Naming.lookup("rmi://localhost:1096/request_downloader");

            for (Element link : links) {
                String message = link.attr("abs:href") + " " + link.text();
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                System.out.println("Sent message: " + message);

                queue.addLast(link.attr("abs:href"));
            }



            //IndexInterface index = (IndexInterface) Naming.lookup("rmi://localhost/index");
            //index.add(links.get(0).attr("abs:href"),  links.text());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}
