package cs551.week11.PersonalFallAlert;

import java.io.*;
import java.util.*;
import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	private static String TAG = GPSTracker.class.getName();
	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean isGPSTrackingEnabled = false;
	
	Location location;
	double latitude;
	double longitude;
	
	int geocoderMaxResults = 1;
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
	protected LocationManager locationManager;
	private String provider_info;
	
	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}
	
	public void getLocation() {		
		try {
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (isGPSEnabled) {
				this.isGPSTrackingEnabled = true;
				Log.d(TAG, "Application use GPS Service");
				provider_info = LocationManager.GPS_PROVIDER;				
			} else if (isNetworkEnabled) {
				this.isGPSTrackingEnabled = true;
				Log.d(TAG, "Application use Network State to get GPS coordinates");
				provider_info = LocationManager.NETWORK_PROVIDER;
			}
			
			if (!provider_info.isEmpty()) {
				locationManager.requestLocationUpdates(provider_info,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				if (locationManager != null) {
					location = locationManager.getLastKnownLocation(provider_info);
					updateGPSCoordinates();
				}
			}
		} catch (Exception e){
			Log.e(TAG, "Impossible to connect to LocationManager", e);
		}
	}
	
	public void updateGPSCoordinates() {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}
	
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		return longitude;
	}
	
	public boolean getIsGPSTrackingEnabled() {
		return this.isGPSTrackingEnabled;
	}
	
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}
	
	@Override
	public void onLocationChanged(Location location){
		if(location != null){
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}