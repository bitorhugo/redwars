package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;
import menus.MenuHandler;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private String username;
    private int commander;
    private SubjectRI subject;
    private Game game;
    
    public ObserverImpl(SubjectRI subject) throws RemoteException {
        super();
        this.subject = subject;
    }

    public ObserverImpl(String username, int commander, SubjectRI subject, Game game) throws RemoteException {
        super();
        this.username = username;
        this.commander = commander;
        this.subject = subject;
        this.game = game;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public int getCommander() throws RemoteException {
        return this.commander;
    }    

    public SubjectRI getSubject() {
        return this.subject;
    }

    public String getLastObserverState() throws RemoteException {
        return this.subject.getSate();
    }

    @Override
    public void startGame(String EXCHANGE_NAME) throws RemoteException {
        game.startGame();
        game.isOnline = true;
        this.listen(EXCHANGE_NAME); // start listening for incoming queue messages
    }

    @Override
    public void update() throws RemoteException {
        String state = this.getLastObserverState();
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
                // TODO: Find a way to handle buy state
                String[] params = state.split(":");
                Game.btl.Buyunit(Integer.parseInt(params[1]),
                                 Integer.parseInt(params[2]),
                                 Integer.parseInt(params[3]));
                MenuHandler.CloseMenu();
            }
        }
    }

    public void listen(String EXCHANGE_NAME) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }
    
}
