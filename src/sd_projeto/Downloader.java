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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
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

    private static final List<String> stopWords = Arrays.asList(
            "de", "a", "o", "que", "e", "do", "da", "em", "um", "para", "é", "com", "não", "uma", "os", "no", "se",
            "na", "por", "mais", "as", "dos", "como", "mas", "foi", "ao", "ele", "das", "tem", "à", "seu", "sua", "ou",
            "ser", "quando", "muito", "há", "nos", "já", "está", "eu", "também", "só", "pelo", "pela", "até", "isso",
            "ela", "entre", "era", "depois", "sem", "mesmo", "aos", "ter", "seus", "quem", "nas", "me", "esse", "eles",
            "estão", "você", "tinha", "foram", "essa", "num", "nem", "suas", "meu", "às", "minha", "têm", "numa", "pelos",
            "elas", "havia", "seja", "qual", "será", "nós", "tenho", "lhe", "deles", "essas", "esses", "pelas", "este",
            "fosse", "dele", "tu", "te", "vocês", "vos", "lhes", "meus", "minhas", "teu", "tua", "teus", "tuas", "nosso",
            "nossa", "nossos", "nossas", "dela", "delas", "esta", "estes", "estas", "aquele", "aquela", "aqueles", "aquelas",
            "isto", "aquilo", "estou", "está", "estamos", "estão", "estive", "esteve", "estivemos", "estiveram", "estava",
            "estávamos", "estavam", "estivera", "estivéramos", "esteja", "estejamos", "estejam", "estivesse", "estivéssemos",
            "estivessem", "estiver", "estivermos", "estiverem", "hei", "há", "havemos", "hão", "houve", "houvemos", "houveram",
            "houvera", "houvéramos", "haja", "hajamos", "hajam", "houvesse", "houvéssemos", "houvessem", "houver", "houvermos",
            "houverem", "houverei", "houverá", "houveremos", "houverão", "houveria", "houveríamos", "houveriam", "sou", "somos",
            "são", "era", "éramos", "eram", "fui", "foi", "fomos", "foram", "fora", "fôramos", "seja", "sejamos", "sejam", "fosse",
            "fôssemos", "fossem", "for", "formos", "forem", "serei", "será", "seremos", "serão", "seria", "seríamos", "seriam",
            "tenho", "tem", "temos", "tém", "tinha", "tínhamos", "tinham", "tive", "teve", "tivemos", "tiveram", "tivera",
            "tivéramos", "tenha", "tenhamos", "tenham", "tivesse", "tivéssemos", "tivessem", "tiver", "tivermos", "tiverem",
            "terei", "terá", "teremos", "terão", "teria", "teríamos", "teriam"
    );


    public Downloader(int downloaderNumber) throws RemoteException {
        setName("Downloader " + downloaderNumber);
        start();
    }

    // Função para remover as stopwords de um texto
    private static StringTokenizer removeStopWords(StringTokenizer tokens) {
        StringBuilder filteredText = new StringBuilder();
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().toLowerCase();
            if (!stopWords.contains(token)) {
                filteredText.append(token).append(" ");
            }
        }
        return new StringTokenizer(filteredText.toString());
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

        /*
         Inicializar o BloomFilter
         100 000 000 elementos esperados
         0.01% probabilidade de falsos positivos
         fonte:https://krisives.github.io/bloom-calculator/
         */
        bloomFilter = new BloomFilter<>(1917011676, s -> s.hashCode(), s -> s.hashCode() * s.length());

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

    @Override
    public void run() {
        boolean flag;
        String url;

        try {
            while (true) {
                url = queue.getFirst();
                if (url != null && correctURL(url)) {

                    lock.lock();
                    flag = bloomFilter.contains(url);
                    lock.unlock();

                    if (!flag) {

                        // Adicionar URL ao BloomFilter
                        lock.lock();
                        bloomFilter.add(url);
                        lock.unlock();

                        try {
                            MulticastSocket socket = new MulticastSocket();
                            Document doc = Jsoup.connect(url).get();

                            // Extrair título do documento
                            String title = doc.title();

                            // Extrair data de publicação, se disponível
                            String publicationDate = doc.select("date").text();

                            // Extrair to_do o texto do HTML
                            StringTokenizer tokens = new StringTokenizer(doc.text());
                            tokens = removeStopWords(tokens);

                            // Extrair URLs
                            Elements links = doc.select("a[href]");
                            StringBuilder linksText = new StringBuilder();

                            for (Element link : links) {
                                String linkUrl = link.attr("abs:href");
                                if (correctURL(linkUrl)) {
                                    linksText.append(linkUrl).append(" "); // todos os links so numa string

                                    lock.lock();
                                    flag = bloomFilter.contains(linkUrl);
                                    lock.unlock();

                                    if (!flag)
                                        queue.addLast(linkUrl); // adicionar os links na queue
                                }
                            }

                            // mensagem multicast
                            String message = "Data" + "\nURL: " + url + "\nTitle: " + title + "\nPublication Date: " + publicationDate +
                                    "\nText: " + tokens + "\nLinks: " + linksText;

                            // Envie a mensagem multicast
                            byte[] buffer = message.getBytes();
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            System.out.println("\n==== Sent message ===== \n" + message);
                            System.out.println("\n");

                            
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("URL already visited: " + url);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean correctURL(String url) {
        try {
            URL testURL = new URL(url);
            URLConnection conn = testURL.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + url);
        } catch (IOException e) {
            System.out.println("Cannot establish connection: " + url);
        }
        return false;
    }

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}