package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Connection;

import edu.ufp.inf.sd.rmi.red.model.db.DBI;
import edu.ufp.inf.sd.rmi.red.server.gamesession.GameSession;
import edu.ufp.inf.sd.rmi.red.server.gamesession.GameSessionRI;
import edu.ufp.inf.sd.rmi.red.server.gamesession.RemoteGameSessionExpiredException;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;
import edu.ufp.inf.sd.rmi.red.model.token.Token;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DBI db;
    private Connection conn;
    private Map<UUID, Lobby> lobbies;


    public GameFactoryImpl() throws RemoteException {
        super();
    }

    public GameFactoryImpl(DBI db) throws RemoteException {
        this();
        this.db = db;
    }

    public GameFactoryImpl(DBI db, Connection conn) throws RemoteException {
        this(db);
        this.conn = conn;
    }

    public GameFactoryImpl(DBI db, Map<UUID, Lobby> lobbies, Connection conn) throws RemoteException {
        this(db, conn);
        this.lobbies = lobbies;
    }

    public Map<UUID, Lobby> getLobbies() {
        return this.lobbies;
    }

    public void setLobbies(Map<UUID, Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    @Override
    public GameSessionRI login(String username, String secret) throws RemoteException {
        User u = this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
        GameSession session =
            new GameSession(this.conn, u, this.lobbies);
        return session;
    }

    @Override
    public GameSessionRI register(String username, String secret) throws RemoteException {
        User u = this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
        GameSession session =
            new GameSession(this.conn, u, this.lobbies);
        return session;
    }

    @Override
    public void logout(String username) throws RemoteException {
        User u = this.db.select(username).orElseThrow(RemoteUserNotFoundException::new);
        Token t = u.getToken().orElseThrow(RemoteGameSessionExpiredException::new);
    }

}

