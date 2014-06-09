package com.netproject;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {
	/* local variables */
	private Button connectChatRoom, serviceMap, okBtn ;
	private EditText et_name;
	private String nickName;
	private Boolean isConnecting = false;
	Client client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		
		main_initial();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		
		
		return true;
	}
	

	
	//initial in Main
	private void main_initial(){
		/* Build */
		connectChatRoom = (Button) findViewById(R.id.contChat);
		serviceMap = (Button) findViewById(R.id.servMap);
		okBtn = (Button) findViewById(R.id.ok);
		et_name = (EditText) findViewById(R.id.name);

		/* Execution */
		connectChatRoom.setOnClickListener(listener);
		serviceMap.setOnClickListener(listener);
		okBtn.setOnClickListener(listener);
		
		connectChatRoom.setEnabled(false);
		serviceMap.setEnabled(false);
	}	
	
	//Listener in Main
	private Button.OnClickListener listener = new Button.OnClickListener(){
		//when click
		@Override
		public void onClick(View v){
			
			switch (v.getId())
				{
					case R.id.contChat:{
						final Intent intent = new Intent();
						intent.setClass(MainActivity.this, ChatActivity.class);											
						final Bundle bundle = new Bundle();											
						bundle.putString("name", nickName);
						intent.putExtras(bundle);
						startActivity(intent);   						
						break;
						//Go to chat activity
										
						

					}
					
					case R.id.servMap:{
						Intent intent2 = new Intent();
						intent2.setClass(MainActivity.this, LocateActivity.class);
						startActivity(intent2);
						break;
					}
					
					case R.id.ok:{
						if (et_name.getText().toString().equals("")){
							//do nothing
						}
						else{
							nickName = et_name.getText().toString();
							synchronized(Client.class){
								Client.createClient(nickName);
								while(Client.onlyClient == null){
							      ;
								}
								client = Client.createClient(nickName);								
							}
							try {
								client.enterRoom(1000);
								isConnecting = true;
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							if(isConnecting == true){
								okBtn.setEnabled(false);
								connectChatRoom.setEnabled(true);
								serviceMap.setEnabled(true);
							}
						}
						
						break;
					}
					

				}
		}
				
	};/* Listener end */
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



}
