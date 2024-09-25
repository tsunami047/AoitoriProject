package io.aoitori043.syncdistribute.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OnlineService extends Remote {

    Boolean isOnline(String playerName) throws RemoteException;
}
