package sd_projeto;

import java.rmi.*;
import java.util.ArrayList;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface que define os métodos para comunicação remota com o servidor.
 */
public interface Request extends Remote {
    /**
     * Método que solicita a um barrel a pesquisa de um determinado conjunto de palarvas.
     * @param c O cliente que fez a solicitação.
     * @param m A mensagem com a solicitação.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void send_request_barrels(Client_I c, Message m) throws RemoteException;

    /**
     * Método que solicita a adição de um URL à fila de processamento.
     * @param c O cliente que fez a solicitação.
     * @param m A mensagem com o URL a ser adicionado à fila.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void send_request_queue(Client_I c, Message m) throws RemoteException;


    public void request_adm_painel(Client_I c, Boolean print) throws RemoteException;

    /**
     * ?????????????????????????.
     * @param barrel O barril a ser inscrito.
     * @param id O identificador do barril.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void subscribe(Barrel_I barrel, int id) throws RemoteException;

    /**
     * Envia uma resposta ao cliente.
     * @param m A lista de URLs a ser enviada como resposta ao cliente.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void answer(ArrayList<URL_Content> m) throws RemoteException;

    /**
     * Envia uma mensagem de erro ao cliente quando não há correspondências para uma solicitação.
     * @param s A mensagem de erro a ser enviada ao cliente.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void err_no_matches(Message s) throws RemoteException;

    /**
     * Desconecta um barril do servidor.
     * @param barrel O barril a ser desconectado.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void barrel_disconnect(Barrel_I barrel) throws RemoteException;

    /**
     * Conecta um cliente ao servidor.
     *
     * @param c O cliente a ser conectado.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void client_connect(Client_I c) throws RemoteException;

    /**
     * Desconecta um cliente do servidor.
     *
     * @param c O cliente a ser desconectado.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void client_disconnect(Client_I c) throws RemoteException;

    /**
     * Permite ao cliente solicitar a impressão de uma lista de URLs apresentando os resultados da pesquisa em partes de 10.
     *
     * @param c     O cliente que fez a solicitação.
     * @param m     A mensagem com a solicitação.
     * @param indx  Index para calcular o conjunto de 10 URLs a enviar.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void request10(Client_I c, Message m, int indx) throws RemoteException;

    /**
     * Envia 10 resultados ao cliente para serem exibidos.
     *
     * @param c    O cliente que receberá os resultados.
     * @param indx A posição inicial na lista de URLs.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void print_on_client_10(Client_I c, int indx) throws RemoteException;

    /**
     * Solicita a obtenção dos links que apontam para um determinado URL.
     *
     * @param c       O cliente que fez a solicitação.
     * @param conteudo A mensagem com o URL alvo.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void links_pointing_to(Client_I c, Message conteudo) throws RemoteException;

    /**
     * Envia os links que apontam para um URL específico ao cliente.
     *
     * @param urlsPointingTo A lista de links que apontam para o URL alvo.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    public void answer_pointers(ArrayList<URL_Content> urlsPointingTo) throws RemoteException;
}
