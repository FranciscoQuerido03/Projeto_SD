package com.example.demo;

import java.rmi.Remote;

public interface WebServer_I extends Remote{

    public void update() throws java.rmi.RemoteException;
} 
