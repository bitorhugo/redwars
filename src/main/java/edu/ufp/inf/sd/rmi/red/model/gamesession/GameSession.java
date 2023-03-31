package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.ufp.inf.sd.rmi.red.model.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private User owner;
    private Map<UUID, Lobby> lobbies;

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
    public List<Lobby> lobbies() throws RemoteException {
        return new ArrayList<>(this.lobbies.values());
    }

    @Override
    public List<Lobby> lobbies(String mapname) throws RemoteException {
        return this.lobbies.entrySet().stream()
                                       .filter(lobby -> lobby.getValue().getMapname().compareTo(mapname) == 0)
                                       .map(e -> e.getValue())
                                       .collect(Collectors.toList());
    }

    @Override
    public Lobby lobby(UUID lobby) throws RemoteException {
        return this.lobbies.get(lobby);
    }


    @Override
    public UUID createLobby(String mapname) throws RemoteException {
        this.verifyToken();
        Lobby l = new Lobby(mapname, this.owner.getUsername());
        this.lobbies.put(l.getID(), l);
        System.out.println(this.owner.getUsername() + " created lobby:" + l.getID());
        return l.getID();
    }

    @Override
    public void cancelLobby(UUID id) throws RemoteException {
        this.verifyToken();
        this.lobbies.remove(id);
    }

    private void verifyToken() throws RemoteGameSessionExpiredException {
        owner.verifyToken();
    }

}
