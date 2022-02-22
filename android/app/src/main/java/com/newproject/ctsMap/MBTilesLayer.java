package com.newproject.ctsMap;

/*
 * Copyright 2012 ESRI
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
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.esri.android.map.TiledServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import java.io.ByteArrayOutputStream;

/**
 * The MBTilesLayer class allows you to work with a MBTiles stored in a SQLite
 * database.
 */
public class MBTilesLayer extends TiledServiceLayer {

    private int mLevels = 0;
    protected byte[] mBlankTile;
    protected Envelope mLayerEnvelope;
    public TileInfo mTileInfo;
    protected SQLiteDatabase mapDb;


    /**
     * The constructor to instantiate MBTiles from a path on device
     *
     * @param path path is expected to be of the form /sdcard/path/package.mbtiles
     */
    public MBTilesLayer(String path, Context context) {
        super(path);

        final Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        mBlankTile = stream.toByteArray();

        try {
            mapDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException ex) {
            Log.e(this.getName(), ex.getMessage());
            throw (ex);
        }

        // Default TMS bounds = bounds of Web Mercator projection
        Envelope envWGS = new Envelope(-180.0, -85.0511, 180.0, 85.0511);

        // See if the MBTiles DB defines their own Bounds in the metadata table
        Cursor bounds = mapDb.rawQuery("SELECT value FROM metadata WHERE name = 'bounds'", null);
        if (bounds.moveToFirst()) {
            String bs = bounds.getString(0);
            String[] ba = bs.split(",", 4);
            if (ba.length == 4) {
                double leftLon = Double.parseDouble(ba[0]);
                double topLat = Double.parseDouble(ba[3]);
                double rightLon = Double.parseDouble(ba[2]);
                double bottomLat = Double.parseDouble(ba[1]);

                // TODO: Figure out why this doesn't work
                //envWGS = new Envelope(leftLon, bottomLat, rightLon, topLat);

                // Save the envelope and intersect it with the tile envelop before querying
                // the database as when a tile is requested.
                mLayerEnvelope = new Envelope(leftLon, bottomLat, rightLon, topLat);
                Log.i(TAG, mLayerEnvelope.toString());

            }
        }
        bounds.close();

        Envelope envWeb = (Envelope) GeometryEngine.project(envWGS, SpatialReference.create(4326),
                SpatialReference.create(3857));

        Point origin = envWeb.getUpperLeft();

        Cursor maxLevelCur = mapDb.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles", null);
        if (maxLevelCur.moveToFirst()) {
            mLevels = maxLevelCur.getInt(0);
        }

        Log.i("TAG", "Max levels = " + Integer.toString(mLevels));

        double[] resolution = new double[mLevels];
        double[] scale = new double[mLevels];
        for (int i = 0; i < mLevels; i++) {
            // see the TMS spec for derivation of the level 0 scale and resolution
            // For each level the resolution (in meters per pixel) doubles
            resolution[i] = 156543.032 / Math.pow(2, i);
            // Level 0 scale is 1:554,678,932. Each level doubles this.
            scale[i] = 554678932 / Math.pow(2, i);
        }

        /*
         * Note, the constructor must set the following values or we won't send the
         * status change events to listeners and the tiles will not be fetched
         *
         * Origin is Top Left (web Mercator) , the rest are defined by the TMS
         * Global-mercator spec (scales, resolution, 96dpi 256x256 pixel tiles) See:
         * http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#global-mercator
         */
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        Log.i(TAG, "Density = " + metrics.densityDpi);

        mTileInfo = new TileInfo(origin, scale, resolution, mLevels, metrics.densityDpi, 256, 256);

        this.setTileInfo(mTileInfo);
        this.setFullExtent(envWeb);
        this.setDefaultSpatialReference(SpatialReference.create(3857));
        this.setInitialExtent(envWeb);

        this.initLayer();

    }

    @Override
    protected byte[] getTile(int level, int col, int row) throws Exception {

        // need to flip origin
        int nRows = (1<<level);  //Num rows = 2^level
        int tmsRow = nRows-1-row;

        byte[] bytes = null;

        Cursor imageCur = mapDb.rawQuery("SELECT tile_data FROM tiles WHERE zoom_level = " + Integer.toString(level)
                + " AND tile_column = " + Integer.toString(col) + " AND tile_row = " + Integer.toString(tmsRow), null);

        if (imageCur.moveToFirst()) {

            //Log.i(TAG, "Got image for " + mapDb + " /" + level + "/" + col + "/" + tmsRow);

            bytes = imageCur.getBlob(0);
        }
        else {

            //Log.i(TAG, "No image for " + mapDb + " /" + level + "/" + col + "/" + tmsRow);

            // For missing tiles, subclass with check for null, look for upper zoom level tile
            // and scale.
            bytes = null;
        }

        imageCur.close();

        return bytes;
    }

    private static final String TAG = "MBTilesLayer";

}