package sd_projeto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface QueueInterface extends Remote{
    public String get() throws RemoteException;
    public boolean isEmpty() throws RemoteException;
    public int size() throws RemoteException;
    public void print() throws RemoteException;
    public ArrayList<String> getAll() throws RemoteException;
    public void removeAll() throws RemoteException;
    public void removeFirst() throws RemoteException;
    public void removeLast() throws RemoteException;
    public void addFirst(String url) throws RemoteException;
    public void addLast(String url) throws RemoteException;

}

