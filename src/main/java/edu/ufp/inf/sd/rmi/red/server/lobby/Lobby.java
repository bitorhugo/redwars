package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;
import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby implements SubjectRI {

    private String id;
    private String mapname;
    private List<String> players = new ArrayList<>();

    @JsonIgnore
    private transient TokenRing ring;

    @JsonIgnore
    private transient Channel chan;

    // client -> server
    @JsonIgnore
    private transient String WQ_QUEUE_NAME;

    // server -> client channel
    @JsonIgnore
    private transient String FANOUT_EXCHANGE_NAME = UUID.randomUUID().toString();

    @JsonIgnore
    private transient List<ObserverRI> observers = Collections.synchronizedList(new ArrayList<>());

    public Lobby(Channel chan, String mapname, int lobbyID) {
        this(mapname, lobbyID);
        this.chan = chan;
    }

    public Lobby(String mapname, int lobbyID) {
        super();
        this.id = String.valueOf(lobbyID);
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
    public String getMapname() {
        return this.mapname;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public List<ObserverRI> players() throws RemoteException {
        return this.observers;
    }

    public void addPlayer(String username) {
        this.players.add(username);
    }

    public void removePlayer(String username) {
        this.players.remove(username);
    }

    public boolean containsPlayer(String username) {
        return this.players.contains(username);
    }

    public int playerCount() {
        return this.players.size();
    }

    public List<String> getPlayers() {
        return this.players;
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
            // initialize token ring
            this.ring = new TokenRing(Clock.systemUTC(), this.observers.size());
            this.declareFanoutExchange();
            this.notifyStartGame();
        }
    }

    private void declareFanoutExchange() {
         try {
            this.chan.exchangeDeclare(FANOUT_EXCHANGE_NAME, "fanout");
        } catch (IOException e) {
            System.err.println("Not able to open channel for RabbitMQ");
        }
        System.out.println("INFO: Fanout Exchange declared ");
    }

    private void notifyStartGame() {
        this.players.forEach(p -> {
            try {
                String rpc = "rpc-start-game-gui-" + p;
                String param = this.mapname + ";" + this.FANOUT_EXCHANGE_NAME;
                final String corrId = UUID.randomUUID().toString();
                String replyQueueName = chan.queueDeclare().getQueue();
                AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                        .correlationId(corrId)
                        .replyTo(replyQueueName)
                        .build();
                chan.basicPublish("", rpc, props, param.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.listenStateChanges();
    }

    private void listenStateChanges() {
        try {
            //TODO: make a rpc call to ask for WQ name
            this.chan.queueDeclare(this.WQ_QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // String is composed of {observer_index;command}
                String[] message = new String(delivery.getBody(), "UTF-8").split(";");

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
            this.chan.basicConsume(this.WQ_QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean check_requirements() throws RemoteNotEnoughPlayersException {
        int playerCount = this.playerCount();
        switch (this.mapname) {
            case "FourCorners":
                if (playerCount == 4) {
                    return true;
                }
                break;
            case "SmallVs":
                if (playerCount == 2) {
                    return true;
                }
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

    @Override
    public String toString() {
        return "{" + this.id + "," + this.playerCount() + "}";
    }

}
