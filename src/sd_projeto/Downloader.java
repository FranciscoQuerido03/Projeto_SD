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
    private static final String MULTICAST_ADDRESS = "224.3.2.1";
    private static final int PORT = 4321;
    private static InetAddress group;
    private static final int NUM = 2;
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
        queue = (QueueInterface) Naming.lookup("rmi://localhost:1096/request_downloader");
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
