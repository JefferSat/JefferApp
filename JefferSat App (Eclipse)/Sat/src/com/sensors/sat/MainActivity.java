package com.sensors.sat;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.widget.TextView;


public class MainActivity extends IOIOActivity  {  //"Extends" gives mainactivity all of the attributes, properties and methods of "IOIOactivity"
	private UncaughtExceptionHandler defaultUEH;   // Variable for error handler, which specifies how the thread (a particular process) handles an error
    private TextView mLatLng;                      // Textview displays the current date or time, which is associated with each latitude and longitude variable
    private TextView mBgSpdAlt;                    // Same
    private TextView mLight;                       // Same
    private TextView mGrav;                        // Same
    private TextView mMag;                         // Same
    private TextView mGyro;                        // Same
    private TextView mLED;                         // Same
    private TextView mBatt;                        // Same
    private LocationManager locationManager;       //testing
    private SensorManager mSensorManager;          //testing 4
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;
    public double lat;
    public double lng;
    public double light;
    public double speed;
    public double mph;
    public float bearing;
    public double alt;
    public float gravX;
    public float gravY;
    public float gravZ;
    public float magX;
    public float magY;
    public float magZ;
    public float gyroX;
    public float gyroY;
    public float gyroZ;
    public float batt;
    private Uart uart;
    private Uart uart2;
    private InputStream in;
    private OutputStream out;
    private InputStream in2;
    private OutputStream out2;
    public String b;
    public String temp2;
    public String AT;
    public String read;
    private Camera mCamera;

    
 
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       mLatLng = (TextView) findViewById(R.id.latlng);
       mBgSpdAlt = (TextView) findViewById(R.id.bgspdalt);
       mLight = (TextView) findViewById(R.id.light);
       mGrav = (TextView) findViewById(R.id.grav);
       mMag = (TextView) findViewById(R.id.mag);
       mGyro = (TextView) findViewById(R.id.gyro);
       mLED = (TextView) findViewById(R.id.led);
       mBatt = (TextView) findViewById(R.id.batt);
       
       mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
       mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

       LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, listener);
       
       mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);   
       Sensor LightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
       mSensorManager.registerListener(mSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
       
       Sensor Gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);   
       mSensorManager.registerListener(mSensorListener, Gravity, SensorManager.SENSOR_DELAY_NORMAL);       
   
       Sensor MagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);   
       mSensorManager.registerListener(mSensorListener, MagneticField, SensorManager.SENSOR_DELAY_NORMAL); 
 
       Sensor GyroScope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);   
       mSensorManager.registerListener(mSensorListener, GyroScope, SensorManager.SENSOR_DELAY_NORMAL);
       
       mWakeLock.acquire();
       


    }
    
	class Looper extends BaseIOIOLooper {
		private DigitalOutput led_;
		private int bufferSize = 128;
		private AnalogInput mBatt;
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			uart = ioio_.openUart(4, 5, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
			out = uart.getOutputStream();
			in = uart.getInputStream();
			uart2 = ioio_.openUart(10, 11, 19200, Uart.Parity.NONE, Uart.StopBits.ONE);
			out2 = uart2.getOutputStream();
			in2 = uart2.getInputStream();
			mBatt = ioio_.openAnalogInput(38);

		}
		

	    public void wait(int ms)
	    {
			try 
			{
				Thread.sleep(ms);	
			} 
			catch (InterruptedException e)
			{
			}
	    }
	    
	    
	    //IOIO Loop
	    
		@Override
		public void loop() throws ConnectionLostException 
		{
			led_.write(false);
			wait(100);
		
			try
			{
				batt = (float) ((mBatt.read())*15.6697);
			}
			catch (InterruptedException e)
			{
				//ioio_.disconnect();
			}	

			/*UART Communications - Radio Modem */
			try
			{
			  int availableBytes = in.available();
			  if(in.available()>0)
			  {
			
				  byte[] readBuffer = new byte [bufferSize];
				  in.read(readBuffer, 0, availableBytes);
				  char[] temp = (new String(readBuffer,0, availableBytes)).toCharArray();
				  temp2 = new String(temp);
			
				  if(temp2.contains("lgt"))
				  {
					 String lit = "Light Sensor: " + light;
					 out.write(lit.getBytes());
					 out.write(13);
					  
				  }
				  
				  if(temp2.contains("bat"))
				  {
					 String battery = "Battery: " + batt + "V";
					 out.write(battery.getBytes());
					 out.write(13);  
				  }
				  
				  if(temp2.contains("gps"))
				  {
					 String latlng = "Lat: " + lat + " Long: " + lng;
					 out.write(latlng.getBytes());
					 out.write(13);
					 String bng = "Bearing: " + bearing;
					 out.write(bng.getBytes());
					 out.write(13);
					 String sped = "Speed: " + speed;
					 out.write(sped.getBytes());
					 out.write(13);
					 String altitude = "Altitude: " + alt;
					 out.write(altitude.getBytes());
					 out.write(13);
					 
				  }
				  
				  if(temp2.contains("grv"))
				  {
					 String gravity = "Gravity: ";
					 out.write(gravity.getBytes());
					 out.write(13);
					 String gvx = "X: " + gravX;
					 out.write(gvx.getBytes());
					 out.write(13);
					 String gvy = "Y: " + gravY;
					 out.write(gvy.getBytes());
					 out.write(13);
					 String gvz = "Z: " + gravZ;
					 out.write(gvz.getBytes());
					 out.write(13);
				  }
				  
				  if(temp2.contains("mag"))
				  {
					 String magnet = "Magnetometer: ";
					 out.write(magnet.getBytes());
					 out.write(13);
					 String mgx = "X: " + magX;
					 out.write(mgx.getBytes());
					 out.write(13);
					 String mgy = "Y: " + magY;
					 out.write(mgy.getBytes());
					 out.write(13);
					 String mgz = "Z: " + magZ;
					 out.write(mgz.getBytes());
					 out.write(13);
				  }
				  
				  if(temp2.contains("gyo"))
				  {
					 String gyroscope = "Gyro: ";
					 out.write(gyroscope.getBytes());
					 out.write(13);
					 String gyx = "X: " + gyroX;
					 out.write(gyx.getBytes());
					 out.write(13);
					 String gyy = "Y: " + gyroY;
					 out.write(gyy.getBytes());
					 out.write(13);
					 String gyz = "Z: " + gyroZ;
					 out.write(gyz.getBytes());
					 out.write(13);
				  }
				  if(temp2.contains("pic"))
				  {
					  mCamera = Camera.open();
					  mCamera.takePicture(null, null, jpegCallback);
					  if (mCamera != null)
					  {
				            mCamera.release();
				            mCamera = null;
				      }
				  }
			  }
			}

			catch(IOException e)
			{
				e.printStackTrace();
			}
			
		 
		}
	}


	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	
    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        	lat = location.getLatitude();
        	lng = location.getLongitude();
        	speed = location.getSpeed();
        	bearing = location.getBearing();
        	alt = location.getAltitude();
        	mph = (speed * 3.2808399*3600) / 5280;
            mLatLng.setText("Lat: "+ lat + "\n" + "Long: " + lng); 
            mBgSpdAlt.setText("Bearing: " + bearing + "\n" + "Speed(m/s): " + speed + "\n" + " Altitude(m): " + alt);

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
     
    @Override
    protected void onResume() {
        super.onResume();
      
    }
    
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(listener);
        mSensorManager.unregisterListener(mSensorListener); 
        mWakeLock.release();
    }
    
    protected void onPause() {
    	super.onPause();  	
    }

  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private final SensorEventListener mSensorListener = new SensorEventListener() {
    
    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

    	@Override
    	public void onSensorChanged(SensorEvent event) {
    	Sensor sensor = event.sensor;
    	if(sensor.getType() == Sensor.TYPE_LIGHT) {
		  light = event.values[0];
		  mLight.setText("\n"+ "Light Sensor: " + light);
	

    	}
    	else if(sensor.getType() == Sensor.TYPE_GRAVITY) {
    	  gravX = event.values[0];
    	  gravY = event.values[1];
    	  gravZ = event.values[2];
    	  mGrav.setText("\n" + "Grav X: " + gravX + "\n" + "Grav Y: " + gravY +"\n" + "Grav Z: " + gravZ);
    	
    	}
    	else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      	  magX = event.values[0];
      	  magY = event.values[1];
      	  magZ = event.values[2];

      	  mMag.setText("\n" + "Mag X: " + magX + "\n" + "Mag Y: " + magY + "\n" + "Mag Z: " + magZ);
      	  mBatt.setText("Battery: " + batt + "V" + "\n");
      	} 
    	else if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
    	  gyroX = event.values[0];
          gyroY = event.values[1];
          gyroZ = event.values[2];
    	  mLED.setText("\n" + "Radio Modem: " + temp2);
          mGyro.setText("\n" + "Gyro X: " + gyroX + "\n" + "Gyro Y: " + gyroY + "\n" + "Gyro Z: " + gyroZ);  
    	}
    	
	}	
	};
	
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() 
    {
    	public void uncaughtException(Thread thread, Throwable ex)
    	{
            /*PendingIntent myActivity = PendingIntent.getActivity(getBaseContext(), 1, 
                    new Intent(getBaseContext(), MainActivity.class), PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                    15000, myActivity ); */
    		System.exit(2);
    		defaultUEH.uncaughtException(thread, ex);
    	}
    	
    };
    
    public MainActivity()
    {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }
    
    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback()
    {
    	public void onPictureTaken(byte[] data, Camera camera)
    	{
    		FileOutputStream outStream = null;
    		try
    		{
    			outStream = new FileOutputStream(String.format("/sdcard/JeffAppPic%d.jpg", System.currentTimeMillis()));
    			outStream.write(data);
    			outStream.close();
    			
    		}
    		catch (FileNotFoundException e)
    		{
    			e.printStackTrace();
    		}
    		catch (IOException e)
    		{
    			e.printStackTrace();
    		}
    	}
    };
}












