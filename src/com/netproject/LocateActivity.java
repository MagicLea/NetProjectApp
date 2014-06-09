package com.netproject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocateActivity extends Activity implements LocationListener{
	GoogleMap gmap; //宣告 google map 物件
	float zoom;
    private LocationManager locMgr;
    private static final int LOCS_CHANGED = 0x101;
	String bestProv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locate);
		
		// 取得 google map 元件     
		gmap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap(); 
		

      	LatLng IECS = new LatLng(24.179235, 120.649654); // 資電館 
      	zoom=17; 
		gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(IECS, zoom));			
		gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);       // 一般地圖		

        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        Criteria criteria = new Criteria();
        bestProv = locMgr.getBestProvider(criteria, true); 
                
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask (){
        	Client client = Client.createClient(null);
			@Override
			public void run() {
				if(client.isLocsChanged()){
	        			Message msg = new Message () ;	        			
	        			msg.what = LOCS_CHANGED;
	        			LocateActivity.this.myChatHandler.sendMessage(msg);
	        			client.setLocsChanged(false);	        			
	        		}
			}
        	
        }, 2000);
	}

	//Thread handler
		Handler myChatHandler = new Handler() {
		    @Override
		    public void handleMessage (Message msg) {
		        switch (msg.what)
		        {
		            case LOCS_CHANGED :
						updateMap();
		            break;           	
		        }
		    }
		};
		
	
	@Override
	public void onLocationChanged(Location location) {
		Client client = Client.createClient(null);
		// 取得地圖座標值:緯度,經度   
        String x="緯=" + Double.toString(location.getLatitude());
        String y="經=" + Double.toString(location.getLongitude());
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());  
      	//zoom=17; //設定放大倍率1(地球)-21(街景)
		//gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));	
		gmap.setMyLocationEnabled(true); // 顯示定位圖示        
        client.setLocsChanged(true);
        updateMap();
        try {
			client.sendLoc(point);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
	}
	
	public void updateMap(){
		//
		Client client = Client.createClient(null);
		Hashtable<String,LatLng> locs = client.locs;
		gmap.clear();
		for(Entry<String, LatLng> entry : locs.entrySet()){			
			MarkerOptions marker = new MarkerOptions();
	        marker.draggable(false);
	        marker.position(entry.getValue());
	        marker.title(entry.getKey());
	        gmap.addMarker(marker).showInfoWindow();  
		}
		client.setLocsChanged(true);
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		// 如果GPS或網路定位開啟，更新位置
		if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locMgr.requestLocationUpdates(bestProv, 1000, 1, this);
		} else {
			Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onPause() {	
		super.onPause();
		locMgr.removeUpdates(this);
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Criteria criteria = new Criteria();
        bestProv = locMgr.getBestProvider(criteria, true);
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub		
	}	
}