package edu.ufp.inf.sd.rmi.red.model.lobby;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;

public class Lobby extends UnicastRemoteObject implements SubjectRI {

    private UUID id;
    private List<String> players = new ArrayList<>();
    private List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());
    private String state;
    private String mapname;
    
    public Lobby(String mapname, String player) throws RemoteException {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
        this.players.add(player);
    }

    @Override
    public String getMapname() throws RemoteException {
        return this.mapname;
    }

    @Override
    public UUID getID() throws RemoteException {
        return this.id;
    }

    public void addPlayers(String username) {
        switch (mapname) {
        case "SmallVs":
            this.addPlayerSmallMap(username);
            break;
        case "FourCorners":
            this.addPlayerBigMap(username);
            break;
        }
    }

    // CAUTION: DURING DEV, ADDPLAYERS WILL ALLOW THE OWNER OF THE LOBBY TO ENTER AS ANTOHER PLAYER!!!!!
    private void addPlayerSmallMap(String username) {
        if (!this.players.contains(username) &&
            this.players.size() < 2) {
            this.players.add(username);
            System.out.println(username + " in lobby");
        }
    }

    private void addPlayerBigMap(String username)  {
        if (!this.players.contains(username) &&
            this.players.size() < 4) {
            this.players.add(username);
            System.out.println(username + " in lobby");
        }        
    }
    
    public void removePlayer(String username)  {
        if (this.players.contains(username)) {
            this.players.remove(username);
        }
    }

    @Override
    public List<String> players() throws RemoteException {
        return this.players;
    }

    public void updateObservers() {
        
    }

    @Override
    public void attach(ObserverRI obs) throws RemoteException {
        this.observers.add(obs);
        System.out.println(this.observers.size());
        System.out.println("Attached");
    }

    @Override
    public void detach(ObserverRI obs) throws RemoteException {
        this.observers.remove(obs);
    }

    @Override
    public synchronized void setSate(String state) throws RemoteException {
        this.state = state;
        System.out.println("State in lobby updated, notifying others");
        this.notifyObservers();
    }

    @Override
    public String getSate() throws RemoteException {
        return this.state;
    }


    private void notifyObservers() {
        // iterate over obs list and tell them to get new state
        this.observers.forEach(o -> {
                try {
                    System.out.println("Updating state..");
                    o.update();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
    }
    
}
