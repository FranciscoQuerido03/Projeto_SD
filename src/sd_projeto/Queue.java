package sd_projeto;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Queue extends UnicastRemoteObject implements QueueInterface {

    private Deque<String> queue;
    private int threadNumber = 0;

    protected Queue() throws RemoteException {
        queue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public synchronized String getFirst() throws RemoteException {
        return queue.pollFirst();
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
            System.out.println(item);
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
        System.out.println("Added: " + url + " last");
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
            LocateRegistry.createRegistry(1096).rebind("request_downloader", q);
            LocateRegistry.createRegistry(1097).rebind("request_gateway", q);
            System.out.println("Queue ready.");


        } catch (RemoteException re) {
            System.out.println("Exception in GateWay.main: " + re);
        }
    }

}
