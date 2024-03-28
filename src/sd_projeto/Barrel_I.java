package sd_projeto;

import java.rmi.*;

public interface Barrel_I extends Remote {
	public void request(String s) throws java.rmi.RemoteException;
	public void Update_mem(boolean b, Barrel_I h) throws java.rmi.RemoteException;
	public void printUrls() throws java.rmi.RemoteException;
	public void printWordsHM() throws java.rmi.RemoteException;
}
