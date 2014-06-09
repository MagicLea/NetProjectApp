package com.netproject;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Server {
    @SuppressWarnings("unchecked")
    private static ArrayList<ObjectOutputStream>[] rooms = new ArrayList[10000];

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (listening) {
                new ServerThread(server, serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    public void enterRoom(ObjectOutputStream toClient, int number) {
        if (rooms[number] == null) {
            rooms[number] = new ArrayList<ObjectOutputStream>();
        }
        rooms[number].add(toClient);
    }
    
    public void leaveRoom(ObjectOutputStream toClient, int number){
        rooms[number].remove(toClient);
    }
    
    public void broadcastToRoom(int number, MyProtocol.Unit unit, ObjectOutputStream fromWho){
        for(ObjectOutputStream toClient : rooms[number]){
            if(!toClient.equals(fromWho)){ //若不是發出傳訊的人，則向他發出廣播。
                try {
                    toClient.writeObject(unit);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
