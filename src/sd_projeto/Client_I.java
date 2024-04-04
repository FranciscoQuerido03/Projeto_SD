package sd_projeto;

import java.rmi.*;
import java.util.ArrayList;

public interface Client_I extends Remote {
    public void print_on_client(ArrayList<URL_Content> list) throws java.rmi.RemoteException;

    public void print_err_2_client(Message erro) throws java.rmi.RemoteException;
}
