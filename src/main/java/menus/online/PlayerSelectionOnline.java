package menus.online;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import engine.Game;
import menus.MenuHandler;


/**
 * This deals with player and battle options setup (might split it) such as npc, team, commander, starting money, turn money, fog, etc.
 * @author SergeDavid
 * @version 0.2
 */
public class PlayerSelectionOnline implements ActionListener {
	//TODO: Scale with map size.
	//Commander Selection
	JButton Prev = new JButton("Prev");
	JButton Next = new JButton("Next");
	JLabel Name = new JLabel("Andy");
	int[] plyer = {0,0,0,0};
	
	//NPC Stuff
	JButton ManOrMachine = new JButton("PLY");
	boolean[] npc = {false};
	
	//Other
	JButton Return = new JButton("Return");
	JButton StartMoney = new JButton ("$ 100");int start = 100;
	JButton CityMoney = new JButton ("$ 50");int city = 50;
	JButton ThunderbirdsAreGo = new JButton ("Start");
	
	String mapname;

	public PlayerSelectionOnline(String map) {
		mapname = map;
		Point size = MenuHandler.PrepMenu(400,200);
		SetBounds(size);
		AddListeners();
        AddGui();
	}
    
	private void SetBounds(Point size) {
        Next.setBounds(size.x+84*2, size.y+100, 64, 32);
        Name.setBounds(size.x+10+84*2, size.y+40, 64, 32);
        ManOrMachine.setBounds(size.x+2+84*2, size.y+68, 58, 24);
        Prev.setBounds(size.x+84*2, size.y+10, 64, 32);
        
		ThunderbirdsAreGo.setBounds(size.x+280, size.y+170, 100, 24);
		Return.setBounds(size.x+20, size.y+170, 100, 24);
	}
    
	private void AddGui() {
        Game.gui.add(Prev);
        Game.gui.add(Next);
        Game.gui.add(ManOrMachine);
        Game.gui.add(Name);
		Game.gui.add(ThunderbirdsAreGo);
		Game.gui.add(Return);
	}
    
	private void AddListeners() {
        Next.addActionListener(this);
        ManOrMachine.addActionListener(this);
        Prev.addActionListener(this);
        
		ThunderbirdsAreGo.addActionListener(this);
		Return.addActionListener(this);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
		if (s == Return) {
			MenuHandler.CloseMenu();
			Game.gui.LoginScreen();
		}
        
		if(s == ThunderbirdsAreGo) {
            // here is where the new game is started
            // maybe create a queue of minimum amount of players can attach

            // try {
            //     Game.lobby = Game.session.createLobby(mapname);
            //     System.out.println(Game.lobby);
            //     new WaitQueueMenu();
            // } catch (IOException e1) {
            //     e1.printStackTrace();
            // }

			// MenuHandler.CloseMenu();
			// Game.btl.NewGame(mapname);
			// Game.btl.AddCommanders(plyer, npc, 100, 50);
			// Game.gui.InGameScreen();
		}


		for (int i = 0; i < 4; i++) {
			if (s == Prev) {
				plyer[i]--;
				if (plyer[i]<0) {plyer[i]=Game.displayC.size()-1;}
				Name.setText(Game.displayC.get(plyer[i]).name);
			}
			else if (s == Next) {
				plyer[i]++;
				if (plyer[i]>Game.displayC.size()-1) {plyer[i]=0;}
				Name.setText(Game.displayC.get(plyer[i]).name);
			}
			else if (s == ManOrMachine) {
				npc[i]=!npc[i];
				if (npc[i]) {ManOrMachine.setText("NPC");}
				else {ManOrMachine.setText("PLY");}
			}
		}
	}
}
