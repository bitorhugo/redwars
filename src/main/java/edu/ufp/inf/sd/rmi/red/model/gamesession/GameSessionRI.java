package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;


public interface GameSessionRI extends Remote {
    public List<UUID> availableGames() throws RemoteException;
    public void attach() throws RemoteException;
    public void detach() throws RemoteException;
    public void createGame(UUID id) throws RemoteException;
    public void cancelGame(UUID id) throws RemoteException;
}
