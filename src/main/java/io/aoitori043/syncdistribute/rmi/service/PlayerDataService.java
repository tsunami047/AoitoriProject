package io.aoitori043.syncdistribute.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface PlayerDataService extends Remote {

    Map<String,String> getCurrentData(String playerName) throws RemoteException;

    void set(String playerName,String varName,String result) throws RemoteException;
}
