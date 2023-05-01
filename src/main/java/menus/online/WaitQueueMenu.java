package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
        // try {
        //     // open queue for receiving response from server
        //     Game.chan.queueDeclare(Game.u, false, false, false, null);
        //     DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        //         String[] response = new String(delivery.getBody(), "UTF-8").split(";");
        //         String status = response[0];
        //         switch (status) {
        //         case "ok":
        //             for (int i = 1; i < response.length; ++i) {
        //                 pls.addElement(response[i]);
        //             }
        //             Game.chan.queueDelete(Game.u); // maybe do not delete queue yet, since we may need it to initialize the game
        //             break;
        //         }
        //     };
        //     Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });
            
        //     // query the server for lobbies
        //     Game.chan.queueDeclare("search_lobby", false, false, false, null);
        //     String msg = "getPlayers" + ";" + Game.lobbyID + ";" + Game.u;
        //     Game.chan.basicPublish("", "search_lobby", null, msg.getBytes("UTF-8"));

        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
        return pls;
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            // try {
            //     Game.chan.queueDeclare(Game.u, false, false, false, null);
            //     DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //         String[] response = new String(delivery.getBody(), "UTF-8").split(";");
            //         String status = response[0];
            //         switch (status) {
            //         case "ok":
            //             System.out.println("INFO: Message " + status + " received, leaving lobby");
            //             Game.lobbyID = null;
            //             Game.chan.queueDelete(Game.u);
            //             MenuHandler.CloseMenu();
            //             Game.gui.MenuScreen();
            //             break;
            //         }
            //     };
            //     Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });

            //     Game.chan.exchangeDeclare(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "fanout");
            //     String msg = "delete" + ";" + Game.lobbyID + ";" + Game.u;
            //     Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));
            // } catch (IOException e1) {
            //     e1.printStackTrace();
            // }
        MenuHandler.CloseMenu();
        Game.gui.MenuScreen();
        }

        if (s == this.Refresh) {
            new WaitQueueMenu();
        }

        if (s == this.Start) {
            try {

                // start to listen for messages coming from lobby exchange
                
                Game.chan.queueDeclare("search_lobby", false, false, false, null);
                String msg = "startGame" + ";" + Game.lobbyID + ";" + Game.u;
                Game.chan.basicPublish("", "search_lobby", null, msg.getBytes("UTF-8"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            // try {
                
            // } catch (IOException e1){
            
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
