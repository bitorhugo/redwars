package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.ufp.inf.sd.rmi.red.model.db.DBI;
import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSession;
import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSessionRI;
import edu.ufp.inf.sd.rmi.red.model.gamesession.RemoteGameSessionExpiredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DBI db;
    private List<GameSessionRI> observers = Collections.synchronizedList(new ArrayList<>());

    public GameFactoryImpl() throws RemoteException {
        super();
    }

    public GameFactoryImpl(DBI db) throws RemoteException {
        super();
        this.db = db;
    }

    @Override
    public GameSessionRI login(String username, String secret) throws RemoteException {
        User u = this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
        GameSessionRI session = new GameSession(u.getToken().orElseThrow(RemoteGameSessionExpiredException::new));
        this.observers.add(session);
        return session;
    }

    @Override
    public GameSessionRI register(String username, String secret) throws RemoteException {
        User u = this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
        GameSessionRI session = new GameSession(u.getToken().orElseThrow(RemoteGameSessionExpiredException::new));
        this.observers.add(session);
        return session;
    }

    
}

