package com.amca.android.multicastapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends ListActivity {
	String ip = "224.0.0.3";
    String user = "spondob-tab";
    int port = 6789;
    
    InetAddress group;
    MulticastSocket s;
    final List<String> messages = new ArrayList<String>();
	MulticastLock lock;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );
        lock = wifi.createMulticastLock("MainActivity");
        lock.setReferenceCounted(true);
        lock.acquire();
        
        try {
            group = InetAddress.getByName(ip);
            s = new MulticastSocket(port);
            //s.setTimeToLive(2);
            s.setReuseAddress(true);
            //s.setNetworkInterface(NetworkInterface.getByName("wlan0"));
            s.joinGroup(group);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        
        Button sendButton = (Button) findViewById(R.id.button1);
        final EditText message = (EditText) findViewById(R.id.editText1);
        //final Multicast mc = new Multicast(); 
        receive();
        
        displayListMessages();
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	send(message.getText().toString());
            	message.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        if (lock != null) {
        	lock.release();
        	lock = null;
        }
    }
    
    public void receive()
	{
		Thread thread = new Thread()
		{
		    @Override
		    public void run() {
		        try {
		        	byte[] buffer = new byte[10 * 1024];
		            DatagramPacket data = new DatagramPacket(buffer, buffer.length);
		            while (true) {
		                s.receive(data);
		                String msgRcv = "Received: " + (new String(buffer, 0, data.getLength()));
		                System.out.println(msgRcv);
		                messages.add(msgRcv);
		                runOnUiThread(new Runnable() {
							
							public void run() {
								displayListMessages();
								
							}
						});
		                //
		                //Toast.makeText(MainActivity.this, msgRcv, Toast.LENGTH_LONG).show();  
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		};
		thread.start();
	}
	
	public void send(String message){
		final String msg = user + ": " + message;
		Thread thread = new Thread()
		{
		    @Override
		    public void run() {
		        try {
	                System.out.println("Sending out: " + msg);
	                DatagramPacket data = new DatagramPacket(
	                        msg.getBytes(), msg.length(), group, port);
	                s.send(data);
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		};
		thread.start(); 
	}
    
	public void displayListMessages(){
        String[] simpleArray = new String[ messages.size() ];
        messages.toArray( simpleArray );
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, simpleArray);
		setListAdapter(adapter);
	}
}
