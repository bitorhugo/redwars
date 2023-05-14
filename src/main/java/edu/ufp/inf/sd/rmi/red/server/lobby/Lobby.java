package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.server.tokenring.TokenRing;

public class Lobby {

    private String id;
    private String mapname;
    private List<String> players = new ArrayList<>();

    private transient TokenRing ring;
    private transient Channel chan;
    private transient String WQ_QUEUE_NAME = UUID.randomUUID().toString();
    private transient String FANOUT_EXCHANGE_NAME = UUID.randomUUID().toString();

    
    public Lobby(String mapname, int lobbyID) {
        super();
        this.id = String.valueOf(lobbyID);
        this.mapname = mapname;
    }

    public Lobby(Channel chan, String mapname, int lobbyID) {
        this(mapname, lobbyID);
        this.chan = chan;
    }

    public String getID() {
        return this.id;
    }
    
    public String getMapname() {
        return this.mapname;
    }

    public List<String> getPlayers() {
        return this.players;
    }

    public TokenRing getRing() {
        return this.ring;
    }
    
    public Channel getChannel() {
        return this.chan;
    }

    public String getWorkQueueName() {
        return this.WQ_QUEUE_NAME;
    }

    public void setWorkQueueName(String workQueueName) {
        this.WQ_QUEUE_NAME = workQueueName;
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


    public void startGame() throws IOException, InterruptedException, ExecutionException {
        if (this.check_requirements()) {

            this.ring = new TokenRing(Clock.systemUTC(), this.players.size());
            
            // declare work queue that server will consume from
            this.declareWorkQueue();

            // declare Fanout Exchange
            this.declareFanoutExchange();

            // notify players to start their game gui
            this.notifyStartGame();
        }
    }

    private void notifyStartGame() {
        this.players.forEach(p -> {
            try {
                // get each clients rpc
                System.out.println("Starting game for " + p);
                String uniqueRPC = "rpc-start-game-gui-" + p;
                
                // send as params: mapname, list of each players commanders, work-queue name, fanout name
                String params = this.mapname + ";" + this.WQ_QUEUE_NAME + ";" + this.FANOUT_EXCHANGE_NAME;
                
                final String corrId = UUID.randomUUID().toString();
                String replyQueueName = chan.queueDeclare().getQueue();
                AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                        .correlationId(corrId)
                        .replyTo(replyQueueName)
                        .build();
                chan.basicPublish("", uniqueRPC, props, params.getBytes("UTF-8"));
            } catch (IOException e) {e.printStackTrace();}
        });
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

    private void declareWorkQueue() {
        try {
            System.out.println("Waiting for messages");
            this.chan.queueDeclare(WQ_QUEUE_NAME, false, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String[] message = new String(delivery.getBody(), "UTF-8").split(";");

                String player = message[0];
                String command = message[1];

                System.out.println(" [x] Received 'player=" + player + "' command=" + command + "'");

                if (this.ring.getTokenHolder() == this.players.indexOf(player)) { // check to see if message comes from token holder
                    this.chan.basicPublish(FANOUT_EXCHANGE_NAME, "", null, command.getBytes("UTF-8"));
                    if (command.compareTo("endturn") == 0) {
                        this.ring.passToken();
                    }
                }
            };
            this.chan.basicConsume(this.WQ_QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            System.err.println("Not able to declare Work-Queue " + this.WQ_QUEUE_NAME);
        }
        System.out.println("INFO: Work-Queue " + this.WQ_QUEUE_NAME + " declared");
    }

    private void declareFanoutExchange() {
         try {
            this.chan.exchangeDeclare(FANOUT_EXCHANGE_NAME, "fanout");
        } catch (IOException e) {
            System.err.println("Not able to open channel for RabbitMQ");
        }
        System.out.println("INFO: Fanout Exchange declared ");
    }

    @Override
    public String toString() {
        return "{" + this.id + "," + this.playerCount() + "}";
    }

}
