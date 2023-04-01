package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import edu.ufp.inf.sd.rmi.red.client.ObserverImpl;
import engine.Game;
import menus.MenuHandler;


public class WaitQueueMenu implements ActionListener {

    public JButton Start = new JButton("Start");
    public JButton Return = new JButton("Return");
    public JButton Refresh = new JButton("Refresh");
    public JLabel PanelInfo = new JLabel("WaitQueue");
    public JList<String> playersInQeueu;

    public WaitQueueMenu() {
        System.out.println(Game.lobby);
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.playerList(size);
    }
    public WaitQueueMenu(String mapname,
                         int[]ply,
                         boolean[]npc,
                         int startMoney,
                         int cityMoney) {
        System.out.println(Game.lobby);
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
        JScrollPane players = new JScrollPane(this.playersInQeueu = new JList<>(players()));
        players.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(players);
		this.playersInQeueu.setBounds(0, 0, 140, 260);
		this.playersInQeueu.setSelectedIndex(0);
	}

    private DefaultListModel<String> players() {
        DefaultListModel<String> players = new DefaultListModel<>();
        try {
            Game.lobby.players().forEach(player -> {
                    players.addElement(player);
                });;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return players;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) { // exit game lobby
            try {
                // if lobby has only one player, cancel lobby
                if (Game.lobby.players().size() == 1) {
                    Game.session.cancelLobby(Game.lobby.getID());
                }
                else {
                    Game.session.exitLobby(Game.lobby.getID());
                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            Game.lobby = null;
            MenuHandler.CloseMenu();
            Game.gui.LoginScreen();
        }

        if (s == this.Refresh) {
            new WaitQueueMenu();
        }

        if (s == this.Start) {
            try { 
                // set up observer
                Game.obs = new ObserverImpl(Game.lobby);
                Game.lobby.attach(Game.obs);
                Game.obs.getSubject().setSate("Setting state from gui");
                
                // MenuHandler.CloseMenu();
                // Game.btl.NewGame(Game.lobby.getMapname());
                // boolean []npc = {false, false, false, false};
                // int []cmds = {0, 0, 0, 0};
                // Game.btl.AddCommanders(cmds, npc, 100, 50);
                // Game.gui.InGameScreen();
                
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
        
    }
    
}
