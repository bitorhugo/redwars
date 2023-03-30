package menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
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

import engine.Game;


public class GameSelection implements ActionListener {

    public JButton Attach = new JButton("Attach");
    public JButton Return = new JButton("Return");
    public JButton Refresh = new JButton("Refresh");
    public JLabel PanelInfo = new JLabel("Game Selection");
    public JList<UUID> availableGamesList;
    private JScrollPane availableGames;

    public GameSelection() {
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
        this.Refresh.setBounds(size.x, size.y = 38 * 2, 100, 32);
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
        Game.gui.add(this.Attach);
        Game.gui.add(this.Return);
        Game.gui.add(this.Refresh);
        Game.gui.add(this.PanelInfo);
    }

    private void addActionListeners() {
        this.Attach.addActionListener(this);
        this.Return.addActionListener(this);
        this.Refresh.addActionListener(this);
    }

    private void gameList(Point size) {
        availableGames = new JScrollPane(this.availableGamesList = new JList<>(this.availableGames()));
        availableGames.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(availableGames);
		this.availableGamesList.setBounds(0, 0, 140, 260);
		this.availableGamesList.setSelectedIndex(0);
	}

    private DefaultListModel<UUID> availableGames() {
        DefaultListModel<UUID> gamesList = new DefaultListModel<>();
        try {
            Game.session.lobbies().forEach(gameID -> {
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

        if (s == this.Refresh) {
            new GameSelection();
        }
    }
    
}
