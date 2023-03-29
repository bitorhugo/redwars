package menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JList;

import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSessionRI;
import engine.Game;


public class GameSelection implements ActionListener {

    private GameSessionRI session;
    public JButton Attach = new JButton("Attach");
    public JButton Return = new JButton("Return");
    public JList<Integer> availableGamesList;

    public GameSelection(GameSessionRI session) {
        this.session = session;
        Point size = MenuHandler.PrepMenu(400, 200);
        MenuHandler.HideBackground();
        this.SetBounds(size);
        this.addGui();
        this.addActionListeners();
    }

    private void SetBounds(Point size) {
		this.Attach.setBounds(size.x+200, size.y+170, 100, 24);
		this.Return.setBounds(size.x+20, size.y+170, 100, 24);
	}

    private void addGui() {
        Game.gui.add(Attach);
        Game.gui.add(Return);
    }

    private void addActionListeners() {
        this.Attach.addActionListener(this);
        this.Return.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.Return) {
            MenuHandler.CloseMenu();
            Game.gui.LoginScreen();
        }
        
    }
    
}
