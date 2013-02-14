package com.amca.android.multicastapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class Multicast {
    String ip = "224.0.0.3";
    String user = "spondob-tab";
    int port = 6789;
    
    InetAddress group;
    MulticastSocket s;
    List<String> messages = new ArrayList<String>();
    public Multicast(){
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
}
