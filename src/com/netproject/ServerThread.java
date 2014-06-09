package com.netproject;
import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    private Server server = null;
    private Socket socket = null;
    private ObjectInputStream fromClient;
    private ObjectOutputStream toClient;
    private Integer roomNumber;

    public ServerThread(Server server ,Socket socket) {
        super("SeverThread");
        this.server = server;
        this.socket = socket;
    }

    public void run() {
        try {
            fromClient = new ObjectInputStream(socket.getInputStream());
            toClient = new ObjectOutputStream(socket.getOutputStream());
            //是否有東西可以接收
            while(true){
                    MyProtocol.Unit unit = (MyProtocol.Unit)fromClient.readObject();
                    //處理接收的訊息
                    if(unit.type == MyProtocol.ENTER_ROOM){ //進入房間訊息
                        roomNumber = (Integer)unit.contents[0];
                        server.enterRoom(toClient,roomNumber);
                        System.out.println(socket + " add to Room " + roomNumber);
                    }
                    else if(unit.type == MyProtocol.COMMUNICATION_TEXT){ //文字聊天訊息
                        String str = (String)unit.contents[0];
                        System.out.println(str);
                        server.broadcastToRoom(roomNumber, unit, toClient);
                    }
                    else if(unit.type == MyProtocol.COMMUNICATION_PIC){ //圖片訊息
                        System.out.println(socket + " send ");
                        server.broadcastToRoom(roomNumber, unit, toClient);
                    }
                    else if(unit.type == MyProtocol.COMMUNICATION_WARN){ //緊急事件訊息
                        String str = (String)unit.contents[0];
                        System.out.println(str);
                        server.broadcastToRoom(roomNumber, unit, toClient);
                    }
                    else if(unit.type == MyProtocol.LOCATION){ //定位訊息
                    	String name = (String)unit.contents[0];
                    	Double x = (Double)unit.contents[1];
                    	Double y = (Double)unit.contents[2];
                    	System.out.println(socket + "(" + x + "," + y + ")");
                    	server.broadcastToRoom(roomNumber, unit, toClient);
                    }
                    
                    
            }
            
        } catch(SocketException e){
            ;
        } catch(EOFException e){
            ;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if(roomNumber!=null){
                server.leaveRoom(toClient,roomNumber);
                System.out.println(socket + " leave from Room " + roomNumber);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

    }
}
