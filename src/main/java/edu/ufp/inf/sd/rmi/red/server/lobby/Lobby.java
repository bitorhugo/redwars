package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby extends UnicastRemoteObject implements SubjectRI {

    // rabbit stuff
    private Channel channel;
    private String EXCHANGE_NAME;
    private final static String EXCHANGE_TYPE = "fanout";
    
    private UUID id;
    private List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());
    private String state;
    private String mapname;
    private TokenRing ring;

    public Lobby(Connection rabbitConnection, String mapname, String username) throws RemoteException {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
        this.EXCHANGE_NAME = this.id.toString();
        this.channel = this.createRabbitChannel(rabbitConnection).orElseThrow();
    }
    
    public Lobby(String mapname, String username) throws RemoteException {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
    }

    public Channel getRabbitChannel() {
        return this.channel;
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
            this.ring = new TokenRing(Clock.systemUTC(), this.observers.size()); // initialize token ring
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
        if (this.channel != null) { // rabbit implementation
            try {
                this.channel.basicPublish(EXCHANGE_NAME, "", null, state.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        else {
            if (this.ring.getTokenHolder() == this.observers.indexOf(obs)) {
                this.state = state;
                System.out.println("State in lobby updated, notifying others");
                this.notifyObservers();
            }
            if (state.compareTo("endturn") == 0) {
                this.ring.passToken();
            }        //TODO: Implement RemoteNotHoldingTokenException
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
        // iterate over obs list and tell them to start the game
        this.observers.forEach(o -> {
                try {
                    System.out.println("INFO: Staring game for: " + o);
                    o.startGame(this.EXCHANGE_NAME);
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

    public Optional<Channel> createRabbitChannel(Connection conn) {
        Channel chan;
        try {
            chan = conn.createChannel();
        } catch (IOException e) {
            chan = null;
            System.err.println("Not able to open channel for RabbitMQ");
        }
        return Optional.ofNullable(chan);
    }

    public void deleteRabbitChannel(Channel chan) {
        try {
            chan.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }    

    
}
