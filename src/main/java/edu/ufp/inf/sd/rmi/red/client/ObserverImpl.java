package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.server.lobby.SubjectRI;
import engine.Game;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private SubjectRI subject;
    private Game game;
    
    public ObserverImpl(SubjectRI subject) throws RemoteException {
        super();
        this.subject = subject;
    }

    public ObserverImpl(SubjectRI subject, Game game) throws RemoteException {
        super();
        this.subject = subject;
        this.game = game;
    }

    public SubjectRI getSubject() {
        return this.subject;
    }

    public String getLastObserverState() throws RemoteException {
        return this.subject.getSate();
    }

    @Override
    public void startGame() throws RemoteException {
        System.out.println(game);
        game.startGame();
    }

    @Override
    public void update() throws RemoteException {
        // get subject and new state
        System.out.println("State = " + this.subject.getSate());
    }

    
}
