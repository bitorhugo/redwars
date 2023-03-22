package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.model.session.Session;

public interface GameFactoryRI extends Remote {
    public Session login(String username, String secret) throws RemoteException;

    public Session register(String username, String secret) throws RemoteException;
}
