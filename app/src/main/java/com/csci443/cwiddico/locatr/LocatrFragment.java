package com.csci443.cwiddico.locatr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

import com.csci443.cwiddico.locatr.database.LocCursor;
import com.csci443.cwiddico.locatr.database.LocSchema;
import com.csci443.cwiddico.locatr.database.LocSchema.LocTable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.csci443.cwiddico.locatr.database.LocHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cwiddico on 3/14/2018.
 */

public class    LocatrFragment extends SupportMapFragment {

    private static final int REQUEST_LOCATIONS_PERMISSIONS = 0;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,

};
    public FloatingActionButton fab;
    public RelativeLayout mCont;
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Bitmap mMapImage;
    private GalleryItem mMapItem;
    private Location mCurrentLocation;
    private static final String TAG = "LocatrFragment";

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private String mWeather;
    private String mTemp;


    public static LocatrFragment newInstance() {
        return new LocatrFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext().getApplicationContext();
        mDatabase = new LocHelper(mContext)
                .getWritableDatabase();
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
                                                  @Override
                                                  public boolean onMarkerClick(Marker marker) {
                                                      Toast.makeText(mContext,
                                                              marker.getPosition().latitude + " " + marker.getPosition().longitude + " " + marker.getSnippet() + "K " + marker.getTitle(),
                                                              Toast.LENGTH_SHORT).show();
                                                      return true;
                                                  }
                                              }
                );
                updateUI();

                PollService.setServiceAlarm(getActivity(), true);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mapView = super.onCreateView( inflater, container, savedInstanceState );


        FloatingActionButton fab = new FloatingActionButton(mapView.getContext());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findImage();
            }
        });

        mCont = new RelativeLayout( getActivity() ); // or other ViewGroup
        mCont.addView( mapView );
        mCont.addView(fab);
        return mCont;
    }

        @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());

        MenuItem clearItem = menu.findItem(R.id.action_clear);
        clearItem.setEnabled(mClient.isConnected());

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                mMap.clear();
                return true;

            case R.id.action_locate:
                if(hasLocationPermission()) {
                    findImage();
                }
                else{
                    requestPermissions(LOCATION_PERMISSIONS,REQUEST_LOCATIONS_PERMISSIONS);
                }
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATIONS_PERMISSIONS:
                if (hasLocationPermission()){
                    findImage();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "Got a fix: " + location);
                        new SearchTask().execute(location);
                    }
                });

    }

    private class SearchTask extends AsyncTask<Location,Void,Void> {
        private JSONObject data = null;
        private JSONObject desc = null;

        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;
        private Location mLocation;
        private String Weather;
        private String tem;
        private String main;
        private String Tempe;
        @Override
        protected Void doInBackground(Location... params) {
            mLocation = params[0];
            FlickrFetchr fetchr = new FlickrFetchr();
            List<GalleryItem> items = fetchr.searchPhotos(params[0]);
            if (items.size() == 0) {
                return null;
            }
            mGalleryItem = items.get(0);

            try {
                byte[] bytes = fetchr.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+mLocation.getLatitude()+"&lon=" + mLocation.getLongitude()+"&APPID=17f2c498c2bb194645a66d1b718957f7");
                Log.d(TAG, "doInBackground: " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer json = new StringBuffer(1024);
                String tmp = "";

                while((tmp = reader.readLine()) != null)
                    json.append(tmp).append("\n");
                reader.close();

                data = new JSONObject(json.toString());

                Log.d(TAG, "doInBackground: " + data.toString());

                Weather = data.getString("weather");

                JSONArray jsonarr = new JSONArray(Weather.toString());

                JSONObject jsonObject = jsonarr.getJSONObject(0);
                main = jsonObject.getString("main");


                //

                Weather = data.getString("main");

                JSONObject js = new JSONObject(Weather);
                Tempe = js.getString("temp");




                Log.d(TAG, "doInBackground: " + Tempe);

                if(data.getInt("cod") != 200) {
                    System.out.println("Cancelled");
                    return null;
                }


            } catch (IOException ioe) {
                Log.i(TAG, "Unable to download bitmap", ioe);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMapImage = mBitmap;
            mMapItem = mGalleryItem;
            mCurrentLocation = mLocation;
            mWeather = main;
            mTemp = Tempe;
            updateUI();
        }
    }

    private boolean hasLocationPermission(){
        int result = ContextCompat
                .checkSelfPermission(getActivity(),LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUI() {
        if (mMap == null || mMapImage == null) {
            return;
        }

        LatLng itemPoint = new LatLng(mMapItem.getLat(), mMapItem.getLon());
        LatLng myPoint = new LatLng(
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint)
                .snippet(mTemp)
                .title(mWeather);
        //mMap.clear();
        mMap.addMarker(itemMarker);
        mMap.addMarker(myMarker);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                .include(myPoint)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
        Date currentTime = Calendar.getInstance().getTime();
        Snackbar.make(mCont, currentTime + " " + mWeather + " " + mTemp, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private static ContentValues getContentValues(LatLng point){
        ContentValues values = new ContentValues();
        values.put(LocTable.Cols.LAT, point.latitude);
        values.put(LocTable.Cols.LONG, point.longitude);
        values.put(LocTable.Cols.DATE, point.latitude);
        values.put(LocTable.Cols.TIME, point.latitude);
        values.put(LocTable.Cols.WEATHER, point.latitude);
        values.put(LocTable.Cols.TEMPERATURE, point.latitude);
        return values;
    }
    public void addPoint(LatLng point){
        ContentValues values = getContentValues(point);
        mDatabase.insert(LocTable.NAME, null, values);
    }

    private LocCursor queryLoc(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LocTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new LocCursor(cursor);
    }

}
