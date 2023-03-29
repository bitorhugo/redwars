package menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;


import engine.Game;


public class WaitQeueuMenu implements ActionListener {

    private int gameID;
    
    public JButton Start = new JButton("Start");
    public JButton Return = new JButton("Return");
    public JList<Integer> playersInQeueu;

    public WaitQeueuMenu(int gameID) {
        this.gameID = gameID;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.PlayerList(size);
    }

    private void SetBounds(Point size) {
		this.Start.setBounds(size.x, size.y+10, 100, 32);
		this.Return.setBounds(size.x,size.y+10+38*1, 100, 32);
	}

    private void addGui() {
        Game.gui.add(Start);
        Game.gui.add(Return);
    }

    private void addActionListeners() {
        this.Start.addActionListener(this);
        this.Return.addActionListener(this);
    }

    private void PlayerList(Point size) {
        JScrollPane availableGames = new JScrollPane(this.playersInQeueu=new JList<>(this.players()));
        availableGames.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(availableGames);
		this.playersInQeueu.setBounds(0, 0, 140, 260);
		this.playersInQeueu.setSelectedIndex(0);
	}

    private DefaultListModel<Integer> players() {
        DefaultListModel<Integer> gamesList = new DefaultListModel<>();
        return gamesList;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) { // cancel game creation
            try {
                Game.session.cancelGame(this.gameID);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            MenuHandler.CloseMenu();
            Game.gui.LoginScreen();
        }

        if (s == this.Start) {
            // MenuHandler.CloseMenu();
			// Game.btl.NewGame(mapname);
			// Game.btl.AddCommanders(plyer, npc, 100, 50);
			// Game.gui.InGameScreen();
        }
        
    }
    
}
