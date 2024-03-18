package sd_projeto;

import java.rmi.*;

import sd_projeto.Message;
import sd_projeto.Urls_list;

public interface Request extends Remote {
    public void send_request(Message m) throws RemoteException;
    public void subscribe(Barrel_I barrel) throws RemoteException;
    public void answer(Urls_list m) throws RemoteException;
}