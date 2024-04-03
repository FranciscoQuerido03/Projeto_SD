package sd_projeto;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Queue extends UnicastRemoteObject implements QueueInterface {

    private Deque<String> queue;
    private int threadNumber = 0;
    
    private static String NAMING_DOWNLOADER;
    private static String NAMING_GATEWAY;

    protected Queue() throws RemoteException {
        queue = new ConcurrentLinkedDeque<>();

        File_Infos f = new File_Infos();
		f.get_data("Queue");

        NAMING_DOWNLOADER = f.Registo[0];
        NAMING_GATEWAY = f.Registo[1];
    }

    @Override
    public synchronized String getFirst() throws RemoteException {
        String url = queue.pollFirst();
        //System.out.println(queue.size());
        return url;

    }

    @Override
    public synchronized boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }

    @Override
    public synchronized int size() throws RemoteException {
        return queue.size();
    }

    @Override
    public synchronized void print() throws RemoteException {
        for (String item : queue) {
            //System.out.println(item);
        }
    }

    @Override
    public synchronized ArrayList<String> getAll() throws RemoteException {
        return new ArrayList<>(queue);
    }

    @Override
    public synchronized void addFirst(String url) throws RemoteException {
        queue.addFirst(url);
        System.out.println("Added: " + url + " first");
        //checkNadd();
    }


    @Override
    public synchronized void addLast(String url) throws RemoteException {
        queue.addLast(url);
        //System.out.println("Added: " + url + " last");
        //checkNadd();
    }


//    private void checkNadd() {
//        if (threadNumber == 0) {
//            threadNumber++;
//            new Thread(() -> {
//                try {
//                    alertObserver();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//    }
//    public synchronized void alertObserver() throws RemoteException {
//        while (!queue.isEmpty() || !observers.isEmpty()) {
//            String dataToSend = queue.pollFirst();
//
//            Random random = new Random();
//            int index = random.nextInt(observers.size());
//            DownloaderInterface observer = observers.get(index);
//
//            observer.onUrlAdded(dataToSend);
//            System.out.println("Data sent to process: " + dataToSend);
//        }
//        System.out.println("Queue is empty.");
//        threadNumber = 0;
//    }

    public static void main(String[] args) {
        try {
            Queue q = new Queue();
            LocateRegistry.createRegistry(1096).rebind(NAMING_DOWNLOADER, q);
            LocateRegistry.createRegistry(1097).rebind(NAMING_GATEWAY, q);
            System.out.println("Queue ready.");


        } catch (RemoteException re) {
            System.out.println("Exception in GateWay.main: " + re);
        }
    }

}