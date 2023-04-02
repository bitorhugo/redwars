package edu.ufp.inf.sd.rmi.red.server.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;

public interface GameSessionRI extends Remote {
    public SubjectRI createLobby(String mapname) throws RemoteException;
    public void cancelLobby(UUID id) throws RemoteException;
    // public SubjectRI enterLobby(UUID lobby) throws RemoteException;
    // public void exitLobby(UUID lobby) throws RemoteException;
    public List<SubjectRI> lobbies() throws RemoteException;
    public List<SubjectRI> lobbies(String mapname) throws RemoteException;
    public SubjectRI lobby(UUID lobby) throws RemoteException;

}
