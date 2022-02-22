/*
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

package com.newproject.ctsMap;

import android.util.Log;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.newproject.utils.UtfGridHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to hole the x any y values (relative to the corner) of a tile point.
 */
public class MapTilePoint
{
    public static Map<Double, Integer> mScaleMap = new HashMap();
    private MBTilesLayer[] layers;
    private MapTile mMapTile;
    private int mX;
    private int mY;

    static
    {
        int i = 0;
        while (i < 20)
        {
            double d = 5.54678932E8D / Math.pow(2.0D, i);
            mScaleMap.put(Double.valueOf(d), Integer.valueOf(i + 1));
            Log.i("MapTile", "scale = " + d + ", zoom = " + i);
            i += 1;
        }
    }

    /**
     * @param screenPoint
     * @param scale is used to calculate the zoom level
     * @param layers
     */
    public MapTilePoint(Point screenPoint, double scale, MBTilesLayer[] layers)
    {
        this.layers = layers;
        Point geoPoint = (Point)GeometryEngine.project((Point)GeometryEngine.normalizeCentralMeridian(screenPoint, SpatialReference.create(3857)), SpatialReference.create(3857), SpatialReference.create(4326));
        if (geoPoint != null)
        {
            // Get the lat/lon of the point clicked
            double lon = geoPoint.getX();
            double lat = geoPoint.getY();
            Log.i("MapTile", lat + " " + lon);
            Log.i("MapTile", "scale = " + scale);

            if (mScaleMap.containsKey(Double.valueOf(scale)))
            {
                // Get the zxy tile address
                int zoom = ((Integer)mScaleMap.get(Double.valueOf(scale))).intValue();
                int x_tile = (int)Math.floor((180.0D + lon) / 360.0D * (1 << zoom));
                int y_tile = (int)Math.floor((1.0D - Math.log(Math.tan(Math.toRadians(lat)) + 1.0D / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0D * (1 << zoom));

                if ((x_tile < 0) || (x_tile >= 1 << zoom) || (y_tile < 0) || (y_tile >= 1 << zoom)) {
                    throw new RuntimeException("X/Y out of range.");
                }

                // Get the global x/y coordinate
                double x = (180.0D + lon) / 360.0D * (1 << zoom);
                double y = (1.0D - Math.log(Math.tan(Math.toRadians(lat)) + 1.0D / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0D * (1 << zoom);

                // Get the relative x/y coordinate
                this.mX = ((int)((x - x_tile) * 256.0D));
                this.mY = ((int)((y - y_tile) * 256.0D));
                Log.i("MapTile", "Point X = " + geoPoint.getX() + " " + this.mX);
                Log.i("MapTile", "Point Y = " + geoPoint.getY() + " " + this.mY);
                this.mMapTile = new MapTile(zoom, x_tile, y_tile);
            }
            else {
                throw new RuntimeException("Scale " + scale + " not in scalemap: " + mScaleMap);
            }
        }
    }

    public String getGridJson() throws Exception {
        String json = null;
        for (int i = 0; i < layers.length; i++) {

            if (this.layers[i] != null) {

                Log.i("MapTilePoint", "Trying grid for layer " + this.layers[i].mapDb.getPath() + "...");
                byte[] gridBytes = ((ChartTileServiceLayer) this.layers[i]).getGrid(this.mMapTile.getZ(), this.mMapTile.getX(), this.mMapTile.getY());

                if (gridBytes != null) {

                    Log.i("MapTilePoint", "Got grid, decoding grid...");
                    UtfGridHelper.MBTileUTFGrid utdGrid = UtfGridHelper.decodeUtfGrid(gridBytes);

                    Log.i("MapTilePoint", "Grid decoded, getting grid data...");
                    Map map = ((ChartTileServiceLayer) this.layers[i]).getGridData(this.mMapTile.getZ(), this.mMapTile.getX(), this.mMapTile.getY());

                    Log.i("MapTilePoint", "Got grid data, getting json key. Grid data = " + map);
                    i = UtfGridHelper.utfGridCode(256, this.mX, this.mY, utdGrid, 4);

                    Log.i("MapTilePoint", "key = " + i);
                    json = (String) map.get(Integer.toString(i));

                    Log.i("MapTilePoint", "Found json: " + json);
                    break;
                }
            }
        }
        return json;
    }

    public MapTile getMapTile()
    {
        return this.mMapTile;
    }

    public int getX()
    {
        return this.mX;
    }

    public int getY()
    {
        return this.mY;
    }
}
