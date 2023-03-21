package edu.ufp.inf.sd.rmi.red.model.user;

import java.rmi.RemoteException;

public class RemoteUserAlreadyRegisteredException extends RemoteException {

    public RemoteUserAlreadyRegisteredException(){
        super();
    }

    public RemoteUserAlreadyRegisteredException(String s){
        super(s);
    }

    public RemoteUserAlreadyRegisteredException(String s, Throwable t){
        super(s, t);
    }
}
