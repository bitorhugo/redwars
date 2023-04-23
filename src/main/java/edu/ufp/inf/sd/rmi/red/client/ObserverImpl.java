package edu.ufp.inf.sd.rmi.red.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;
import menus.MenuHandler;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private Connection conn; // connection for rabbit
    private Channel channel; // channel for rabbit work qeueu
    private String WQ_QUEUE_NAME;
    
    private String username;
    private int commander;
    private SubjectRI subject;
    private final int id;
    private Game game;
    
    public ObserverImpl(int id, String username, int commander, Game game) throws RemoteException {
        super();
        this.username = username;
        this.commander = commander;
        this.game = game;
        this.id = id;
    }

    public ObserverImpl(int id, String username, int commander, SubjectRI subject, Game game) throws RemoteException {
        this(id, username, commander, game);
        this.subject = subject;
        this.bindQueue();
    }

    @Override
    public int getId() throws RemoteException {
        return this.id;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public int getCommander() throws RemoteException {
        return this.commander;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public String getQeueuName() {
        return this.WQ_QUEUE_NAME;
    }

    public SubjectRI getSubject() {
        return this.subject;
    }

    // public String getLastObserverState() throws RemoteException {
    //     return this.subject.getSate();
    // }

    private void bindQueue() {
        // create a connection and channel to bind to work queue
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.WQ_QUEUE_NAME = this.subject.getQeueuName();
            this.conn = factory.newConnection();
            System.out.println("INFO: Connection created " + this.conn);
            this.channel = this.conn.createChannel();
            System.out.println("INFO: Channel created " + this.channel);
            channel.queueDeclare(WQ_QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }        
    }

    public void closeChannel() {
        try {
            this.channel.close();
            System.out.println("INFO: Channel closed " + this.channel);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    
    public void closeConnection() {
        try {
            this.conn.close();
            System.out.println("INFO: Connection closed " + this.conn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startGame(String EXCHANGE_NAME) throws RemoteException {
        game.startGame();
        game.isOnline = true;
        this.listen(EXCHANGE_NAME); // start listening for incoming queue messages
    }

    // @Override
    // public void update() throws RemoteException {
    //     String state = this.getLastObserverState();
    //     this.handleState(state);
    // }

    private void listen(String EXCHANGE_NAME) {
        try {
            Channel channel = this.conn.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                this.handleState(message);
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }

    private void handleState(String state) {
        if (Game.GameState == Game.State.PLAYING) {
            var ply = Game.player.get(Game.btl.currentplayer);
            switch (state) {
            case "up":
                ply.selecty--;
                if (ply.selecty < 0) {
                    ply.selecty++;
                }
                break;
            case "down":
                ply.selecty++;
                if (ply.selecty >= Game.map.height) {
                    ply.selecty--;
                }
                break;
            case "left":
                ply.selectx--;
                if (ply.selectx < 0) {
                    ply.selectx++;
                }
                break;
            case "right":
                ply.selectx++;
                if (ply.selectx >= Game.map.width) {
                    ply.selectx--;
                }
                break;
            case "select":
                Game.btl.Action();
                break;
            case "cancel":
                Game.player.get(Game.btl.currentplayer).Cancle();
                break;
            case "start":
                new menus.Pause();
                break;
            case "endturn":
                MenuHandler.CloseMenu();
                Game.btl.EndTurn();
                break;
            default:
                String[] params = state.split(":");
                Game.btl.Buyunit(Integer.parseInt(params[1]),
                                 Integer.parseInt(params[2]),
                                 Integer.parseInt(params[3]));
                MenuHandler.CloseMenu();
            }
        }        
    }
    
}
