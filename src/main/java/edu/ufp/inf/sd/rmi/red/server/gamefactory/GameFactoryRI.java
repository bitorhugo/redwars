package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameFactoryRI extends Remote {
    public void hello() throws RemoteException;
}
