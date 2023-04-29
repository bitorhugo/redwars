package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby implements SubjectRI {
    
    private UUID id;
    private String mapname;
    private transient TokenRing ring;
    
    private transient Channel chan;
    // client -> server
    private transient String WQ_QUEUE_NAME;
    // server -> client channel
    private transient String FANOUT_EXCHANGE_NAME;

    private transient List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());


    public Lobby(Channel chan, String mapname) {
        this(mapname);
        this.chan = chan;
        this.WQ_QUEUE_NAME = this.id.toString();
    }

    public Lobby(String mapname) {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
    }

    public Channel getChannel() {
        return this.chan;
    }

    public TokenRing getRing() {
        return this.ring;
    }

    @Override
    public String getQeueuName() throws RemoteException {
        return this.WQ_QUEUE_NAME;
    }

    @Override
    public String getMapname() throws RemoteException {
        return this.mapname;
    }

    @Override
    public UUID getID() {
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

            // initialize token ring
            this.ring = new TokenRing(Clock.systemUTC(), this.observers.size());

            // create publish channel
            this.FANOUT_EXCHANGE_NAME = this.id.toString();
            this.notifyStartGame();
        }
    }

    private void notifyStartGame() {
        // iterate over obs list and tell them to start the game
        this.observers.forEach(o -> {
                try {
                    System.out.println("INFO: Staring game for: " + o);
                    o.startGame(this.FANOUT_EXCHANGE_NAME);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        
        // start listening for state changes from clients
        this.listenStateChanges();
    }

    private void listenStateChanges() {
        try {
            this.chan.queueDeclare(this.WQ_QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // String is composed of {observer_index;command}
                String []message = new String(delivery.getBody(), "UTF-8").split(";");

                int obs = Integer.parseInt(message[0]);
                String msg = message[1];
                
                System.out.println(" [x] Received 'obs=" + obs + "' msg=" + msg + "'");

                if (this.ring.getTokenHolder() == obs) { // check to see if message comes from token holder
                    // send command to all clients
                    this.chan.basicPublish(FANOUT_EXCHANGE_NAME, "", null, msg.getBytes("UTF-8"));

                    if (msg.compareTo("endturn") == 0) {
                        this.ring.passToken();
                    }
                }
            };
            this.chan.basicConsume(this.WQ_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void closeChannel(Channel chan) {
        try {
            chan.close();
            System.out.println("INFO: Channel closed " + chan);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    
}
