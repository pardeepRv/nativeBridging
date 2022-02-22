/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 * https://developers.arcgis.com/android/sample-code/local-mbtiles/
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 NOAA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *//*


package com.newproject.ctsMap;


import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class Main2Activity extends Activity {

    private static final String TILE_PATH = "/ChartTileService/";
    private static final String FILE_PATH = "Tiles";
    private static final String TILE9_URL = "https://tileservice.charts.noaa.gov/mbtiles/50000_1/MBTILES_08.mbtiles";
    MBTilesLayer[] layers = new MBTilesLayer[28];
    ArcGISTiledMapServiceLayer mArcGISTiledMapServiceLayer;
    MapView mMapView = null;
    boolean mActiveNetwork;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private String[] mMbTilesNames;
    private String nameOfFile = "";
    private DownloadManager.Request request;
    private File file;
    private File downloadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Object title = getTitle();

        mTitle = mDrawerTitle = getTitle();
        mMbTilesNames = getResources().getStringArray(gov.noaa.charts.tileservice.ctsdemo.R.array.mbtiles_array);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
//        ArcGISRuntimeEnvironment.setApiKey("AAPKad0a00328a4b44aa8de1f28d6031bda4ZTazxqhiS7IyKkC-18w6cdAIJI1y7FmUdmX3J0VFp8pFSuS1YUflqPGI7pX3dhWd")

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(gov.noaa.charts.tileservice.ctsdemo.R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMbTilesNames));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  */
/* host Activity *//*

                mDrawerLayout,         */
/* DrawerLayout object *//*

                */
/* nav drawer image to replace 'Up' caret *//*

                gov.noaa.charts.tileservice.ctsdemo.R.string.drawer_open,  */
/* "open drawer" description for accessibility *//*

                gov.noaa.charts.tileservice.ctsdemo.R.string.drawer_close  */
/* "close drawer" description for accessibility *//*

        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Retrieve the map and initial extent from XML layout
        mMapView = findViewById(R.id.map);

        // create an ArcGISTiledMapServiceLayer as a background if network available
        mActiveNetwork = isNetworkAvailable();
        if (mActiveNetwork) {
            mArcGISTiledMapServiceLayer = new ArcGISTiledMapServiceLayer(
//                    "https://tileservice.charts.noaa.gov/mbtiles/50000_1/MBTILES_08.mbtiles");
                    "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer");
            // Add tiled layer to MapView
            mMapView.addLayer(mArcGISTiledMapServiceLayer);
        } else {
            Toast toast = Toast.makeText(this, gov.noaa.charts.tileservice.ctsdemo.R.string.offline_message, Toast.LENGTH_SHORT);
            toast.show();
        }

        // enable map to wrap around
        mMapView.enableWrapAround(true);

        if (savedInstanceState != null) {
            List layerState = (List) savedInstanceState.get("layerState");
            if (layerState != null) {
                for (int i = 0; i < layerState.size(); i++) {
                    boolean selected = (boolean) layerState.get(i);
                    if (selected) {
                        MBTilesLayer layer = new ChartTileServiceLayer(Environment.getExternalStorageDirectory() +
                                TILE_PATH + "/" + mMbTilesNames[i] + ".mbtiles",
                                MainActivity.this.getBaseContext());
                        mMapView.addLayer(layer);
                        layers[i] = layer;
                    }
                }
                Toast.makeText(this, "Restored " + layerState.size() + " layers", Toast.LENGTH_SHORT).show();
            }

        }

        mMapView.enableWrapAround(true);

        // Handle tap event to demo UTFGrid metadata retrieval
        mMapView.setOnSingleTapListener(new OnSingleTapListener() {
            private static final long serialVersionUID = 1L;

            public void onSingleTap(float screenX, float screenY) {
                if (MapTilePoint.mScaleMap.containsKey(MainActivity.this.mMapView.getScale())) {
                    MBTilesLayer firstLayer = MainActivity.this.findFirstLayer();
                    if (firstLayer != null) {
                        TiledServiceLayer.TileInfo tileInfo = firstLayer.mTileInfo;
                        MapTilePoint localMapTilePoint = new MapTilePoint(MainActivity.this.mMapView.toMapPoint(screenX, screenY), MainActivity.this.mMapView.getScale(), MainActivity.this.layers);
                        try {
                            String json = localMapTilePoint.getGridJson();
                            Point screenPoint = new Point(Math.round(screenX), Math.round(screenY));
//                          create a map point from screen point
                            Point mapPoint = mMapView.toMapPoint(screenPoint);
//                          convert to WGS84 for lat/lon format
//                          Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReference.getWgs84());
//                          format output
                            Log.d("TAG MAP POINT", "Lat: " + String.format("%.4f", tileInfo.getOrigin().getX()) + ", Lon: " + String.format("%.4f", tileInfo.getOrigin().getX()));

                            Toast.makeText(MainActivity.this, "/" + localMapTilePoint.getMapTile().getZ() + "/" + localMapTilePoint.getMapTile().getX() + "/" + localMapTilePoint.getMapTile().getY() + " (" + localMapTilePoint.getX() + ", " + localMapTilePoint.getX() + ") " + json, Toast.LENGTH_SHORT).show();
                        } catch (Exception localException) {
                            Log.e("MainActivity", "Error getting grid json", localException);
                        }
                    }
                }
            }
        });

        // When zooming in or out, make sure we 'snap' to a scale matching a zoom level.
        mMapView.setOnZoomListener(new OnZoomListener() {
            private static final long serialVersionUID = 1L;
            double mInitialScale;

            public void postAction(float param1, float param2, double param3) {
                double scale = MainActivity.this.findNearestScale(this.mInitialScale, MainActivity.this.mMapView.getScale());
                if (scale != MainActivity.this.mMapView.getScale()) {
                    Log.i("MainActivity", "Snapping to scale: " + scale);
                    MainActivity.this.mMapView.zoomToScale(MainActivity.this.mMapView.getCenter(), scale);
                    return;
                }
                Log.i("MainActivity", "Already at nearest scale");
            }

            public void preAction(float param1, float param2, double param3) {
                this.mInitialScale = MainActivity.this.mMapView.getScale();
            }
        });
//        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        downloadTileFile(TILE9_URL);
        requestPermissions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List layerState = new ArrayList<Boolean>();
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                layerState.add(true);
            } else {
                layerState.add(false);
            }
        }
        outState.putSerializable("layerState", (Serializable) layerState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(gov.noaa.charts.tileservice.ctsdemo.R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    */
