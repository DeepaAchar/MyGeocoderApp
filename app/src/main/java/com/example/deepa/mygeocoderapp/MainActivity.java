package com.example.deepa.mygeocoderapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    private TextView textViewAddress;
    private TextView textViewLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLocation = (TextView) findViewById(R.id.textViewLoc);
        textViewAddress = (TextView) findViewById(R.id.textViewAddr);

        if(mGoogleApiClient==null){
            mGoogleApiClient=new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }




    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(currentLocation!=null){
            String latitude=String.valueOf(currentLocation.getLatitude());
            String longitude=String.valueOf(currentLocation.getLongitude());
            textViewLocation.setText("Latitude:"+latitude+"  Longitude:"+longitude);
            (new GetAddress(this)).execute(currentLocation);
        }
        else{
            Toast.makeText( this,"No location detected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i("Main A ctivity:","Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i("Main Activity:", "Connection failed:"+connectionResult.getErrorCode());
    }

    //Inner class
    private class GetAddress extends AsyncTask <Location, Void, String>{

        Context contxt;
        String addrText;

        public GetAddress(Context context){
            super();
            contxt=context;
        }

        @Override
        protected void onPostExecute(String s) {
            textViewAddress.setText("Address:   "+addrText);
        }

        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder=new Geocoder(contxt, Locale.getDefault());

            Location loc=params[0];
           List<Address> addresses=null;
            try{
                addresses=geocoder.getFromLocation(loc.getLatitude(),loc.getLongitude(),1);
            } catch (IOException e) {
                Log.e("Main Activity:", "IO Exception...! ");
                e.printStackTrace();
                return ("IO Exception while trying to get address");
            }catch(IllegalArgumentException e){
                Log.e("Main Activity:", "Illegal Arguments...!");
                e.printStackTrace();
                return ("Illegal arguments");
            }

            if(addresses!=null && addresses.size()>0){
                Address addr=addresses.get(0);
               addrText=addr.getLocality()+", "+addr.getAdminArea()+", "+addr.getCountryName()+",  "+addr.getPostalCode();
                return addrText;
            }

            return ("No address found...!");
        }
    }
}
