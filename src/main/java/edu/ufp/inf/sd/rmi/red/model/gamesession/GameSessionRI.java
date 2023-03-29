package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameSessionRI extends Remote {
    public void attach() throws RemoteException;
    public void detach() throws RemoteException;
    public List<Integer> availableGames() throws RemoteException;
    public int createGame(String mapname) throws RemoteException;
    public void cancelGame(int id) throws RemoteException;
}
