package server;

import java.io.*;
import java.net.*;
public class PoolEchoServer extends Thread{
	
	public final static int defaultPort = 8885;
	static ServerSocket serverSocket;
	static int num_threads = 10;
	private float ax = (float) 0.0;
	private float ay = (float) 0.0;
	private float az = (float) 0.0;
	private float ox = (float) 0.0;
	private float oy = (float) 0.0;
	private float oz = (float) 0.0;
	private double stndrGrvt, lat = 0.0, lng = 0.0;
	
	public static void main(String[] args){
		int port = defaultPort;
		try{
			port = Integer.parseInt(args[0]);
		} catch(Exception e){ }
		if(port <= 0 || port >= 65536){
			port = defaultPort;
		}
		
		try{
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Server socket started!!!");
			for(int i=0;i<num_threads;i++){
				System.out.println("Create num_threads "+i+" Port: "+port);
				PoolEchoServer pes = new PoolEchoServer(ss);
				pes.start();
			}
		} catch(Exception e){
			System.err.println(e);
		}
	}

	public PoolEchoServer(ServerSocket ss) {
		serverSocket = ss;
	}
	
	public void run(){
		while(true){
			try{
				DataOutputStream dos;
				DataInputStream dis;
				Socket connection = serverSocket.accept();
				System.out.println("Accept Client!!!");
				dos = new DataOutputStream(connection.getOutputStream());
				dis = new DataInputStream(connection.getInputStream());
				while(true){
					byte messageType = dis.readByte();
					switch(messageType){
						case 0:
							stndrGrvt = dis.readDouble();
							break;
						case 1:
							ax = dis.readFloat();
							break;
						case 2:
							ay = dis.readFloat();
							break;
						case 3:
							az = dis.readFloat();
							break;
						case 4:
							ox = dis.readFloat();
							break;
						case 5:
							oy = dis.readFloat();
							break;
						case 6:
							oz = dis.readFloat();
							break;
						case 7:
							lat = dis.readDouble();
							break;
						case 8:
							lng = dis.readDouble();
							break;
					}
					int risk = checkFallArm();
					if(risk == 1){
						dos.writeInt(1);
					} else{
						dos.writeInt(0);
					}
				}
			} catch(Exception e){
				System.out.println(e.toString());
			}
		}
	}

	private int checkFallArm() {
		double a = Math.round(Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2)));
		int ca = Math.abs((int) (a - stndrGrvt));
		int risk;
		if(ca == 9 || ca == 10){
			risk = 1;
		} else if(ca == 7 || ca == 8){
			risk = 2;
		} else if(ca == 4 || ca == 5 || ca == 6){
			risk = 3;
		} else if(ca == 1 || ca == 2 || ca == 3){
			risk = 4;
		} else{
			risk = 5;
		}
		System.out.println("Accelerometer:");
		System.out.println("X: "+ax);
		System.out.println("Y: "+ay);
		System.out.println("Z: "+az);
		System.out.println("Orientation:");
		System.out.println("X: "+ox);
		System.out.println("Y: "+oy);
		System.out.println("Z: "+oz);
		if(risk < 3){
			System.out.println("Fall Down (Yes/No): Yes");
			System.out.println("Longitude: "+lng);
			System.out.println("Latitude: "+lat);
			return 1; 
		} else{
			System.out.println("Fall Down (Yes/No): No");
			System.out.println("Longitude: "+lng);
			System.out.println("Latitude: "+lat);
			return 0;
		}
	}
	
	private int checkFallArmKNN(){
		double a = Math.round(Math.sqrt(Math.pow((ax+ay+az-ox-oy-oz), 2)));
		System.out.println(a);
		System.out.println((a>0?"Not Fall":"Fall"));
		return 0;
	}
}