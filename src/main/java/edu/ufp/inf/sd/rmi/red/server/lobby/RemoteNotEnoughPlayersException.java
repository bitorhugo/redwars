package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.rmi.RemoteException;

public class RemoteNotEnoughPlayersException extends RemoteException {
    public RemoteNotEnoughPlayersException () {
        super();
    }
    public RemoteNotEnoughPlayersException(String s){

        super(s);
    }

    public RemoteNotEnoughPlayersException(String s, Throwable t){
        super(s, t);
    }
}
