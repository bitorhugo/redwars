package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby extends UnicastRemoteObject implements SubjectRI {

    private UUID id;
    // private List<Player> players = new ArrayList<>();
    private List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());
    private String state;
    private String mapname;
    private TokenRing ring;
    
    public Lobby(String mapname, String username) throws RemoteException {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
    }

    @Override
    public String getMapname() throws RemoteException {
        return this.mapname;
    }

    @Override
    public UUID getID() throws RemoteException {
        return this.id;
    }

    @Override
    public List<ObserverRI> players() throws RemoteException {
        return this.observers;
    }

    public int playerCount() {
        return this.observers.size();
    }

    @Override
    public void attach(ObserverRI obs) throws RemoteException {
        this.observers.add(obs);
        System.out.println("INFO: " + obs + " added");
        System.out.println("INFO: Player Count: " + this.playerCount());
    }

    @Override
    public void detach(ObserverRI obs) throws RemoteException {
        this.observers.remove(obs);
        System.out.println("INFO: " + obs + " removed");
        System.out.println("INFO: Player Count: " + this.playerCount());
    }

    @Override
    public void startGame() throws RemoteNotEnoughPlayersException {
        if (this.check_requirements()) {
            System.out.println("INFO: Starting game");
            this.ring = new TokenRing(this.observers.size()); // initialize token ring
            this.notifyStartGame();
        }
    }

    @Override
    public synchronized void setSate(String state) throws RemoteException {
        this.state = state;
        System.out.println("State in lobby updated, notifying others");
        this.notifyObservers();
    }
    
    @Override
    public synchronized void setSate(String state, ObserverRI obs) throws RemoteException {
        if (this.ring.getTokenHolder() == this.observers.indexOf(obs)) {
            this.state = state;
            System.out.println("State in lobby updated, notifying others");
            this.notifyObservers();
        }
        if (state.compareTo("endturn") == 0) {
            this.ring.passToken();
        }
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

    private void notifyStartGame() {
        // iterate over obs list and tell them to get new state
        this.observers.forEach(o -> {
                try {
                    System.out.println("INFO: Staring game for: " + o);
                    o.startGame();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
    }

    private boolean check_requirements() throws RemoteNotEnoughPlayersException {
        int playerCount = this.playerCount();
        switch (this.mapname) {
        case "FourCorners":
            if (playerCount == 4) {return true;}
            break;
        case "SmallVs":
            if (playerCount == 2) {return true;}
            break;
        }
        throw new RemoteNotEnoughPlayersException("Not enough Players");
    }
    
}
