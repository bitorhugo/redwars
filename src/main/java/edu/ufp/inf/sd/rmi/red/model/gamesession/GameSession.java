package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.ufp.inf.sd.rmi.red.model.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.model.lobby.SubjectRI;
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
    public SubjectRI createLobby(String mapname) throws RemoteException {
        this.verifyToken();
        Lobby l = new Lobby(mapname, this.owner.getUsername());
        System.out.println(this.owner.getUsername() + " created lobby:" + l);
        this.lobbies.put(l.getID(), l);
        return l;
    }

    @Override
    public void cancelLobby(UUID id) throws RemoteException {
        this.verifyToken();
        this.lobbies.remove(id);
    }
    
    @Override
    public SubjectRI enterLobby(UUID lobby) throws RemoteException {
        this.verifyToken();
        System.out.println(this.owner.getUsername() + " is entering lobby " + this.lobbies.get(lobby));
        var l = this.lobbies.get(lobby);
        l.addPlayers(owner.getUsername());
        return l;
    }

    @Override
    public void exitLobby(UUID lobby) throws RemoteException {
        this.verifyToken();
        this.lobbies.get(lobby).removePlayer(this.owner.getUsername());
    }

    @Override
    public List<SubjectRI> lobbies() throws RemoteException {
        return new ArrayList<>(this.lobbies.values());
    }

    @Override
    public List<SubjectRI> lobbies(String mapname) throws RemoteException {
        return this.lobbies.entrySet().stream()
                                       .filter(lobby -> {
                                        try {
                                            return lobby.getValue().getMapname().compareTo(mapname) == 0;
                                        } catch (RemoteException e1) {
                                            e1.printStackTrace();
                                        }
                                        return false;
                                    })
                                       .map(e -> e.getValue())
                                       .collect(Collectors.toList());
    }

    @Override
    public Lobby lobby(UUID lobby) throws RemoteException {
        return this.lobbies.get(lobby);
    }

    private void verifyToken() throws RemoteGameSessionExpiredException {
        owner.verifyToken();
    }

}
