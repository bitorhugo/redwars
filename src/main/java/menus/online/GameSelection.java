package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.client.exchange.ExchangeEnum;
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
    private Map<String, String> lobbyNames = new HashMap<>();

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
            // open queue for receiving response from server
            Map<String, Object> args = new HashMap<>();
            args.put("x-expires", 60000); // queue time-to-live = 60s
            Game.chan.queueDeclare(Game.u, false, false, false, args);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String[] response = new String(delivery.getBody(), "UTF-8").split(";");
                String status = response[0];
                String[] lobbies = response[1].split(",");
                switch (status) {
                case "ok":
                    for (var lobby: lobbies) {
                        int playerCount = 0;
                        String displayMsg = this.displayMsg(mapname, playerCount);
                            this.lobbyNames.put(displayMsg, lobby);
                            lobbiesList.addElement(displayMsg);
                    }
                        Game.chan.queueDelete(Game.u);
                    break;
                default:
                }
            };
            Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });
            
            // query the server for lobbies
            String msg = "search" + ";" + mapname + ";" + Game.u;
            Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));
            System.out.println("INFO: Success! Message " + msg + " sent to Exchange LOBBIES.");
            
            // Game.session.lobbies(mapname).forEach(lobby -> {
            //         try {
            //             int playerCount = lobby.players().size();
            //             String displayMsg = this.displayMsg(mapname, playerCount);
            //         this.lobbyNames.put(displayMsg, lobby);
            //         lobbiesList.addElement(displayMsg);
            //         } catch (RemoteException e) {
            //             e.printStackTrace();
            //         }
            //     });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lobbiesList;
    }

    private String displayMsg(String mapname, int playerCount) {
        switch (mapname) {
            case "FourCorners":
                return "(" + this.lobbyNames.size() + ")(" + playerCount + "/4)";
            case "SmallVs":
                return "(" + this.lobbyNames.size() + ")(" + playerCount + "/2)";
        }
        return "(" + this.lobbyNames.size() + ")(" + playerCount + ")";
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            MenuHandler.CloseMenu();
            Game.gui.MenuScreen();
        }

        if (s == this.Enter) {
            String selected = this.availableGamesList.getSelectedValue();
            // UUID l = this.lobbyNames.get(selected);
            // Game.lobby = Game.session.lobby(l);
            // System.out.println("INFO: Selected lobby " + Game.lobby);
            // int id = Game.lobby.players().size();
            // Game.obs = new ObserverImpl(id, Game.u, Game.cmd, Game.lobby, Game.g);
            // Game.lobby.attach(Game.obs);
            // new WaitQueueMenu();
            // } catch (RemoteException e1) {
            //     e1.printStackTrace();
            // }
        }
        
        if (s == this.Refresh) {
            new GameSelection(mapname);
        }
    }
    
}
