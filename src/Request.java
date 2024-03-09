package sd_projeto;

import java.rmi.*;

public interface Request extends Remote {
    public void send_request(Message m) throws RemoteException;
}