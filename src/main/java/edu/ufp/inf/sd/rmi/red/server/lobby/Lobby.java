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
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.server.tokenring.RemoteNotCurrentTokenHolderException;
import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby extends UnicastRemoteObject implements SubjectRI {

    // client -> server channel
    private Channel channelClientServer;
    private String WQ_QUEUE_NAME;

    // server -> client channel
    private Channel channelServerClient;
    private String FANOUT_EXCHANGE_NAME;
    private final static String FANOUT_EXCHANGE_TYPE = "fanout";
    
    
    private UUID id;
    private List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());
    private String state;
    private String mapname;
    private TokenRing ring;

    public Lobby(Connection conn, String mapname, String username) throws RemoteException {
        this(mapname, username);

        // create client -> server channel
        this.WQ_QUEUE_NAME = this.id.toString();
        this.channelClientServer = this.createClientServerChannel(conn).orElseThrow();

        // create server -> client channel
        this.FANOUT_EXCHANGE_NAME = this.id.toString();
        this.channelServerClient = this.createRabbitChannel(conn).orElseThrow();
    }
    
    public Lobby(String mapname, String username) throws RemoteException {
        super();
        this.id = UUID.randomUUID();
        this.mapname = mapname;
    }

    public Channel getRabbitChannel() {
        return this.channelServerClient;
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

    // @Override
    // public synchronized void setSate(String state) throws RemoteException {
    //     this.state = state;
    //     System.out.println("State in lobby updated, notifying others");
    //     this.notifyObservers();
    // }
    
    // @Override
    // public synchronized void setSate(String state, ObserverRI obs) throws RemoteException {
    //     if (this.ring.getTokenHolder() == this.observers.indexOf(obs)) {
    //         if (this.channelServerClient != null) { // rabbit implementation
    //             try {
    //                 this.channelServerClient.basicPublish(FANOUT_EXCHANGE_NAME, "", null, state.getBytes("UTF-8"));
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //             }
    //         } else {
    //             this.state = state;
    //             System.out.println("State in lobby updated, notifying others");
    //             this.notifyObservers();

    //         }
    //         if (state.compareTo("endturn") == 0) {
    //             this.ring.passToken();
    //         }
    //     }
    //     else {
    //         throw new RemoteNotCurrentTokenHolderException("Your are not holding the token");
    //     }
    // }

    // @Override
    // public String getSate() throws RemoteException {
    //     return this.state;
    // }

    // private void notifyObservers() {
    //     // iterate over obs list and tell them to get new state
    //     this.observers.forEach(o -> {
    //             try {
    //                 System.out.println("Updating state..");
    //                 o.update();
    //             } catch (RemoteException e) {
    //                 e.printStackTrace();
    //             }
    //         });
    // }

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
            this.channelClientServer.queueDeclare(this.WQ_QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // String is composed of {observer_index;command}
                String []message = new String(delivery.getBody(), "UTF-8").split(";");

                int obs = Integer.parseInt(message[0]);
                String msg = message[1];
                
                System.out.println(" [x] Received 'obs=" + obs + "' msg=" + msg + "'");

                if (this.ring.getTokenHolder() == obs) { // check to see if message comes from token holder
                    // send command to all clients
                    this.channelServerClient.basicPublish(FANOUT_EXCHANGE_NAME, "", null, msg.getBytes("UTF-8"));

                    if (msg.compareTo("endturn") == 0) {
                        this.ring.passToken();
                    }
                }
                
                
            };
            this.channelClientServer.basicConsume(this.WQ_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
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

    public Optional<Channel> createClientServerChannel(Connection conn) {
        Channel chan;
        try {
            chan = conn.createChannel();
        } catch(IOException e) {
            chan = null;
            System.err.println("Not able to open channel for RabbitMQ");
        }
        return Optional.ofNullable(chan);
    }

    public Optional<Channel> createRabbitChannel(Connection conn) {
        Channel chan;
        try {
            chan = conn.createChannel();
            chan.exchangeDeclare(FANOUT_EXCHANGE_NAME, FANOUT_EXCHANGE_TYPE);
        } catch (IOException e) {
            chan = null;
            System.err.println("Not able to open channel for RabbitMQ");
        }
        System.out.println("INFO: Channel created " + chan);
        return Optional.ofNullable(chan);
    }

    public void deleteRabbitChannel(Channel chan) {
        try {
            chan.close();
            System.out.println("INFO: Channel closed " + chan);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }    

    
}
