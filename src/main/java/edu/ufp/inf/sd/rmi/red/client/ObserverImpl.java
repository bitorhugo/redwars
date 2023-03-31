package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.model.lobby.SubjectRI;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private SubjectRI subject;
    
    public ObserverImpl(SubjectRI subject) throws RemoteException {
        super();
        this.subject = subject;
    }

    public SubjectRI getSubject() {
        return this.subject;
    }

    public String getLastObserverState() throws RemoteException {
        return this.subject.getSate();
    }

    @Override
    public void update() throws RemoteException {
        // get subject and new state
        System.out.println("State = " + this.subject.getSate());
    }

    
}
