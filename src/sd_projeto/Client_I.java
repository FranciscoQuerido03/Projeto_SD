package sd_projeto;

import java.rmi.*;

public interface Client_I extends Remote {
    public void print_on_client(Urls_list list) throws java.rmi.RemoteException;

    public void print_err_2_client(Message erro) throws java.rmi.RemoteException;
}
