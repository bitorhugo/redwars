package engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Keyboard handling for the game along with the mouse setup for game handling.
 * Menus are being moved to gui.gms
 * @author SergeDavid
 * @version 0.1
 */
@SuppressWarnings("unused")
public class InputHandler implements KeyListener,MouseListener,ActionListener {
	
	//Development buttons and the exit game button (escape key)
	private final int dev1 = KeyEvent.VK_NUMPAD1;
	private final int dev2 = KeyEvent.VK_NUMPAD2;
	private final int dev3 = KeyEvent.VK_NUMPAD3;
	private final int dev4 = KeyEvent.VK_NUMPAD4;
	private final int dev5 = KeyEvent.VK_NUMPAD5;
	private final int dev6 = KeyEvent.VK_NUMPAD6;
	private final int dev7 = KeyEvent.VK_NUMPAD7;
	private final int dev8 = KeyEvent.VK_NUMPAD8;
	private final int dev9 = KeyEvent.VK_NUMPAD9;
	private final int exit = KeyEvent.VK_ESCAPE;
	
	//Movement buttons
	private final int up = KeyEvent.VK_UP;
	private final int down = KeyEvent.VK_DOWN;
	private final int left = KeyEvent.VK_LEFT;
	private final int right = KeyEvent.VK_RIGHT;

	//Command buttons
	private final int select = KeyEvent.VK_Z;
	private final int cancel = KeyEvent.VK_X;
	private final int start = KeyEvent.VK_ENTER;
	
	//Mouse (right/left clicks)
	private final int main = MouseEvent.BUTTON1;
	private final int alt = MouseEvent.BUTTON1;

	public InputHandler() {
		Game.gui.addKeyListener(this);
		Game.gui.addMouseListener(this);
	}

	int DevPathing = 1;

