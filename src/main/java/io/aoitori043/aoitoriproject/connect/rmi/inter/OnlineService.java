package io.aoitori043.aoitoriproject.connect.rmi.inter;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OnlineService extends Remote {

    Boolean isOnline(String playerName) throws RemoteException;
}
