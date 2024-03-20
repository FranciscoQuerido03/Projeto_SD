package sd_projeto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface QueueInterface extends Remote{
    public void add(String url) throws RemoteException;
    public String get() throws RemoteException;
    public boolean isEmpty() throws RemoteException;
    public int size() throws RemoteException;
    public void print() throws RemoteException;
    public void remove() throws RemoteException;
    public void addAll(ArrayList<String> urls) throws RemoteException;
    public ArrayList<String> getAll() throws RemoteException;
    public void removeAll() throws RemoteException;
    public void printAll() throws RemoteException;
    public void removeFirst() throws RemoteException;
    public void removeLast() throws RemoteException;
    public String getFirst() throws RemoteException;
    public String getLast() throws RemoteException;
    public void printFirst() throws RemoteException;
    public void printLast() throws RemoteException;
    public void addFirst(String url) throws RemoteException;
    public void addLast(String url) throws RemoteException;
    public void printAllReverse() throws RemoteException;
    public void printReverse() throws RemoteException;
    public void removeFirstOccurrence(String url) throws RemoteException;
    public void removeLastOccurrence(String url) throws RemoteException;
    public void printFirstOccurrence(String url) throws RemoteException;
    public void printLastOccurrence(String url) throws RemoteException;
    public void addAfter(String url, String newUrl) throws RemoteException;
    public void addBefore(String url, String newUrl) throws RemoteException;
    public void printAfter(String url) throws RemoteException;
    public void printBefore(String url) throws RemoteException;
    public void removeAfter(String url) throws RemoteException;
    public void removeBefore(String url) throws RemoteException;
    public void addAllAfter(String url, ArrayList<String> urls) throws RemoteException;
    public void addAllBefore(String url, ArrayList<String> urls) throws RemoteException;
    public void printAllAfter(String url) throws RemoteException;
    public void printAllBefore(String url) throws RemoteException;
    public void removeAllAfter(String url) throws RemoteException;
    public void removeAllBefore(String url) throws RemoteException;
    public void removeFirstOccurrenceAfter(String url, String newUrl) throws RemoteException;
    public void removeFirstOccurrenceBefore(String url, String newUrl) throws RemoteException;
    public void removeLastOccurrenceAfter(String url, String newUrl) throws RemoteException;
    public void removeLastOccurrenceBefore(String url, String newUrl) throws RemoteException;
    public void printFirstOccurrenceAfter(String url, String newUrl) throws RemoteException;
    public void printFirstOccurrenceBefore(String url, String newUrl) throws RemoteException;
}

