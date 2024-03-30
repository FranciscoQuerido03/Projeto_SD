package sd_projeto;

import java.rmi.*;

public interface Request extends Remote {
    public void send_request(Client_I c, Message m) throws RemoteException;
    public void send_url(Client_I c, Message m) throws RemoteException;
    public void subscribe(Barrel_I barrel) throws RemoteException;
    public void answer(Urls_list m) throws RemoteException;
    public void err_no_matches(Message s) throws RemoteException;
    public void barrel_disconnect(Barrel_I barrel) throws RemoteException;
}