package edu.ufp.inf.sd.rmi.red.server.tokenring;

import java.rmi.RemoteException;

public class RemoteNotCurrentTokenHolderException extends RemoteException {
    public RemoteNotCurrentTokenHolderException () {
        super();
    }
    public RemoteNotCurrentTokenHolderException(String s){

        super(s);
    }

    public RemoteNotCurrentTokenHolderException(String s, Throwable t){
        super(s, t);
    }
}
