
package com.netproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class ChatActivity extends Activity {
	/* local variables */
	private String chatTemp, revName;
	private MyProtocol.Unit chatRev;
	private Button btn_send, btn_with, btn_warn;
	private EditText eText_typing;
	private TextView tv_chat, tv_roomInfo;
	private static final int GUI_OK = 0x101;
	private static final int CONNECTED = 0x102;
	Client client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);	
		

		
		Intent intent = this.getIntent();
		final Bundle bundle =intent.getExtras();
		
		revName = bundle.getString("name");
		chah_initial();		
		
		//read data thread
		Thread readThread = new Thread(){
			public void run() {
					client = Client.createClient(null);
					Message msg = new Message () ;	        			
        			msg.what = CONNECTED;
        			ChatActivity.this.myChatHandler.sendMessage (msg); 
				
				//receive
	        	while (true) {
	        		try {
	        			msg = new Message () ;	        			
	        			chatRev =  client.recieveUnit();
	        			msg.what = GUI_OK;
	        			ChatActivity.this.myChatHandler.sendMessage (msg) ; 
	        		} catch (SocketException e) {
	                    ;
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }
		};
		readThread.start();		
		
		
		
		
	}
	
	//Thread handler
	Handler myChatHandler = new Handler() {
	    @Override
	    public void handleMessage (Message msg) {
	        switch (msg.what)
	        {
	            case GUI_OK :
				try {
					transfromData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            break;
	            case CONNECTED :
	            	tv_roomInfo.setText("已進入房間編號 1000");
	            	break;	            	
	        }
	    }
	};
	
	//receive data transform
	private void transfromData() throws IOException{
		if (chatRev.type == MyProtocol.COMMUNICATION_TEXT) { //if text
            String strRev = (String) chatRev.contents[0];
            tv_chat.setTextColor(Color.BLACK);
            tv_chat.setTextSize(20);
			tv_chat.append(strRev + "\n");           
        }
        else if(chatRev.type == MyProtocol.COMMUNICATION_PIC){ //if picture
            String fileName = "receive01.jpg";
            List<byte[]> fileContents = (List<byte[]>) chatRev.contents[0];
            File tarFile = Environment.getExternalStorageDirectory();
            FileOutputStream fos = new FileOutputStream(tarFile.toString() + File.separator + fileName);             
            for(byte[] fileContent : fileContents){
            	fos.write(fileContent);
            }
            fos.close();
        }
        else if (chatRev.type ==  MyProtocol.COMMUNICATION_WARN){ //if warning
        	String strRev = (String) chatRev.contents[0];
			tv_chat.append(strRev + "\n"); 

		}
        else if(chatRev.type == MyProtocol.LOCATION){ //if location
        	String name = (String)chatRev.contents[0];
        	Double x = (Double)chatRev.contents[1];
        	Double y = (Double)chatRev.contents[2];
        	LatLng loc = new LatLng(x,y);
        	client.locs.put(name, loc);
        }
	}

	
	
	//selected pictures and return
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (resultCode == RESULT_OK) {
	    	//get URI of picture
	    	Uri uri = data.getData();
	    	try {
				client.sendPic(getContentResolver().openInputStream(uri));
				Toast.makeText(this, "已送出圖片", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "傳送圖片失敗", Toast.LENGTH_LONG).show();
			}	    				
		}
	}
		
	//initial in Chat
	public void chah_initial(){
		/* Build */
		btn_send = (Button) findViewById(R.id.send);
		btn_with = (Button) findViewById(R.id.with);
		btn_warn = (Button) findViewById(R.id.warn);
		eText_typing = (EditText) findViewById(R.id.typing);
		tv_chat = (TextView) findViewById(R.id.chatContext);
		tv_roomInfo = (TextView) findViewById(R.id.roomInf);
		/* Execution */
		btn_send.setOnClickListener(listener);
		btn_with.setOnClickListener(listener);
		btn_warn.setOnClickListener(listener);
		eText_typing.setOnClickListener(listener);	
		}
	
	//Listener in Chat
	private Button.OnClickListener listener = new Button.OnClickListener(){
		/* click */
		@Override
		public void onClick(View v){
			switch (v.getId())
				{
					case R.id.send:{
						/* local variables */
						chatTemp = eText_typing.getText().toString();
						try {								
							//Get chat Content								
							if(chatTemp.equals("")){
								//do nothing
							}
							else{
								client.sendText(chatTemp,true);
							}
							
						} catch (IOException e) {
							
							
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//Clear typing
						eText_typing.setText("");								
						//add to text view
						tv_chat.setTextColor(Color.BLUE);
						tv_chat.setTextSize(20);
						tv_chat.append(revName + " > " + chatTemp + "\n");
						
						break;
					}
					
						
					case R.id.with:{
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(intent, 1);
					
						break;
					}
					
					case R.id.warn:{
						try {
							client.sendWarn();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			
		}/* on click end*/	
	};/* listener end */	
	

}