/* Called whenever we call invalidateOptionsMenu() *//*

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(gov.noaa.charts.tileservice.ctsdemo.R.id.action_info).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        if (item.getItemId() == R.id.action_info) {
            String url = "http://tileservice.charts.noaa.gov/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    */
/* The click listner for ListView in the navigation drawer *//*

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (layers[position] == null) {
                Log.i("MainActivity", Environment.getExternalStorageDirectory().getAbsolutePath() + "/tiles/" + mMbTilesNames[position] + ".mbtiles");

                // MBTilesLayer layer = new MBTilesLayer(Environment.getExternalStorageDirectory() +
                MBTilesLayer layer = new ChartTileServiceLayer(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/ChartTileService/" + mMbTilesNames[position] + ".mbtiles",
                        MainActivity.this.getBaseContext());

                mMapView.addLayer(layer);
                layers[position] = layer;
            } else {
                mMapView.removeLayer(layers[position]);
                layers[position] = null;
            }
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    */
/**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     *//*


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(onDownloadComplete);
    }

    private MBTilesLayer findFirstLayer() {
        MBTilesLayer localMBTilesLayer = null;
        int i = 0;
        while (i < this.layers.length) {
            if (this.layers[i] != null) {
                localMBTilesLayer = this.layers[i];
            }
            i += 1;
        }
        return localMBTilesLayer;
    }


    private double findNearestScale(double initialScale, double currentScale) {
        if (findFirstLayer()!=null && findFirstLayer().getTileInfo()!=null) {
            double[] scales = findFirstLayer().getTileInfo().getScales();
            int i = 0;
            while (i < scales.length) {
                if (i < scales.length - 1) {
                    double d1 = scales[i];
                    double d2 = scales[(i + 1)];
                    Log.i("MainActivity", "scale1: " + d1);
                    Log.i("MainActivity", "currentScale: " + currentScale);
                    Log.i("MainActivity", "scale2: " + d2);
                    if ((currentScale < d1) && (currentScale > d2)) {
                        if (currentScale > initialScale) {
                            return d1;
                        }
                        return d2;
                    }
                }
                i += 1;
            }
        }
        return currentScale;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        111);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadTileFile(TILE9_URL);
            } else {
                Toast.makeText(this, "Permission Denied..!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private long downloadID;

    private void downloadTileFile(String url) {
        request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading MBTile...");  //set title for notification in status_bar
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  //flag for if you want to show notification in status or not

        //String nameOfFile = "YourFileName.pdf";    //if you want to give file_name manually
        nameOfFile = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url)); //fetching name of file and type from server

        downloadFile = new File(Environment.getExternalStorageDirectory() + File.separator + DIRECTORY_DOWNLOADS + File.separator + FILE_PATH);       // location, where to download file in external directory

        Log.d("Path Data_1", "" + downloadFile.getPath());
        file = new File(downloadFile.getPath() + File.separator + nameOfFile);

        // location, where to download file in external directory
        if (downloadFile.exists() && file.exists() && file.isFile()) {
            try{
                MBTilesLayer layer = new ChartTileServiceLayer(file.getPath(),
                        MainActivity.this.getBaseContext());

                mMapView.addLayer(layer);
                layers[0] = layer;
            }catch (Exception e){
                file.delete();
                startDownloadFile();
                Log.d("Path Data_2", "" + downloadFile.getPath());
            }
            Log.d("Path Data_3", "" + downloadFile.getPath());
        } else {
            Log.d("Path Data_4", "" + downloadFile.getPath());
            downloadFile.mkdirs();
            startDownloadFile();
        }
    }

    private void startDownloadFile(){
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator + FILE_PATH + File.separator + nameOfFile);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                MBTilesLayer layer = new ChartTileServiceLayer(file.getPath(),
                        MainActivity.this.getBaseContext());

                mMapView.addLayer(layer);
                layers[0] = layer;
            }
        }
    };
}*/
