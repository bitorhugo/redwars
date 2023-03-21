package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public interface GameFactoryRI extends Remote {
    public SessionToken login(User user) throws RemoteException;
}
