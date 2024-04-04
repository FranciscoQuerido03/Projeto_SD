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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;


public class Downloader extends Thread {
    private static String MULTICAST_ADDRESS;
    private static String NAMING;
    private static int PORT;
    private static InetAddress group;
    private static int NUM;
    private static QueueInterface queue;
    private static final Object lock = new Object();

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
            "terei", "terá", "teremos", "terão", "teria", "teríamos", "teriam", "?", "!", "-", " "
    );


    public Downloader(int downloaderNumber) throws RemoteException {
        setName("Downloader " + downloaderNumber);
        start();
    }

    // Função para remover as stopwords de um texto
    private static String removeStopWords(String text) {
        String[] words = text.split("\\s+");
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                filteredWords.add(word);
            }
        }
        return String.join(" ", filteredWords);
    }


    public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, MalformedURLException {

        if (args.length != 1) {
            System.out.println("Usage: java Downloader <id>");
            System.exit(1);
        }

        int id = parseInt(args[0]);

        File_Infos f = new File_Infos();
        f.get_data("Downloader");

        MULTICAST_ADDRESS = f.Address;
        PORT = f.Port;
        NUM = f.NUM_BARRELS;
        NAMING = f.lookup[0];

        queue = (QueueInterface) Naming.lookup(NAMING);
        group = InetAddress.getByName(MULTICAST_ADDRESS);




        new Downloader(id);
        System.out.println("Downloader " + id + " ready.");


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

                    try {
                        MulticastSocket socket = new MulticastSocket();
                        Document doc = Jsoup.connect(url).get();

                        // Extrair título do documento
                        String title = doc.title();

                        // Extrair data de publicação, se disponível
                        String publicationDate = doc.select("date").text();

                        // Extrair to_do o texto do HTML
                        String tokens = doc.text();
                        Pattern pattern = Pattern.compile("\\b\\p{L}+\\b");
                        Matcher matcher = pattern.matcher(tokens);
                        List<String> filteredWords = new ArrayList<>();
                        while (matcher.find()) {
                            String word = matcher.group().toLowerCase();
                            if (!stopWords.contains(word)) {
                                filteredWords.add(word);
                            }
                        }
                        tokens = String.join(" ", filteredWords);

                        // Extrair URLs
                        Elements links = doc.select("a[href]");
                        StringBuilder linksText = new StringBuilder();

                        for (Element link : links) {
                            String linkUrl = link.attr("abs:href");
                            if (correctURL(linkUrl)) {
                                linksText.append(linkUrl).append(" "); // todos os links so numa string

                                queue.addLast(linkUrl); // adicionar os links na queue
                            }
                        }

                        // mensagem multicast
                        String message1 = "Data" + "\nURL: " + url + "\nTitle: " + title + "\nPublication Date: " + publicationDate + "\n";
                        String message2 = "Text: " + tokens + "\n";
                        String message3 = "Links: " + linksText + "\n";

                        // Envie a mensagem multicast
                        byte[] buffer = message1.getBytes();
                        byte[] buffer2 = message2.getBytes();
                        byte[] buffer3 = message3.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                        DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, PORT);
                        DatagramPacket packet3 = new DatagramPacket(buffer3, buffer3.length, group, PORT);

                        synchronized(lock){
                            socket.send(packet);
                            socket.send(packet2);
                            socket.send(packet3);
                        }

                        System.out.println("Sent message: \n" + message1);
                        System.out.println("Sent message: \n" + message2);
                        System.out.println("Sent message: \n" + message3);

                        url = null;


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
//                    if (url != null)
//                        System.out.println("Thread("+this.threadId()+")"+" URL already visited: " + url);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean correctURL(String url) {
        try {
            URL testURL = new URL(url);
            URLConnection connection = testURL.openConnection();

            // Verificar se a conexão é do tipo HttpURLConnection
            if (connection instanceof HttpURLConnection conn) {
                conn.setRequestMethod("HEAD"); // Apenas cabeçalhos, sem baixar o conteúdo
                int responseCode = conn.getResponseCode();
                return (responseCode == HttpURLConnection.HTTP_OK);
            } else {
                // Tratar casos em que a URL não é uma conexão HTTP
                return false;
            }
        } catch (IOException ignored) {
            // Tratar exceções de E/S, como URL malformada ou problemas de conexão
            return false;
        }
    }



    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}