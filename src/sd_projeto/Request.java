package sd_projeto;

import java.rmi.*;

public interface Request extends Remote {
    public void send_request_barrels(Client_I c, Message m, int min) throws RemoteException;
    public void send_request_queue(Client_I c, Message m) throws RemoteException;
    public Message adm_painel() throws RemoteException;

    public void subscribe(Barrel_I barrel, int id) throws RemoteException;
    public void answer(Urls_list m) throws RemoteException;
    public void err_no_matches(Message s) throws RemoteException;
    public void barrel_disconnect(Barrel_I barrel) throws RemoteException;
}