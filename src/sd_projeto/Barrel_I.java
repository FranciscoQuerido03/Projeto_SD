package sd_projeto;

import java.rmi.*;

/**
 * Interface remota que define métodos para interagir com um barrel.
 */
public interface Barrel_I extends Remote {
	/**
	 * Método que solicita a um barrel a pesquisa de um determinado conjunto de palarvas.
	 *
	 * @param s Palavras a serem pesquisadas.
	 * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
	 */
	public void request(String s) throws java.rmi.RemoteException;

    public void links_pointing_to(String clientRequest) throws RemoteException;
}
