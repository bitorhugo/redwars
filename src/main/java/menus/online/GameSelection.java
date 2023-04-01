package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
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

import edu.ufp.inf.sd.rmi.red.client.ObserverImpl;
import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;
import menus.MenuHandler;


public class GameSelection implements ActionListener {

    public JButton Enter = new JButton("Enter");
    public JButton Return = new JButton("Return");
    public JButton Refresh = new JButton("Refresh");
    public JLabel PanelInfo = new JLabel("Game Selection");
    public JList<String> availableGamesList;
    private JScrollPane lobbies;
    private String mapname;
    private Map<String, SubjectRI> lobbyNames = new HashMap<>();

    private int[] plys;
    boolean[] npc;
    int startMoney;
    int cityMoney;

    public GameSelection(String mapname) {
        this.mapname = mapname;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
        this.gameList(size);
    }

    public GameSelection(String mapname, int[] plys, boolean[]npc, int startMoney, int cityMoney) {
        this.plys = plys;
        this.npc = npc;
        this.startMoney = startMoney;
        this.cityMoney = cityMoney;
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
        lobbies = new JScrollPane(this.availableGamesList = new JList<>(this.availableGames(this.mapname)));
        lobbies.setBounds(size.x+220, size.y, 140, 260);
        Game.gui.add(lobbies);
		this.availableGamesList.setBounds(0, 0, 140, 260);
		this.availableGamesList.setSelectedIndex(0);
	}

    private DefaultListModel<String> availableGames(String mapname) {
        DefaultListModel<String> lobbiesList = new DefaultListModel<>();
        try {
            Game.session.lobbies(mapname).forEach(lobby -> {
                    try {
                        int playerCount = lobby.players().size();
                        String displayMsg = this.displayMsg(mapname, playerCount);
                    this.lobbyNames.put(displayMsg, lobby);
                    lobbiesList.addElement(displayMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
        } catch (RemoteException e) {
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
            Game.gui.LoginScreen();
        }

        if (s == this.Enter) {
            try {
                // get value from scroll pane
                String selected = this.availableGamesList.getSelectedValue();
                UUID l = this.lobbyNames.get(selected).getID();
                Game.lobby = Game.session.enterLobby(l);
                new WaitQueueMenu(mapname,
                                  plys,
                                  npc,
                                  startMoney,
                                  cityMoney);
                // Game.obs = new ObserverImpl(Game.lobby, Game.g);
                // Game.lobby.attach(false, Game.obs);

            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }

        if (s == this.Refresh) {
            new GameSelection(this.mapname,
                              this.plys,
                              this.npc,
                              this.startMoney,
                              this.cityMoney);
        }
    }
    
}
