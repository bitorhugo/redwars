package menus.online;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.awt.Point;

import javax.swing.JButton;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
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
                Game.chan.queueDeclare(Game.u, false, false, false, null);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String[] response = new String(delivery.getBody(), "UTF-8").split(";");
                    System.out.println(" [x] Received " + Arrays.asList(response));
                    switch (response[0]) {
                    case "ok":
                            Game.lobbyID = response[1];
                            Game.chan.queueDelete(Game.u);
                            new WaitQueueMenu();
                        break;
                    default:
                    }
                };
                Game.chan.basicConsume(Game.u, true, deliverCallback, consumerTag -> { });

                // fanout message of new looby
                Game.chan.exchangeDeclare(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "fanout");
                String msg = "new" + ";" + Game.u + ";" + this.mapname;
                Game.chan.basicPublish(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "", null, msg.getBytes("UTF-8"));
                System.out.println("INFO: Success! Message " + msg + " sent to Exchange LOBBIES.");

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
