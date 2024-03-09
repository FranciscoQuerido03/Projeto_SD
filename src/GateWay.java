package sd_projeto;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
//import java.net.*;

public class GateWay extends UnicastRemoteObject implements Request {

	public GateWay() throws RemoteException {
		super();
	}

	public void send_request(Message m) throws RemoteException {
        System.out.println("Server: " + m.toString());
    }

	// =======================================================

	public static void main(String args[]) {

		try {
			GateWay h = new GateWay();
			LocateRegistry.createRegistry(1099).rebind("request", h);
			System.out.println("Server ready.");
		} catch (RemoteException re) {
			System.out.println("Exception in HelloImpl.main: " + re);
		} /*catch (MalformedURLException e) {
			System.out.println("MalformedURLException in HelloImpl.main: " + e);
		}*/

	}

}