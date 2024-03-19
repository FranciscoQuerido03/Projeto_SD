import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.rmi.Naming;


public class Main {
    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        QueueInterface queue = (QueueInterface) Naming.lookup("rmi://localhost/queue");
        ArrayList<String> urls = new ArrayList<>();

//        for (int i = 0; i < queue.size(); i++) {
//            urls.add(queue.getFirst());
//        }

        urls.add("https://pt.wikipedia.org/wiki/Lista_de_munic%C3%ADpios_brasileiros_por_%C3%A1rea_decrescente");
        urls.add("https://www.facebook.com");
        urls.add("https://www.twitter.com");
        urls.add("https://www.instagram.com");
        urls.add("https://www.linkedin.com");
        urls.add("https://www.pinterest.com");
        urls.add("https://www.snapchat.com");
        urls.add("https://www.reddit.com");
        urls.add("https://www.tumblr.com");
        urls.add("https://www.flickr.com");
        urls.add("https://www.google.com");

        int urls_size = urls.size();

        for (int i = 0; i < urls_size; i++) {
            String url = urls.removeFirst();
            new Downloader(url);
        }

    }
}