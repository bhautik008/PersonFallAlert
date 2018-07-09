package cs551.week11.PersonalFallAlert;

import java.io.*;
import java.net.*;
import org.openintents.sensorsimulator.hardware.*;
import android.app.Activity;
import android.hardware.SensorManager;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class PersonalFallAlertActivity extends Activity{

	private SensorManagerSimulator mSensorManager;
	private Socket socket = null;
	private TextView aX, aY, aZ, oX, oY, oZ,isFall,lngText,latText;
	private Button connectBtn, disconnectBtn;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private SensorEventListener mEventListenerAccelerometer;
	private SensorEventListener mEventListenerOrientation;
	private float ax = (float) 0.0;
	private float ay = (float) 0.0;
	private float az = (float) 0.0;
	private float ox = (float) 0.0;
	private float oy = (float) 0.0;
	private float oz = (float) 0.0;
	boolean isGPS;
	GPSTracker gpsTracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aX = (TextView) findViewById(R.id.aX);
		aY = (TextView) findViewById(R.id.aY);
		aZ = (TextView) findViewById(R.id.aZ);
		oX = (TextView) findViewById(R.id.oX);
		oY = (TextView) findViewById(R.id.oY);
		oZ = (TextView) findViewById(R.id.oZ);
		isFall = (TextView) findViewById(R.id.isFall);
		lngText = (TextView) findViewById(R.id.lngText);
		latText = (TextView) findViewById(R.id.latText);
		connectBtn = (Button) findViewById(R.id.connectBtn);
		disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
		mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
		mSensorManager.connectSimulator();
		initListeners();
		connectBtn.setOnClickListener(connectSocket);
		disconnectBtn.setOnClickListener(disconnectSocket);
		gpsTracker = new GPSTracker(this);
	}

	private View.OnClickListener connectSocket = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			new createConnection().execute();
			connectBtn.setEnabled(false);
			disconnectBtn.setEnabled(true);
		}
	};
	
	private View.OnClickListener disconnectSocket = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			if (socket != null){
				try{
					socket.close();
				} catch(IOException e){
					e.printStackTrace();
				}
			}
			if (dos != null){
				try{
					dos.close();
				} catch(IOException e){
					e.printStackTrace();
				}
			}
			if (dis != null){
				try{
					dis.close();
				} catch(IOException e){
					e.printStackTrace();
				}
			}
			connectBtn.setEnabled(true);
			disconnectBtn.setEnabled(false);
		}
	};
	
	private void initListeners() {
		final double stndrGrvt = SensorManager.STANDARD_GRAVITY;
		mEventListenerAccelerometer = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				double lat = 0.0,lng = 0.0;
				float[] values = event.values;
				aX.setText("X: " + values[0]);
				aY.setText("Y: " + values[1]);
				aZ.setText("Z: " + values[2]);
				if(ax != values[0] || ay != values[1] || az != values[2]){
					ax = values[0];
					ay = values[1];
					az = values[2];
					if(gpsTracker.isGPSTrackingEnabled){
						lat = gpsTracker.getLatitude();
						lng = gpsTracker.getLongitude();
					}
					latText.setText("Latitude: "+lat);
					lngText.setText("Longitude: "+lng);
					try {
						dos.writeByte(0);
						dos.writeDouble(stndrGrvt);
						dos.flush();
						dos.writeByte(1);
						dos.writeFloat(values[0]);
						dos.flush();
						dos.writeByte(2);
						dos.writeFloat(values[1]);
						dos.flush();
						dos.writeByte(3);
						dos.writeFloat(values[2]);
						dos.flush();
						dos.writeByte(7);
						dos.writeDouble(lat);
						dos.flush();
						dos.writeByte(8);
						dos.writeDouble(lng);
						dos.flush();
					} catch (Exception e) {
						Log.e("Error", e.toString());
					}
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerOrientation = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
				oX.setText("X: " + values[0]);
				oY.setText("Y: " + values[1]);
				oZ.setText("Z: " + values[2]);
				if(ox != values[0] || oy != values[1] || oz != values[2]){
					ox = values[0];
					oy = values[1];
					oz = values[2];
					try {
						dos.writeByte(4);
						dos.writeFloat(values[0]);
						dos.flush();
						dos.writeByte(5);
						dos.writeFloat(values[1]);
						dos.flush();
						dos.writeByte(6);
						dos.writeFloat(values[2]);
						dos.flush();
					} catch (Exception e) {
						Log.e("Error", e.toString());
					}
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mEventListenerAccelerometer,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(mEventListenerOrientation,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(mEventListenerAccelerometer);
		mSensorManager.unregisterListener(mEventListenerOrientation);
		super.onStop();
	}
	
	private class createConnection extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... arg0){
			try{
				socket = new Socket("192.168.1.175", 8885);
				dos = new DataOutputStream(socket.getOutputStream());
				dis = new DataInputStream(socket.getInputStream());
				try {
					int risk = dis.readInt();
					if(risk == 1){
						isFall.setText("Fall Down (Yes/No): Yes");
					} else{
						isFall.setText("Fall Down (Yes/No): No");
					}
				} catch (Exception e) {
					Log.e("ERROR In Get", e.toString());
				}
			} catch(UnknownHostException e){
				Log.e("Error", e.toString());
			} catch(Exception e){
				Log.e("Error", e.toString());
			}
			return null;
		}
		
		protected void onPostExecute(Void void1){
		}
	}
}