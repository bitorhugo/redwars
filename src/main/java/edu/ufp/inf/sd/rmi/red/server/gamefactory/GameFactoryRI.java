package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSessionRI;

public interface GameFactoryRI extends Remote {
    public GameSessionRI login(String username, String secret) throws RemoteException;

    public GameSessionRI register(String username, String secret) throws RemoteException;
}
