package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;
import menus.MenuHandler;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private String username;
    private int commander;
    private SubjectRI subject;
    private Game game;
    
    public ObserverImpl(SubjectRI subject) throws RemoteException {
        super();
        this.subject = subject;
    }

    public ObserverImpl(String username, int commander, SubjectRI subject, Game game) throws RemoteException {
        super();
        this.username = username;
        this.commander = commander;
        this.subject = subject;
        this.game = game;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public int getCommander() throws RemoteException {
        return this.commander;
    }    

    public SubjectRI getSubject() {
        return this.subject;
    }

    public String getLastObserverState() throws RemoteException {
        return this.subject.getSate();
    }

    @Override
    public void startGame() throws RemoteException {
        game.startGame();
        game.isOnline = true;
    }

    @Override
    public void update() throws RemoteException {
        String state = this.getLastObserverState();
        if (Game.GameState == Game.State.PLAYING) {
            var ply = Game.player.get(Game.btl.currentplayer);
            switch (state) {
            case "up":
                ply.selecty--;
                if (ply.selecty < 0) {
                    ply.selecty++;
                }
                break;
            case "down":
                ply.selecty++;
                if (ply.selecty >= Game.map.height) {
                    ply.selecty--;
                }
                break;
            case "left":
                ply.selectx--;
                if (ply.selectx < 0) {
                    ply.selectx++;
                }
                break;
            case "right":
                ply.selectx++;
                if (ply.selectx >= Game.map.width) {
                    ply.selectx--;
                }
                break;
            case "select":
                Game.btl.Action();
                break;
            case "cancel":
                Game.player.get(Game.btl.currentplayer).Cancle();
                break;
            case "start":
                new menus.Pause();
                break;
            case "endturn":
                MenuHandler.CloseMenu();
                Game.btl.EndTurn();
                break;
            default:
                // TODO: Find a way to handle buy state
                String[] params = state.split(":");
                Game.btl.Buyunit(Integer.parseInt(params[1]),
                                 Integer.parseInt(params[2]),
                                 Integer.parseInt(params[3]));
                MenuHandler.CloseMenu();
            }
        }
    }


}
