package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private String username;
    private int commander;
    private SubjectRI subject;
    private Game game;
    
    public ObserverImpl(SubjectRI subject) throws RemoteException {
        super();
        this.subject = subject;
    }

    public ObserverImpl(String username, SubjectRI subject, Game game) throws RemoteException {
        super();
        this.username = username;
        this.subject = subject;
        this.game = game;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
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
    }

    @Override
    public void update() throws RemoteException {
        if (Game.GameState == Game.State.PLAYING) {
            var ply = Game.player.get(Game.btl.currentplayer);
            String state = this.getLastObserverState();
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
            }
        }
        if (Game.GameState == Game.State.EDITOR) {
            var ply = Game.player.get(Game.btl.currentplayer);
            String state = this.getLastObserverState();
            switch (state) {
            case "up":
                Game.edit.selecty--;if (Game.edit.selecty<0) {Game.edit.selecty++;} Game.edit.moved = true;
                break;
            case "down":
                Game.edit.selecty++;if (Game.edit.selecty>=Game.map.height) {Game.edit.selecty--;} Game.edit.moved = true;
                break;
            case "left":
                Game.edit.selectx--;if (Game.edit.selectx<0) {Game.edit.selectx++;} Game.edit.moved = true;
                break;
            case "right":
                Game.edit.selectx++;if (Game.edit.selectx>=Game.map.width) {Game.edit.selectx--;} Game.edit.moved = true;
                break;
            case "select":
                Game.edit.holding = true;
                break;
            case "cancel":
                Game.edit.ButtButton();
                break;
            case "start":
                new menus.EditorMenu();
                break;
            }
        }
    }


}
