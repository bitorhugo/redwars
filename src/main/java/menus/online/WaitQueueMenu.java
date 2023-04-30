package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


public class WaitQueueMenu implements ActionListener {

    public JButton Start = new JButton("Start");
    public JButton Return = new JButton("Return");
    public JButton Refresh = new JButton("Refresh");
    public JLabel PanelInfo = new JLabel("WaitQueue");
    public JList<String> playersInQeueu;

    public WaitQueueMenu() {
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.playerList(size);
    }

    private void SetBounds(Point size) {
		this.Start.setBounds(size.x, size.y+10, 100, 32);
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
		PanelInfo.setBounds(8, 2, size2.width, size2.height);
		PanelInfo.setBounds(size.width/2+insets.left-size2.width/2-8,insets.top+8, size2.width+16, size2.height+8);
	}

    private void addGui() {
        Game.gui.add(this.Start);
        Game.gui.add(this.Refresh);
        Game.gui.add(this.Return);
        Game.gui.add(this.PanelInfo);
    }

    private void addActionListeners() {
        this.Start.addActionListener(this);
        this.Return.addActionListener(this);
        this.Refresh.addActionListener(this);
    }

    private void playerList(Point size) {
        JScrollPane pls = new JScrollPane(this.playersInQeueu = new JList<>(players()));
        pls.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(pls);
		this.playersInQeueu.setBounds(0, 0, 140, 260);
		this.playersInQeueu.setSelectedIndex(0);
	}

    private DefaultListModel<String> players() {
        DefaultListModel<String> pls = new DefaultListModel<>();
        try {
            // open queue for receiving response from server
            Game.chan.queueDeclare(Game.u, false, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String[] response = new String(delivery.getBody(), "UTF-8").split(";");
                String status = response[0];
                switch (status) {
                case "ok":
                    for (int i = 1; i < response.length; ++i) {
                        pls.addElement(response[i]);
                    }
                    Game.chan.queueDelete(Game.u);
                    break;
                default:
                }
            };
            Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });
            
            // query the server for lobbies
            String msg = "getPlayers" + ";" + Game.lobbyID + ";" + Game.u;
            System.out.println(Game.lobbyID);
            Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pls;
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            // try {
            //     // if lobby has only one player, cancel lobby
            //     if (Game.lobby.players().size() == 1) {
            //         // close client channel connection
            //         Game.obs.closeChannel();
            //         // close client connection
            //         Game.obs.closeConnection();

            //         Game.lobby.detach(Game.obs);
            //         Game.session.deleteLobby(Game.lobby.getID());
            //     }
            //     else { // else just leave the lobby
            //         Game.lobby.detach(Game.obs);
            //         // close channel connection
            //         Game.obs.closeConnection();
            //     }
            // } catch (RemoteException e1) {
            //     e1.printStackTrace();
            // }
            // Game.lobby = null;// set lobby to null
            // Game.obs = null;  // set observer to null
            MenuHandler.CloseMenu();
            Game.gui.MenuScreen();
        }

        if (s == this.Refresh) {
            new WaitQueueMenu();
        }

        if (s == this.Start) {
            // try {
            //     // Start the game
            //     Game.lobby.startGame();
            // } catch (RemoteException e1) {
            //     String error = e1.getCause().toString();
            //     Game.error.ShowError(error);
            // }
        }
        
    }
    
}
