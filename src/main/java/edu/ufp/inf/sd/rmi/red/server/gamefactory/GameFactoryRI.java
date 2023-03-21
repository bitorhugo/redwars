package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;

public interface GameFactoryRI extends Remote {
    public SessionToken login(String username, String secret) throws RemoteException;

    public SessionToken register(String username, String secret) throws RemoteException;
}
