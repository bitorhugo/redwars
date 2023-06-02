package edu.ufp.inf.sd.rmi.red.server;

import edu.ufp.inf.sd.rmi.red.model.db.VolatileDB;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.server.queuenames.rpc.RPCEnum;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class RedServer {

    private transient Connection conn;
    private transient Channel chan;
    
    private static final String AUTHEXCHANGENAME = "auth";
    private static final String LOBBIESEXCHANGENAME = "lobbies";
    
    private VolatileDB db = new VolatileDB();
    private Map<String, Lobby> lobbies = Collections.synchronizedMap(new HashMap<>());

    /**
     * @param args 
     */
    public RedServer (String args[]) {
        // First create a rabbit connection
        this.connectToBroker(args[0]);
        // listen for Auth_Queue
        this.listenAuth();
        // listen for Lobbies_Queue
        this.listenLobbies();
    }

    public VolatileDB getDB() {
        return this.db;
    }

    public Map<String, Lobby> getLobbies() {
        return this.lobbies;
    }

    public void setLobbies(Map<String, Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    private void connectToBroker (String host) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        try {
            this.conn = factory.newConnection();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Connection {0} created.", this.conn);
            this.chan = this.conn.createChannel();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Channel {0} created.", this.chan);
        } catch (IOException | TimeoutException e) {
            System.err.println("ERROR: Not able to open connection with RabbitMQ Services");
            System.exit(-1);
        }
    }

    private void listenAuth() {
        try {
            this.chan.exchangeDeclare(AUTHEXCHANGENAME, "fanout");
            String queueName = this.chan.queueDeclare().getQueue();
            this.chan.queueBind(queueName, AUTHEXCHANGENAME, "");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Exchange {0} created", AUTHEXCHANGENAME);

            DeliverCallback deliverCallbackFanout = (consumerTag, delivery) -> {
                String[] message = new String(delivery.getBody(), "UTF-8").split(";");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "EXCHANGE AUTH: Received {0}", Arrays.asList(message));
                try {
                    this.handleAuth("register", message[0], message[1]); // register;username;secret
                } catch (RemoteUserNotFoundException | RemoteUserAlreadyRegisteredException e) {
                    e.printStackTrace();
                }
            };
            this.chan.basicConsume(queueName, true, deliverCallbackFanout, consumerTag -> { });
            
            this.LoginRPC();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    private void listenLobbies() {
        try {
            this.chan.exchangeDeclare(LOBBIESEXCHANGENAME, "fanout");
            String queueName = this.chan.queueDeclare().getQueue();
            this.chan.queueBind(queueName, LOBBIESEXCHANGENAME, "");
            DeliverCallback deliverFanoutCallback = (consumerTag, delivery) -> {
                String []message = new String(delivery.getBody(), "UTF-8").split(";");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "EXCHANGE LOBBIES: Received {0}", Arrays.asList(message));
                this.handleLobbies(message);
            };

            this.chan.basicConsume(queueName, true, deliverFanoutCallback, consumerTag -> { });

            // listen for rpc calls
            this.searchLobbiesRPC();
            this.getPlayersRPC();
            this.startGameRPC();
            this.checkLobbyFullRPC();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleAuth(String action, String username, String secret) throws RemoteUserNotFoundException, RemoteUserAlreadyRegisteredException {
        try {
            String message;
            switch (action) {
            case "login":
                this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
                this.chan.queueDeclare(username, false, false, false, null);
                message = "ok";
                this.chan.basicPublish("", username, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
                break;
            case "register":
                this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
                // this.chan.queueDeclare(username, false, false, false, null);
                message = "ok";
                this.chan.basicPublish("", username, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLobbies(String[] message) {
        String action = message[0];
        String username = null;
        String mapname = null;
        String cmd = null;
        String lobbyID = null;

        Lobby l = null;
        
        switch (action) {

        case "new": // message = new;username;mapname
            username = message[1];
            mapname = message[2];
            cmd = message[3];
            l = new Lobby(this.chan, mapname, this.lobbies.size());
            l.addPlayer(username);
            this.lobbies.put(l.getID(), l);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "LOBBY {0} created", l.getID());
            break;
            
        case "join":
            lobbyID = message[1];
            username = message[2];
            l = this.lobbies.get(lobbyID);
            l.addPlayer(username);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Player {0} joined lobby {1}", new Object[]{username, lobbyID});
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Players inside lobby {0}: {1} ", new Object[]{lobbyID, l.getPlayers()});
            break;

        case "removePlayer":
            username = message[1];
            String tmp = username;
            var lobbies1 = this.lobbies.entrySet().stream()
                .filter(set -> {
                        return set.getValue().containsPlayer(tmp);
                    })
                .map(e -> e.getValue())
                .collect(Collectors.toList());
            l = lobbies1.get(0);
            l.removePlayer(username);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Player {0} removed",
                                                            username);
            if (l.playerCount() == 0) {
                this.lobbies.remove(l.getID());
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lobby {0} removed",
                                                                l.getID());
            }
            break;
        }
    }

    private void searchLobbiesRPC() {
        try {
            this.chan.queueDeclare(RPCEnum.RPC_SEARCH_LOBBIES.getValue(), false, false, false, null);
            this.chan.queuePurge(RPCEnum.RPC_SEARCH_LOBBIES.getValue());

            this.chan.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

                String response = "";
                try {
                    String mapname = new String(delivery.getBody(), "UTF-8");
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Searching for {0} lobbies", mapname);
                    var lobbies = this.lobbies.entrySet().stream()
                        .filter(set -> {
                                return set.getValue().getMapname().compareTo(mapname) == 0;
                            })
                        .map(e -> e.getValue())
                        .collect(Collectors.toList());
                    for (var l : lobbies) {
                        response += l.getID() + "," + l.playerCount() + ",";
                    }
                    System.out.println(response);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e);
                } finally {
                    this.chan.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    this.chan.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            this.chan.basicConsume(RPCEnum.RPC_SEARCH_LOBBIES.getValue(), false, deliverCallback, (consumerTag -> {}));
        } catch (Exception e) {
        }
    }

    private void LoginRPC() {
        try {
            this.chan.queueDeclare(RPCEnum.RPC_LOGIN.getValue(), false, false, false, null);
            this.chan.queuePurge(RPCEnum.RPC_LOGIN.getValue());

            this.chan.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

                String response = "";
                try {
                    String []cred = new String(delivery.getBody(), "UTF-8").split(";");
                    String username = cred[0];
                    String secret = cred[1];
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Searching for user: {0}", username);
                    this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
                    response += "ok";
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e);
                } finally {
                    this.chan.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    this.chan.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            this.chan.basicConsume(RPCEnum.RPC_LOGIN.getValue(), false, deliverCallback, (consumerTag -> {}));
        } catch (Exception e) {}        
    }

    private void getPlayersRPC() {
        try {
            this.chan.queueDeclare(RPCEnum.RPC_GET_PLAYERS.getValue(), false, false, false, null);
            this.chan.queuePurge(RPCEnum.RPC_GET_PLAYERS.getValue());

            this.chan.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

                String response = "";
                try {
                    String username = new String(delivery.getBody(), "UTF-8");
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Retrieving players from lobby where username {0} is inside", username);
                    var lobby = this.lobbies.entrySet().stream()
                        .filter(set -> {
                                return set.getValue().containsPlayer(username);
                            })
                        .map(e -> e.getValue())
                        .collect(Collectors.toList()).get(0);

                    for (var p : lobby.getPlayers()) {
                        response += p + ";";
                    }
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Sending {0}", response);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e);
                } finally {
                    this.chan.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    this.chan.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            this.chan.basicConsume(RPCEnum.RPC_GET_PLAYERS.getValue(), false, deliverCallback, (consumerTag -> {}));
        } catch (Exception e) {}        
    }

    private void checkLobbyFullRPC() {
        try {
            this.chan.queueDeclare(RPCEnum.RPC_CHECK_LOBBY_FULL.getValue(), false, false, false, null);
            this.chan.queuePurge(RPCEnum.RPC_CHECK_LOBBY_FULL.getValue());

            this.chan.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

                String response = "";
                try {
                    String lobbyID = new String(delivery.getBody(), "UTF-8");
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Checking lobby ", lobbyID);

                    if (!this.lobbies.get(lobbyID).isFull()) {
                        response += "ok";
                    }
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Sending {0}", response);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e);
                } finally {
                    this.chan.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    this.chan.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            this.chan.basicConsume(RPCEnum.RPC_CHECK_LOBBY_FULL.getValue(), false, deliverCallback, (consumerTag -> {}));
        } catch (Exception e) {}        
    }

    private void startGameRPC() {
        try {
            this.chan.queueDeclare(RPCEnum.RPC_START_GAME.getValue(), false, false, false, null);
            this.chan.queuePurge(RPCEnum.RPC_START_GAME.getValue());

            this.chan.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

                String response = "";
                try {
                    String username = new String(delivery.getBody(), "UTF-8");
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received {0}", username);
                    var lobby = this.lobbies.entrySet().stream()
                        .filter(set -> {
                                return set.getValue().containsPlayer(username);
                            })
                        .map(e -> e.getValue())
                        .collect(Collectors.toList()).get(0);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting game for {0}", lobby.getPlayers());
                    lobby.startGame(); // start game for all clients
                } catch (RuntimeException | InterruptedException | ExecutionException e) {
                    System.out.println(" [.] " + e);
                } finally {
                    this.chan.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    this.chan.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            this.chan.basicConsume(RPCEnum.RPC_START_GAME.getValue(), false, deliverCallback, (consumerTag -> {}));
        } catch (IOException e) {
            e.printStackTrace();
        }                
    }
    
    public static void main(String[] args) {
        if (args != null && args.length < 3) {
            System.err.println("usage: java [options] <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        }
        new RedServer(args);
    }
    
}

