package sd_projeto;

import java.rmi.*;

public interface Barrel_I extends Remote {
	public void request(String s) throws java.rmi.RemoteException;
}
