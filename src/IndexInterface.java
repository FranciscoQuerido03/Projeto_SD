
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IndexInterface extends Remote {
    public double add(String url, ArrayList<String> palavras) throws RemoteException;
    public double get(String url) throws RemoteException;
;
}
