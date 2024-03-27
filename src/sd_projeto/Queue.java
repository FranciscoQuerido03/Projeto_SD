package sd_projeto;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Queue extends UnicastRemoteObject implements QueueInterface {

    private Deque<String> queue;

    protected Queue() throws RemoteException {
        queue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public synchronized String get() throws RemoteException {
        return queue.poll();
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
    public synchronized void removeAll() throws RemoteException {
        queue.clear();
    }

    @Override
    public synchronized void addFirst(String url) throws RemoteException {
        queue.addFirst(url);
    }

    @Override
    public synchronized void addLast(String url) throws RemoteException {
        queue.addLast(url);
        System.out.println("Added: " + url);
    }

    @Override
    public synchronized void removeFirst() throws RemoteException {
        queue.pollFirst();
    }

    @Override
    public synchronized void removeLast() throws RemoteException {
        queue.pollLast();
    }

    public static void main(String args[]) {
        try {
            Queue q = new Queue();
            LocateRegistry.createRegistry(1096).rebind("request_downloader", q);
            LocateRegistry.createRegistry(1097).rebind("request_gateway", q);
            System.out.println("Queue ready.");
            while (true) {
                Thread.sleep(5000);
                System.out.println("Queue size: " + q.size());
                q.print();
            }

        } catch (RemoteException re) {
            System.out.println("Exception in GateWay.main: " + re);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
