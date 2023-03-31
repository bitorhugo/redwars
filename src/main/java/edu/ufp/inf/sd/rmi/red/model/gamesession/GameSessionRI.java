package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.model.lobby.Lobby;

public interface GameSessionRI extends Remote {
    public void enterLobby(UUID lobby) throws RemoteException;
    public void exitLobby(UUID lobby) throws RemoteException;
    public List<Lobby> lobbies() throws RemoteException;
    public List<Lobby> lobbies(String mapname) throws RemoteException;
    public Lobby lobby(UUID lobby) throws RemoteException;
    public UUID createLobby(String mapname) throws RemoteException;
    public void cancelLobby(UUID id) throws RemoteException;
    public void attach(UUID looby) throws RemoteException;
    public void detach() throws RemoteException;
    public void setState(UUID lobby) throws RemoteException;
}
