package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.awt.Point;

import javax.swing.JButton;

import edu.ufp.inf.sd.rmi.red.client.ObserverImpl;
import edu.ufp.inf.sd.rmi.red.client.exchange.ExchangeEnum;
import engine.Game;
import menus.MenuHandler;

public class OnlineMenu implements ActionListener {

	public JButton New = new JButton("New Lobby");
	public JButton Search = new JButton("Search");
    public JButton Return = new JButton("Return");

    private String mapname;

    public OnlineMenu(String mapname) {
        this.mapname = mapname;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addActionListeners();
        this.addGui();
    }

    private void SetBounds(Point size) {
        this.New.setBounds(size.x, size.y + 10, 100, 32);
		this.Search.setBounds(size.x,size.y+10+38*1, 100, 32);
        this.Return.setBounds(size.x, size.y+10+38*2, 100, 32);
	}

    private void addGui() {
        Game.gui.add(this.New);
        Game.gui.add(this.Search);
        Game.gui.add(this.Return);
    }

    private void addActionListeners() {
        this.New.addActionListener(this);
        this.Search.addActionListener(this);
        this.Return.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == New) {
            try {
                String msg = this.mapname + ";" + "new";

                // fanout message of new looby
                Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), mapname, null, msg.getBytes("UTF-8"));
                System.out.println("INFO: Success! Message " + msg + " sent to Exchange LOBBIES.");
                
                //     Game.lobby = Game.session.createLobby(mapname);
                //     System.out.println("INFO: New lobby created: " + Game.lobby);
                //     int id = Game.lobby.players().size();
                //     Game.obs = new ObserverImpl(id, Game.u, Game.cmd, Game.lobby, Game.g);
                //     Game.lobby.attach(Game.obs);                // attach observer to lobby
                //     new WaitQueueMenu();
                // } catch (RemoteException e1) {
                //     e1.printStackTrace();
                // }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (s == Search) {
            new GameSelection(mapname);
        }

        if (s == Return) {
            MenuHandler.CloseMenu();
            Game.gui.MenuScreen();
        }
    }
    
}
