package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Channel;

import edu.ufp.inf.sd.rmi.red.model.db.DB;
import edu.ufp.inf.sd.rmi.red.server.gamesession.GameSession;
import edu.ufp.inf.sd.rmi.red.server.gamesession.GameSessionRI;
import edu.ufp.inf.sd.rmi.red.server.gamesession.RemoteGameSessionExpiredException;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.model.token.Token;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DB db;
    private Channel chan;
    private Map<UUID, Lobby> lobbies = Collections.synchronizedMap(new HashMap<>());


    public GameFactoryImpl() throws RemoteException {
        super();
    }

    public GameFactoryImpl(DB db) throws RemoteException {
        this();
        this.db = db;
    }

    public GameFactoryImpl(Channel chan, DB db) throws RemoteException {
        this(db);
        this.chan = chan;
    }

    @Override
    public GameSessionRI login(String username, String secret) throws RemoteException {
        User u = this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
        GameSession session =
            new GameSession(this.chan, u, this.lobbies);
        return session;
    }

    @Override
    public GameSessionRI register(String username, String secret) throws RemoteException {
        User u = this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
        GameSession session =
            new GameSession(this.chan, u, this.lobbies);
        return session;
    }

    @Override
    public void logout(String username) throws RemoteException {
        User u = this.db.select(username).orElseThrow(RemoteUserNotFoundException::new);
        Token t = u.getToken().orElseThrow(RemoteGameSessionExpiredException::new);
    }

}

