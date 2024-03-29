package sd_projeto;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Downloader extends Thread {
    private static final String MULTICAST_ADDRESS = "224.3.2.1";
    private static final int PORT = 4321;
    private static InetAddress group;
    private static final int NUM = 5;
    private String url;
    private static QueueInterface queue;

    public Downloader() throws RemoteException {
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                url = queue.getFirst();

                if (url != null) {
                    try {
                        MulticastSocket socket = new MulticastSocket();
                        Document doc = Jsoup.connect(url).get();
                        Elements links = doc.select("a[href]");
                        print("\nLinks: (%d)", links.size());

                        for (Element link : links) {
                            String message = link.attr("abs:href") + " " + link.text();
                            byte[] buffer = message.getBytes();
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            System.out.println("Sent message: " + message);

                            queue.addLast(link.attr("abs:href"));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, MalformedURLException {
        queue = (QueueInterface) Naming.lookup("rmi://localhost:1096/request_downloader");
        group = InetAddress.getByName(MULTICAST_ADDRESS);

        for (int i = 0; i < NUM; i++) {
            new Downloader();
            System.out.println("Downloader" + i + " ready.");
        }
    }

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}
