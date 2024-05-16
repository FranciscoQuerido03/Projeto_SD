package sd_projeto;

import java.rmi.Remote;

public interface WebServer_I extends Remote{

    public void update(Message m) throws java.rmi.RemoteException;
} 
