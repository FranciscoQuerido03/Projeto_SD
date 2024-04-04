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
            "terei", "terá", "teremos", "terão", "teria", "teríamos", "teriam", "?", "!", "-", " ", "–", ":", ";", ",", "."
    );


    public Downloader() throws RemoteException {
        setName("Downloader ");
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

        File_Infos f = new File_Infos();
        f.get_data("Downloader");

        MULTICAST_ADDRESS = f.Address;
        PORT = f.Port;
        NUM = f.NUM_BARRELS;
        NAMING = f.lookup[0];

        queue = (QueueInterface) Naming.lookup(NAMING);
        group = InetAddress.getByName(MULTICAST_ADDRESS);

        new Downloader();
        System.out.println("Downloader ready.");

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

                        // Extrair todo o texto do HTML
                        String tokens = doc.text();
                        tokens = removeStopWords(tokens);

                        // Extrair URLs
                        Elements links = doc.select("a[href]");
                        StringBuilder linksText = new StringBuilder();

                        for (Element link : links) {
                            String linkUrl = link.attr("abs:href");
                            if (correctURL(linkUrl)) {
                                linksText.append(linkUrl).append(" "); // todos os links em uma string
                                queue.addLast(linkUrl); // adicionar os links na fila
                            }
                        }

                        // mensagens
                        String header = "Data: " + url + "\n";
                        String message1 = header + "Title: " + title;
                        String message2 = header + "Text: ";
                        String message3 = header + "Links: ";
                        String endMessage = header + "END\n";

                        // Enviar título
                        byte[] buffer1 = message1.getBytes();
                        DatagramPacket packet1 = new DatagramPacket(buffer1, buffer1.length, group, PORT);
                        socket.send(packet1);

                        // Enviar texto em partes de 50 palavras
                        String[] words = tokens.split("\\s+");
                        for (int i = 0; i < words.length; i += 50) {
                            StringBuilder textPart = new StringBuilder();
                            for (int j = i; j < Math.min(i + 50, words.length); j++) {
                                textPart.append(words[j]).append(" ");
                            }
                            byte[] buffer2 = (message2 + textPart + "\n").getBytes();
                            DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, PORT);
                            socket.send(packet2);
                        }

                        // Enviar links em partes de 10
                        String[] linkUrls = linksText.toString().split("\\s+");
                        for (int i = 0; i < linkUrls.length; i += 10) {
                            StringBuilder linksPart = new StringBuilder();
                            for (int j = i; j < Math.min(i + 10, linkUrls.length); j++) {
                                linksPart.append(linkUrls[j]).append(" ");
                            }
                            byte[] buffer3 = (message3 + linksPart + "\n").getBytes();
                            DatagramPacket packet3 = new DatagramPacket(buffer3, buffer3.length, group, PORT);
                            socket.send(packet3);
                        }

                        // Enviar mensagem final
                        byte[] buffer4 = endMessage.getBytes();
                        DatagramPacket packet4 = new DatagramPacket(buffer4, buffer4.length, group, PORT);
                        socket.send(packet4);


                       System.out.println("Sent message");

                        url = null;

                    } catch (IOException ignored) {}
                } else {
                    sleep(1000);
                }
            }
        } catch (InterruptedException | RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean correctURL(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }



    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }
}