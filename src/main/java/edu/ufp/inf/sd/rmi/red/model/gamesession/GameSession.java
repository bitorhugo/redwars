package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.ufp.inf.sd.rmi.red.model.token.Token;

public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private Token token;
    // availableGames will contain all games that were created
    // the integer will represent the game ID
    // the String will be the absolute path on the server where the diretory of savegame.properties file of that specific game is stored
    // the save folder will be called 'saves' and each sub-directory name will be the ID of the game
    // e.g.
    // saves/
    //      0/savegame.properties
    //      1/savageame/properties
    Map<Integer, String> availableGames = new ConcurrentHashMap<>();
    
    public GameSession(Token token) throws RemoteException {
        super();
        System.out.println(token);
        this.token = token;
    }
    
    @Override
    public void test(String s) throws RemoteException {
        System.out.println(s);
    }
        
}