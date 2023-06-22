package com.example.regresarllamada;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    Button btnEnviar;
    //private Object permissions;
    String mensajeTexto = "";
    TextView txtTitulo;
    // private TelephonyManager telephonyManager;
    // private TelephonyCallback.CallStateListener callStateListener;
    // private CallStateListener callStateListener;
    private static String TAG = "myCallReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        CallStateListener phoneListener = new CallStateListener();
        //  telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //  telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);
        // PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    public class CallStateListener extends PhoneStateListener {
        private static final String TAG = "CallStateListener";
        //public CallReceiver() { }

        protected void onIncomingCallStarted(int ctx, String number, Date start) {
            Log.d("onIncomingCallStarted()",number);
        }

        protected void onOutgoingCallStarted(int ctx, String number, Date start) {
            Log.d(TAG, "onOutgoingCallStarted() number: " + number);
        }

        protected void onIncomingCallEnded(int ctx, String number, Date start, Date end) {
            Log.i(TAG, "onIncomingCallEnded() : (savedNumber: "+ number + ", callStartTime: " +start.toString());
            Intent dial = new Intent(Intent.ACTION_CALL);
            dial.setData(Uri.parse("tel:"+number));
            startActivity(dial);
        }

        protected void onOutgoingCallEnded(int ctx, String savedNumber, Date callStartTime, Date end) {
            Log.i(TAG, "onOutgoingCallEnded() : (savedNumber: "+ savedNumber + ", callStartTime: " +callStartTime.toString());
        }

        protected void onMissedCall(int ctx, String incomingNumber, Date start) {
            Log.i(TAG, "onMissedCall() : (savedNumber: "+ incomingNumber + ", callStartTime: " +start.toString());
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            txtTitulo.setText(incomingNumber);
            Toast toast = Toast.makeText(context, "la Katia inicio", duration);
            toast.show();
            if(lastState == state){
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                     toast = Toast.makeText(context, "la Katia"+TelephonyManager.CALL_STATE_IDLE, duration);
                     toast.show();
                     Intent dial = new Intent(Intent.ACTION_CALL);
                     dial.setData(Uri.parse("tel:"+incomingNumber));
                     startActivity(dial);
                    // Llamada finalizada (llamada perdida)
                   if(lastState == TelephonyManager.CALL_STATE_RINGING){
                        onMissedCall(state, savedNumber, callStartTime);
                    }
                    else if(isIncoming){


                        onIncomingCallEnded(state, savedNumber, callStartTime, new Date());
                    }
                    else{
                        onOutgoingCallEnded(state, savedNumber, callStartTime, new Date());
                    }
                    break;
                  /*  Log.d(TAG, "Llamada perdida");
                    Intent dial = new Intent(Intent.ACTION_CALL);
                    dial.setData(Uri.parse("tel:7751334349"));
                    startActivity(dial);*/
                case TelephonyManager.CALL_STATE_RINGING:
                    // Llamada entrante
                    toast = Toast.makeText(context, "isIncoming"+isIncoming, duration);
                    toast.show();
                    mensaje(incomingNumber);
                    Log.d(TAG, "Llamada entrante");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // Llamada en curso
                    Log.d(TAG, "Llamada en curso");
                    break;
            }
            lastState = state;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallStateListener phoneListener = new CallStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        // Detener la escucha del estado de la llamada
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
    }
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }*/

    public void mensaje(String numero) {
        String URL="http://maps.google.com/maps?&z=15&mrt=loc&t=m&q=";
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            mensajeTexto  = "SIN PERMISOS";
        }
        else {
            /*Se asigna a la clase LocationManager el servicio a nivel de sistema a partir del nombre.*/
            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitudeGPS = loc.getLongitude();
            double latitudeGPS = loc.getLatitude();

            //OBTENER DIRECCION
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(latitudeGPS, longitudeGPS, 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    String direccion = DirCalle.getAddressLine(0);
                    mensajeTexto = "Estoy en "+URL+ latitudeGPS + "+" + longitudeGPS;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numero, null,mensajeTexto,null,null);
        //Toast.makeText(getApplicationContext(), mensajeTexto,Toast.LENGTH_LONG).show();
    }
}
