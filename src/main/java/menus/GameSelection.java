package menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSessionRI;
import engine.Game;


public class GameSelection implements ActionListener {

    private GameSessionRI session;
    public JButton Attach = new JButton("Attach");
    public JButton Return = new JButton("Return");
    public JList<Integer> availableGamesList;

    public GameSelection() {
        //        this.session = session;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.gameList(size);
    }

    private void SetBounds(Point size) {
		this.Attach.setBounds(size.x, size.y+10, 100, 32);
		this.Return.setBounds(size.x,size.y+10+38*1, 100, 32);
	}

    private void addGui() {
        Game.gui.add(Attach);
        Game.gui.add(Return);
    }

    private void addActionListeners() {
        this.Attach.addActionListener(this);
        this.Return.addActionListener(this);
    }

    private void gameList(Point size) {
        JScrollPane availableGames = new JScrollPane(this.availableGamesList=new JList<>(this.availableGames()));
        availableGames.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(availableGames);
		this.availableGamesList.setBounds(0, 0, 140, 260);
		this.availableGamesList.setSelectedIndex(0);
	}

    private DefaultListModel<Integer> availableGames() {
        DefaultListModel<Integer> gamesList = new DefaultListModel<>();
        try {
            Game.session.availableGames().forEach(gameID -> {
                    gamesList.addElement(gameID);
                });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return gamesList;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            MenuHandler.CloseMenu();
            Game.gui.LoginScreen();
        }

        if (s == this.Attach) {
            try {
                Game.session.attach();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
        
    }
    
}
