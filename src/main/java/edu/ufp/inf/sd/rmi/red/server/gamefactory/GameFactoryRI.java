package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSession;

public interface GameFactoryRI extends Remote {
    public GameSession login(String username, String secret) throws RemoteException;

    public GameSession register(String username, String secret) throws RemoteException;
}