/*	Sat Modem Communications
 

 			  if(temp2.contains("+CSQ:3") || temp2.contains("+CSQ:4") || temp2.contains("+CSQ:5"))
 
		  {
			j=2;
			if(i == 0)
			{
			  wait(3000);
			  /*String AT = "AT+SBDWT=Light: " + light;
			  out.write(AT.getBytes());
			  out.write(13);
			  wait(2000);
			  String ATB = "AT+SBDIX";
			   out.write(ATB.getBytes());
			   out.write(13);
			   i++;
			   j++;
			


			}
		     
		  }
		  
		  if(temp2.contains("+SBDIX: 0") || temp2.contains("+SBDIX: 1") || temp2.contains("+SBDIX: 2") || temp2.contains("SBDRING"))
		  {
			 if (i==1) 
			 {
			  wait(2000);
			  String ATC = "AT+SBDRT";
			  out.write(ATC.getBytes());
			  out.write(13);
			  i++;
	
		/*			try
			{
				if(j<1)
				{
					wait(2000);
					AT="AT+CSQ";
					out.write(AT.getBytes());
					out.write(13);
		
				}
				
			}
			catch(IOException e)
			{
				e.printStackTrace();
			} /*
			 }	  
		  }/*
		/*
		  if(temp2.contains("light"))
		  {
			  String AT = "AT+SBDWT=Light: " + light;
			  out.write(AT.getBytes());
			  out.write(13);
			  i++;
		  }
		  if(i==3)
		  {
			  String AT = "AT+SBDIX";
			  out.write(AT.getBytes());
			  out.write(13);
			  i++;					  
		  }
		*/
	  