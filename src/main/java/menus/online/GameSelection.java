package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.server.queuenames.exchange.ExchangeEnum;
import edu.ufp.inf.sd.rmi.red.server.queuenames.rpc.RPCEnum;
import engine.Game;
import menus.MenuHandler;


public class GameSelection implements ActionListener {

    public JButton Enter = new JButton("Enter");
    public JButton Return = new JButton("Return");
    public JButton Refresh = new JButton("Refresh");
    public JLabel PanelInfo = new JLabel("Game Selection");
    public JList<String> availableGamesList;
    private JScrollPane lobbiesPane;
    private String mapname;
    private Map<String, String> lobbiesNames = new HashMap<>();

    public GameSelection(String mapname) {
        this.mapname = mapname;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.gameList(size);
    }

    private void SetBounds(Point size) {
        this.Enter.setBounds(size.x, size.y + 10, 100, 32);
		this.Refresh.setBounds(size.x,size.y+10+38*1, 100, 32);
        this.Return.setBounds(size.x, size.y+10+38*2, 100, 32);
        this.panelInfoSetup();
	}

    private void panelInfoSetup() {
        this.PanelInfo.setForeground(new Color(255, 0, 0));
		Insets insets = Game.gui.getInsets();
		Dimension size = Game.gui.getPreferredSize();
		Dimension size2 = PanelInfo.getPreferredSize();
		//sets the size and what not of the text to be shown and what not
		this.PanelInfo.setBounds(8, 2, size2.width, size2.height);
		this.PanelInfo.setBounds(size.width/2+insets.left-size2.width/2-8,insets.top+8, size2.width+16, size2.height+8);
	}

    private void addGui() {
        Game.gui.add(this.Enter);
        Game.gui.add(this.Refresh);
        Game.gui.add(this.Return);
        Game.gui.add(this.PanelInfo);
    }

    private void addActionListeners() {
        this.Enter.addActionListener(this);
        this.Return.addActionListener(this);
        this.Refresh.addActionListener(this);
    }

    private void gameList(Point size) {
        this.lobbiesPane = new JScrollPane(this.availableGamesList =
                                           new JList<>( this.availableGames(this.mapname) ));
        this.lobbiesPane.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(lobbiesPane);
        
		this.availableGamesList.setBounds(0, 0, 140, 260);
		this.availableGamesList.setSelectedIndex(0);
	}

    private DefaultListModel<String> availableGames(String mapname) {
        DefaultListModel<String> lobbiesList = new DefaultListModel<>();
        try {
            
            String []lobbies = call().split(",");
            for (int i = 0; i < lobbies.length; i += 2) {
                String id = lobbies[i];
                String playerCount = lobbies[i + 1];
                this.lobbiesNames.put(id, "");
                String dsp = this.displayMsg(mapname, Integer.parseInt(playerCount));
                this.lobbiesNames.put(dsp, id);
                lobbiesList.addElement(dsp);
                System.out.println(lobbies);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lobbiesList;
    }

    private String displayMsg(String mapname, int playerCount) {
        switch (mapname) {
            case "FourCorners":
                return "(" + this.lobbiesNames.size() + ")(" + playerCount + "/4)";
            case "SmallVs":
                return "(" + this.lobbiesNames.size() + ")(" + playerCount + "/2)";
        }
        return "(" + this.lobbiesNames.size() + ")(" + playerCount + ")";
    }

    private String call() throws IOException, InterruptedException, ExecutionException {

            String param = mapname;
            
            final String corrId = UUID.randomUUID().toString();
            String replyQueueName = Game.chan.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

            Game.chan.basicPublish("", RPCEnum.RPC_SEARCH_LOBBIES.getValue(), props, param.getBytes("UTF-8"));

            final CompletableFuture<String> response = new CompletableFuture<>();

            String ctag = Game.chan.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        response.complete(new String(delivery.getBody(), "UTF-8"));
                    }
                }, consumerTag -> {
                });

            String result = response.get();
            Game.chan.basicCancel(ctag);
            return result;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            MenuHandler.CloseMenu();
            Game.gui.MenuScreen();
        }

        if (s == this.Enter) {
            try {
            String selected = this.availableGamesList.getSelectedValue();
            var id = this.lobbiesNames.get(selected);
            Game.chan.exchangeDeclare(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "fanout");
            String msg = "join" + ";" + id  + ";" + Game.u;
            Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));
            System.out.println("INFO: Success! Message " + msg + " sent to Exchange LOBBIES.");

            new WaitQueueMenu();
            } catch (Exception e1) {
            }
        }
        
        if (s == this.Refresh) {
            new GameSelection(mapname);
        }
    }
    
}
