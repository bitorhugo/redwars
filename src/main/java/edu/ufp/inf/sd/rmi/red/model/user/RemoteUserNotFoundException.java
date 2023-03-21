package edu.ufp.inf.sd.rmi.red.model.user;

import java.rmi.RemoteException;

public class RemoteUserNotFoundException extends RemoteException {

    public RemoteUserNotFoundException(){
        super();
    }

    public RemoteUserNotFoundException(String s){
        super(s);
    }

    public RemoteUserNotFoundException(String s, Throwable t){
        super(s, t);
    }
}