    private void handleOnlineInputRabbit(KeyEvent e) {
        try {
            int key = e.getKeyCode();

            if (key == exit) {System.exit(0);}

            if (Game.GameState == Game.State.PLAYING) {
                players.Base ply = Game.player.get(Game.btl.currentplayer);

                if (key == up) {
                    String message = "up";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == down) {
                    String message = "down";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == left) {
                    String message = "left";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == right) {
                    String message = "right";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == select) {
                    String message = "select";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == cancel) {
                    String message = "cancel";
                    Game.obs.getChannel().basicPublish("", Game.obs.getQeueuName(), null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                } else if (key == start) {
                    new menus.Pause();
                }
            }

            if (Game.GameState == Game.State.EDITOR) {
                if (key == up) {

                } else if (key == down) {

                } else if (key == left) {

                } else if (key == right) {

                } else if (key == select) {

                } else if (key == cancel) {

                } else if (key == start) {
                    new menus.EditorMenu();
                }
            }

            if (key == dev1) {
                Game.gui.MenuScreen();
            }

            if (key == dev2) {
                Game.load.LoadTexturePack("Test");
            }

            if (key == dev3) {
                DevPathing++;
                switch (DevPathing) {
                case 1:
                    Game.pathing.ShowCost = false;
                    break;
                case 2:
                    Game.pathing.ShowHits = true;
                    break;
                case 3:
                    Game.pathing.ShowHits = false;
                    Game.pathing.ShowCost = true;
                    DevPathing = 0;
                    break;
                }
            }

            if (key == dev4) {
                Game.btl.EndTurn();
            }

            if (key == dev5) {
                Game.player.get(Game.btl.currentplayer).npc = !Game.player.get(Game.btl.currentplayer).npc;
                Game.btl.EndTurn();
            }

            if (key == dev6) {
                new menus.StartMenu();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    // private void handleOnlineInput(KeyEvent e) {
    //     try {
    //         int i = e.getKeyCode();
    //         if (i == exit) {
    //             System.exit(0);
    //         }
    //         if (Game.GameState == Game.State.PLAYING) {
    //             players.Base ply = Game.player.get(Game.btl.currentplayer);

    //             if (i == up) {
    //                 Game.obs.getSubject().setSate("up", Game.obs);
    //             } else if (i == down) {
    //                 Game.obs.getSubject().setSate("down", Game.obs);
    //             } else if (i == left) {
    //                 Game.obs.getSubject().setSate("left", Game.obs);
    //             } else if (i == right) {
    //                 Game.obs.getSubject().setSate("right", Game.obs);
    //             } else if (i == select) {
    //                 Game.obs.getSubject().setSate("select", Game.obs);
    //             } else if (i == cancel) {
    //                 Game.obs.getSubject().setSate("cancel", Game.obs);
    //             } else if (i == start) {
    //                 new menus.Pause();
    //             }
    //         }
    //         if (Game.GameState == Game.State.EDITOR) {
    //             if (i == up) {
    //                 Game.obs.getSubject().setSate("up");
    //             } else if (i == down) {
    //                 Game.obs.getSubject().setSate("down");
    //             } else if (i == left) {
    //                 Game.obs.getSubject().setSate("left");
    //             } else if (i == right) {
    //                 Game.obs.getSubject().setSate("right");
    //             } else if (i == select) {
    //                 Game.obs.getSubject().setSate("select");
    //             } else if (i == cancel) {
    //                 Game.obs.getSubject().setSate("cancel");
    //             } else if (i == start) {
    //                 Game.obs.getSubject().setSate("start");
    //             }
    //         }

    //         if (i == dev1) {
    //             Game.gui.MenuScreen();
    //         } else if (i == dev2) {
    //             Game.load.LoadTexturePack("Test");
    //         } else if (i == dev3) {
    //             DevPathing++;
    //             switch (DevPathing) {
    //                 case 1:
    //                     Game.pathing.ShowCost = false;
    //                     break;
    //                 case 2:
    //                     Game.pathing.ShowHits = true;
    //                     break;
    //                 case 3:
    //                     Game.pathing.ShowHits = false;
    //                     Game.pathing.ShowCost = true;
    //                     DevPathing = 0;
    //                     break;
    //             }
    //         } else if (i == dev4) {
    //             Game.btl.EndTurn();
    //         } else if (i == dev5) {
    //             Game.player.get(Game.btl.currentplayer).npc = !Game.player.get(Game.btl.currentplayer).npc;
    //             Game.btl.EndTurn();
    //         } else if (i == dev6) {
    //             new menus.StartMenu();
    //         }

    //     } catch (RemoteException e1) {
    //         System.out.println(e1.getMessage());
    //     }
    // }

    private void handleOfflineInput(KeyEvent e) {
        int i=e.getKeyCode();
		if (i==exit) {System.exit(0);}
        if (Game.GameState==Game.State.PLAYING) {
			players.Base ply = Game.player.get(Game.btl.currentplayer);
			if (i==up) {
                
                ply.selecty--;if (ply.selecty<0) {ply.selecty++;}
            }
			else if (i==down) {ply.selecty++;if (ply.selecty>=Game.map.height) {ply.selecty--;}}
			else if (i==left) {ply.selectx--;if (ply.selectx<0) {ply.selectx++;}}
			else if (i==right) {ply.selectx++;if (ply.selectx>=Game.map.width) {ply.selectx--;}}
			else if (i==select) {Game.btl.Action();}
			else if (i==cancel) {Game.player.get(Game.btl.currentplayer).Cancle();}
			else if (i==start) {new menus.Pause();}
		}
		if (Game.GameState==Game.State.EDITOR) {
			if (i==up) {Game.edit.selecty--;if (Game.edit.selecty<0) {Game.edit.selecty++;} Game.edit.moved = true;}
			else if (i==down) {Game.edit.selecty++;if (Game.edit.selecty>=Game.map.height) {Game.edit.selecty--;} Game.edit.moved = true;}
			else if (i==left) {Game.edit.selectx--;if (Game.edit.selectx<0) {Game.edit.selectx++;} Game.edit.moved = true;}
			else if (i==right) {Game.edit.selectx++;if (Game.edit.selectx>=Game.map.width) {Game.edit.selectx--;} Game.edit.moved = true;}
			else if (i==select) {Game.edit.holding = true;}
			else if (i==cancel) {Game.edit.ButtButton();}
			else if (i==start) {
				new menus.EditorMenu();
			}
		}
		if (i==dev1) {Game.gui.MenuScreen();}
		else if (i==dev2) {Game.load.LoadTexturePack("Test");}
		else if (i==dev3) {
			DevPathing++;
			switch (DevPathing) {
            case 1:Game.pathing.ShowCost=false;break;
            case 2:Game.pathing.ShowHits=true;break;
            case 3:Game.pathing.ShowHits=false;Game.pathing.ShowCost=true;DevPathing=0;break;
			}
		}
		else if (i==dev4) {Game.btl.EndTurn();}
		else if (i==dev5) {Game.player.get(Game.btl.currentplayer).npc = !Game.player.get(Game.btl.currentplayer).npc; Game.btl.EndTurn();}
		else if (i==dev6) {new menus.StartMenu();}
    }
    
	public void keyPressed(KeyEvent e) {
        if (Game.isOnline) {
            handleOnlineInputRabbit(e);
        }
        else {
            handleOfflineInput(e);
        }
	}

    public void keyReleased(KeyEvent e) {
		int i=e.getKeyCode();
		if (Game.GameState==Game.State.EDITOR) {
			if (i==select) {Game.edit.holding = false;}
		}
	}

    public void keyTyped(KeyEvent arg0) {}
	public void mousePressed() {}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		Game.gui.requestFocusInWindow();
		Object s = e.getSource();
	}
}
