package com.netproject;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Client {
    public final Socket socket;
    public final ObjectOutputStream toServer;
    public final ObjectInputStream fromServer;
    public final String name;
    public final String hostIP;
    public Thread sendThread;
    public Boolean isConnected;
    public static Client onlyClient;
    public Hashtable<String,LatLng> locs;
    private boolean LocsChanged;
    
    public synchronized boolean isLocsChanged() {
		return LocsChanged;
	}

	public synchronized void setLocsChanged(boolean locsChanged) {
		LocsChanged = locsChanged;
	}

	public static synchronized Client createClient(final String name){
    	if(onlyClient == null ){    		
			Thread t = new Thread(){
				public void run() {
					try {
						onlyClient = new Client(name);
					}
					 catch (UnknownHostException e) {
						 e.printStackTrace();
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				}
			};
			t.start();		
    	}
    	return onlyClient;
    }

    private Client(String name)  throws UnknownHostException, IOException  {
    	this.name = name;
        this.hostIP = "127.0.0.1";
        socket = new Socket(this.hostIP, 14055);
        toServer = new ObjectOutputStream(socket.getOutputStream());
        fromServer = new ObjectInputStream(socket.getInputStream());
        isConnected = false;
        locs = new Hashtable<String,LatLng>();
        LocsChanged = false;
    }
    
    

    public void enterRoom(int roomNumber) throws IOException {
        //System.out.print("enter room...");
        MyProtocol.Unit unit;
        Object[] contents = new Object[1];
        contents[0] = roomNumber; 
        unit = new MyProtocol.Unit(MyProtocol.ENTER_ROOM, contents);
        toServer.writeObject(unit);
        isConnected = true;
        //System.out.println("OK");
    }

    public  MyProtocol.Unit recieveUnit() throws IOException{
        MyProtocol.Unit unit;
        try {
            unit = (MyProtocol.Unit) fromServer.readObject();            
            return unit;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendText(String str ,boolean title) throws IOException {
        Object[] contents = new Object[1];
        if(title){
        	contents[0] = name + "> " + str;
        }
        else{
        	contents[0] = str;
        }
        MyProtocol.Unit unit;
        unit = new MyProtocol.Unit(MyProtocol.COMMUNICATION_TEXT, contents);
        toServer.writeObject(unit);
    }
    
    public void sendPic(InputStream is) throws IOException {        
    	byte[] fileContent = new byte[100000];
    	List<byte[]> fileContents = new ArrayList<byte[]>();
        while(is.read(fileContent)>0){
        	fileContents.add(fileContent);
        }
        is.close();
        Object[] contents = new Object[1];
        contents[0] = fileContents;
        MyProtocol.Unit unit;
        unit = new MyProtocol.Unit(MyProtocol.COMMUNICATION_PIC, contents);
        toServer.writeObject(unit);
        sendText("["+name+ "已傳送一張圖片]",false);
    }
    
    public void sendWarn() throws IOException {
        Object[] contents = new Object[1];
        contents[0] = "[Warning Message]"+ name +"發出一個緊急訊息!!" ;
        MyProtocol.Unit unit;
        unit = new MyProtocol.Unit(MyProtocol.COMMUNICATION_WARN, contents);
        toServer.writeObject(unit);
    }
    
    public void sendLoc(LatLng loc) throws IOException {
    	 Object[] contents = new Object[3];
    	 contents[0] = name;
    	 contents[1] = loc.latitude;
    	 contents[2] = loc.longitude;
    	 MyProtocol.Unit unit;
         unit = new MyProtocol.Unit(MyProtocol.LOCATION, contents);
         toServer.writeObject(unit);
    }

    public void close() {
        try {
            toServer.close();
        } catch (IOException e) {
            ;
        }
        try {
            fromServer.close();
        } catch (IOException e) {
            ;
        }
        try {
            socket.close();
        } catch (IOException e) {
            ;
        }
    }
}

