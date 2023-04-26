package edu.ufp.inf.sd.rmi.red.server.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import edu.ufp.inf.sd.rmi.red.server.RedServer;

public interface ClusterRI extends Remote {
    public void connect(RedServer server) throws RemoteException;
    public void disconnect(RedServer server) throws RemoteException;
    public List<RedServer> getServers() throws RemoteException;
    public boolean hasServer(RedServer server) throws RemoteException;
    
}
