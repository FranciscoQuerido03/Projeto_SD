package sd_projeto;

import java.rmi.*;
import java.util.ArrayList;

/**
 * Interface remota para comunicação com o cliente.
 * Define métodos para imprimir uma lista de URLs e mensagens de erro no cliente.
 */
public interface Client_I extends Remote {
    /**
     * Imprime uma lista de URLs no cliente.
     * @param list A lista de URLs a ser impressa.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_on_client(ArrayList<URL_Content> list) throws java.rmi.RemoteException;

    /**
     * Imprime uma mensagem de erro no cliente.
     * @param erro A mensagem de erro a ser impressa.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_err_2_client(Message erro) throws java.rmi.RemoteException;

    public void print_adm_console_on_client(Message m) throws java.rmi.RemoteException;
}
