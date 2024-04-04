package sd_projeto;

import java.rmi.*;

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
    public void print_on_client(Urls_list list) throws java.rmi.RemoteException;

    /**
     * Imprime uma mensagem de erro no cliente.
     * @param erro A mensagem de erro a ser impressa.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_err_2_client(Message erro) throws java.rmi.RemoteException;
}
