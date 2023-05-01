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
                    this.handleAuth("register", message[0], message[1]);
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

            this.searchLobbiesRPC();
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
        try {
            String action = message[0];
            String username = null;
            String mapname = null;
            String lobbyID = null;

            Lobby l = null;
            String response = null;
            
            switch (action) {

            case "new": // message = new;username;mapname
                username = message[1];
                mapname = message[2];
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
                break;

            case "getPlayers":
                lobbyID = message[1];
                username = message[2];
                
                // this.chan.queueDeclare(username, false, false, false, null);
                response = "ok";
                for (var p : this.lobbies.get(lobbyID).getPlayers()) {
                    response += ";" + p;                    
                }
                this.chan.basicPublish("", username, null, response.getBytes("UTF-8"));
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent", response);
                break;

            case "startGame":
                // lobbyID = message[1];
                // username = message[2];
                // response = "startGame;";
                // for (var pls : this.lobbies.get(lobbyID).getPlayers()) {
                //     // this.chan.queueDeclare(username, false, false, false, null);
                //     // this.chan.basicPublish("", pls, null, response.getBytes("UTF-8"));
                //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent to player {1}", new Object[]{response, pls});
                // }
                // Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Game from lobby {0} starting.", lobbyID);
                break;
                
            case "delete":
                lobbyID = message[1];
                username = message[2];
                l = this.lobbies.get(lobbyID);
                if (l.getPlayers().size() == 1) {
                    this.lobbies.remove(lobbyID);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lobby {0} removed", lobbyID);
                }
                else {
                    l.removePlayer(username);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Player {0} removed from Lobby {1}", new Object[]{username, lobbyID});
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                        .filter(lobby -> {
                                return lobby.getValue().getMapname().compareTo(mapname) == 0;
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
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Searching for {0} lobbies");
                    this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
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

    public static void main(String[] args) {
        if (args != null && args.length < 3) {
            System.err.println("usage: java [options] <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        }
        new RedServer(args);
    }
    
}

