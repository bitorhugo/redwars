package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface GameSessionRI extends Remote {
    public void enterLobby(UUID lobby) throws RemoteException;
    public void exitLobby() throws RemoteException;
    public List<UUID> lobbies() throws RemoteException;
    public UUID createLobby(String mapname) throws RemoteException;
    public void cancelLobby(UUID id) throws RemoteException;
}
