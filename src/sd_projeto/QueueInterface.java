package sd_projeto;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface QueueInterface extends Remote{
    public String getFirst() throws RemoteException;
    public boolean isEmpty() throws RemoteException;
    public int size() throws RemoteException;
    public void print() throws RemoteException;
    public ArrayList<String> getAll() throws RemoteException;
    public void addFirst(String url) throws RemoteException;
    public void addLast(String url) throws RemoteException;

}
