package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.awt.Point;

import javax.swing.JButton;

import com.rabbitmq.client.DeliverCallback;


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
                // open queue for receiving response from server
                Map<String, Object> args = new HashMap<>();
                args.put("x-expires", 60000); // queue time-to-live = 60s
                Game.chan.queueDeclare(Game.u, false, false, false, args);
            
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String[] response = new String(delivery.getBody(), "UTF-8").split(";");
                    System.out.println(" [x] Received " + Arrays.asList(response));
                    switch (response[0]) {
                    case "ok":
                            Game.lobbyID = response[1];
                            // since user won't use this queue anymore, delete it
                            Game.chan.queueDelete(Game.u);
                            new WaitQueueMenu();
                        break;
                    default:
                    }
                };
                Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });

                String msg = "new" + ";" + Game.u + ";" + this.mapname;
                // fanout message of new looby
                Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));
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
