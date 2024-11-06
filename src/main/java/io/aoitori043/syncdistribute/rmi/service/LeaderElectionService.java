package io.aoitori043.syncdistribute.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-05  23:53
 * @Description: ?
 */
public interface LeaderElectionService extends Remote {

    void participateInElection(int port) throws RemoteException;
}
