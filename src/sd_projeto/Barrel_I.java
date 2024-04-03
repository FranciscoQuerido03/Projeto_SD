package sd_projeto;

import java.rmi.*;

public interface Barrel_I extends Remote {
	public void request(String s, int min) throws java.rmi.RemoteException;

	/*
	 * Debug Functions
	 */
	public void printUrls() throws java.rmi.RemoteException;
	public void printWordsHM() throws java.rmi.RemoteException;
}
