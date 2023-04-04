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
        // get subject and new state
        System.out.println("State = " + this.subject.getSate());
    }

    
}
