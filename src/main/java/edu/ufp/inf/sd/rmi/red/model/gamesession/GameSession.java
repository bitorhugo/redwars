package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.model.db.GameDBI;
import edu.ufp.inf.sd.rmi.red.model.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private User owner;
    private GameDBI db;
    private Map<UUID, Lobby> lobbies;

    public GameSession(User owner, GameDBI db) throws RemoteException {
        super();
        owner.verifyToken();
        this.owner = owner;
        this.db = db;
    }

    public GameSession(User owner, Map<UUID, Lobby> lobbies) throws RemoteException {
        super();
        owner.verifyToken();
        this.owner = owner;
        this.lobbies = lobbies;
    }

    @Override
    public void enterLobby(UUID lobby) throws RemoteException {
        this.verifyToken();
        System.out.println(this.owner.getUsername() + " is entering lobby " + lobby);
        this.lobbies.get(lobby).addPlayers(owner.getUsername());
    }

    @Override
    public void exitLobby(UUID lobby) throws RemoteException {
        this.verifyToken();
        this.lobbies.get(lobby).removePlayer(this.owner.getUsername());
    }

    @Override
    public List<UUID> lobbies() throws RemoteException {
        return new ArrayList<>(this.lobbies.keySet());
    }

    @Override
    public Lobby lobby(UUID lobby) throws RemoteException {
        return this.lobbies.get(lobby);
    }


    @Override
    public UUID createLobby(String mapname) throws RemoteException {
        this.verifyToken();
        System.out.println(this.owner.getUsername() + " is creating a lobby");
        UUID lobbyID = UUID.randomUUID();
        this.lobbies.put(lobbyID, new Lobby(mapname, this.owner.getUsername()));
        return lobbyID;
    }

    @Override
    public void cancelLobby(UUID id) throws RemoteException {
        //this.verifyToken();
        this.lobbies.remove(id);
    }

    private void verifyToken() throws RemoteGameSessionExpiredException {
        owner.verifyToken();
    }

}
