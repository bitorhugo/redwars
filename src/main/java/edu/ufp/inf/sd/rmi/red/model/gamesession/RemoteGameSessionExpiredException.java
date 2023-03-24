package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;

public class RemoteGameSessionExpiredException extends RemoteException {

    public RemoteGameSessionExpiredException() {
        super();
    }

    public RemoteGameSessionExpiredException(String s){
        super(s);
    }

    public RemoteGameSessionExpiredException(String s, Throwable t){
        super(s, t);
    }
}
