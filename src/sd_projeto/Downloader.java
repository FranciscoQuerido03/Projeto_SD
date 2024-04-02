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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Downloader extends Thread {
    private static String MULTICAST_ADDRESS;
    private static String NAMING;
    private static int PORT;
    private static InetAddress group;
    private static int NUM;
    private static QueueInterface queue;
    private static BloomFilter<String>  bloomFilter;
    private static final Lock lock = new ReentrantLock();

    public Downloader(int downloaderNumber) throws RemoteException {
        setName("Downloader " + downloaderNumber);
        start();
    }

    @Override
    public void run() {
        boolean flag;
        String url;

        try {
            while (true) {
                url = queue.getFirst();
                if(url != null) {
                    lock.lock();
                    flag = bloomFilter.contains(url);
                    lock.unlock();

                    if (!flag) {
                        try {
                            MulticastSocket socket = new MulticastSocket();
                            Document doc = Jsoup.connect(url).get();
                            Elements links = doc.select("a[href]");
                            print("\nLinks: (%d)", links.size());

                            for (Element link : links) {
                                String linkUrl = link.attr("abs:href");

                                lock.lock();
                                flag = bloomFilter.contains(linkUrl);
                                lock.unlock();

                                if (!flag) {
                                    String message = "Data " + linkUrl + " " + link.text();
                                    byte[] buffer = message.getBytes();
                                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                                    socket.send(packet);
                                    System.out.println("Sent message: " + message);

                                    queue.addLast(linkUrl);

                                    lock.lock();
                                    bloomFilter.add(linkUrl);
                                    lock.unlock();
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        System.out.println("URL already visited: " + url);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, MalformedURLException {

        File_Infos f = new File_Infos();
        f.get_data("Downloader");

        MULTICAST_ADDRESS = f.Address;
        PORT = f.Port;
        NUM = f.NUM_BARRELS;
        NAMING = f.lookup[0];

        queue = (QueueInterface) Naming.lookup(NAMING);
        group = InetAddress.getByName(MULTICAST_ADDRESS);

        bloomFilter = new BloomFilter<>(1328771238,s -> s.hashCode(),s -> s.hashCode() * s.length());

        for (int i = 0; i < NUM; i++) {
            new Downloader(i);
            System.out.println("Downloader " + i + " ready.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getName().startsWith("Downloader")) {
                    System.out.println(t + "Ending");
                    t.interrupt();
                }
            }
        }));
    }

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}