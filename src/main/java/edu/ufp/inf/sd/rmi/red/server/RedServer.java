package edu.ufp.inf.sd.rmi.red.server;

import edu.ufp.inf.sd.rmi.red.model.db.VolatileDB;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RedServer implements Serializable {

    private transient Connection conn;
    private transient Channel chan;
    
    private static final String AUTHEXCHANGENAME = "auth";
    private static final String LOBBIESEXCHANGENAME = "lobbies";

    private static final String LOGINQUEUENAME = "login";
    private static final String SEARCHLOBBYNAME = "search_lobby";
    
    private VolatileDB db = new VolatileDB();
    private Map<UUID, Lobby> lobbies = Collections.synchronizedMap(new HashMap<>());

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

    public Map<UUID, Lobby> getLobbies() {
        return this.lobbies;
    }

    public void setLobbies(Map<UUID, Lobby> lobbies) {
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

            this.chan.queueDeclare(LOGINQUEUENAME, false, false, false, null);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Work-Queue {0} created", LOGINQUEUENAME);

            DeliverCallback deliverCallbackFanout = (consumerTag, delivery) -> {
                String[] message = new String(delivery.getBody(), "UTF-8").split(";");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "EXCHANGE AUTH: Received {0}", Arrays.asList(message));

                try {
                    this.handleAuth("register", message[0], message[1]);
                } catch (RemoteUserNotFoundException | RemoteUserAlreadyRegisteredException e) {
                    e.printStackTrace();
                }
            };

            DeliverCallback deliverCallbackWorkQueue = (consumerTag, delivery) -> {
                String[] message = new String(delivery.getBody(), "UTF-8").split(";");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "WORK-QUEUE Login: Received {0}", Arrays.asList(message));
                try {
                    this.handleAuth("login", message[0], message[1]);
                } catch (RemoteUserNotFoundException | RemoteUserAlreadyRegisteredException e) {
                    e.printStackTrace();
                }
            };

            // Consume from fanout exchange
            this.chan.basicConsume(queueName, true, deliverCallbackFanout, consumerTag -> { });

            // Consume from Work-Queue
            boolean autoAck = true;
            this.chan.basicConsume(LOGINQUEUENAME, autoAck, deliverCallbackWorkQueue, consumerTag -> {});
            
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    private void listenLobbies() {
        try {
            this.chan.exchangeDeclare(LOBBIESEXCHANGENAME, "fanout");
            String queueName = this.chan.queueDeclare().getQueue();
            this.chan.queueBind(queueName, LOBBIESEXCHANGENAME, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String []message = new String(delivery.getBody(), "UTF-8").split(";");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "EXCHANGE LOBBIES: Received {0}", Arrays.asList(message));
                this.handleLobbies(message);
            };
            
            this.chan.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
            
        } catch (IOException e){
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
                this.chan.queueDeclare(username, false, false, false, null);
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

            String response;
            switch (action) {

            case "new":
                username = message[1];
                mapname = message[2];
                
                Lobby l = new Lobby(this.chan, mapname);
                l.addPlayer(username);
                this.lobbies.put(l.getID(), l);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "LOBBY {0} created", l.getID());

                this.chan.queueDeclare(username, false, false, false, null);
                response = "ok;" + l.getID().toString();
                this.chan.basicPublish("", username, null, response.getBytes("UTF-8"));
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent", response);
                break;
                
            case "join":
                lobbyID = message[1];
                username = message[2];

                var lo = this.lobbies.get(UUID.fromString(lobbyID));
                lo.addPlayer(username);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Player {0} joined", username);

                this.chan.queueDeclare(username, false, false, false, null);
                response = "ok";
                this.chan.basicPublish("", username, null, response.getBytes("UTF-8"));
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent", response);
                break;
                
            case "search":
                mapname = message[1];
                username = message[2];
                
                this.chan.queueDeclare(username, false, false, false, null);
                response = "ok" + ";";
                if (!this.lobbies.isEmpty()) {
                    for (var lobby: this.lobbies.values()) {
                        response += lobby.getID() + "," + lobby.playerCount();
                    }
                }
                this.chan.basicPublish("", username, null, response.getBytes("UTF-8"));
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent", response);
                break;

            case "getPlayers":
                lobbyID = message[1];
                username = message[2];
                
                this.chan.queueDeclare(username, false, false, false, null);
                response = "ok";
                var players = this.lobbies.get(UUID.fromString(lobbyID)).getPlayers();
                for (var p : players) {
                    response += ";" + p;                    
                }
                this.chan.basicPublish("", username, null, response.getBytes("UTF-8"));
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Message {0} sent", response);
                break;
                
            case "delete":
                break;
                
            }
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

