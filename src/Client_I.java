package sd_projeto;

import java.rmi.*;

public interface Client_I extends Remote {
    public void print_on_client(Urls_list list) throws java.rmi.RemoteException;
}
