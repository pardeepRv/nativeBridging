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
 * Portions are ported from https://svn.osgeo.org/gdal/trunk/gdal/swig/python/scripts/gdal2tiles.py
 * 
 * Copyright (c) 2008, Klokan Petr Pridal
 * Copyright (c) 2010-2013, Even Rouault <even dot rouault at mines-paris dot org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.newproject.ctsMap;

import android.util.Log;

import com.esri.core.geometry.Envelope;

public class MapTile {
    private int z;
    private int x;
    private int y;
    private String path;
    private Envelope envelope;

    // For lat/lng bounds calculation
    private double tileSize = DEFAULT_TILE_SIZE;
    private double originShift = 2 * Math.PI * RADIUS_EARTH_METERS / 2.0;
    private double initialResolution = 2 * Math.PI * RADIUS_EARTH_METERS / tileSize;

    public MapTile(int z, int x, int y) {
        this.z = z;
        this.x = x;
        this.y = y;
        this.path = (new StringBuilder()).append('/').append(z).append('/').append(x).append('/').append(y).toString();

        // Returns bounds of the given tile in EPSG:900913 coordinates
        double[] bounds = TileBounds(this.getX(), this.getY(), this.getZ());
        double[] minLatLon = MetersToLatLon(bounds[0], bounds[3]);
        double[] maxLatLon = MetersToLatLon(bounds[2], bounds[1]);

        if (maxLatLon[1] > 0) {
            maxLatLon[1] = -(180 + Math.abs(180 - maxLatLon[1]));
        }

        if (minLatLon[1] > 0) {
            minLatLon[1] = -(180 + Math.abs(180 - minLatLon[1]));
        }

        envelope = new Envelope(maxLatLon[1], maxLatLon[0], minLatLon[1], minLatLon[0]);

        if (DEBUG) {
            Log.d(TAG, path + " bounds = " + envelope.toString());
        }

    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return path;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    private double[] TileBounds(int tx, int ty, int zoom) {
        // Returns bounds of the given tile in EPSG:900913 coordinates
        double[] wn = PixelsToMeters(tx * tileSize, ty * tileSize, zoom);
        double[] es = PixelsToMeters((tx + 1) * tileSize, (ty + 1) * tileSize, zoom);
        return new double[]{wn[0], wn[1], es[0], es[1]};
    }

    private double[] PixelsToMeters(double px, double py, double zoom) {
        // Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
        double res = Resolution(zoom);
        double mx = px * res - originShift;
        double my = py * res - originShift;

        return new double[]{mx, my};
    }

    private double[] MetersToLatLon(double mx, double my) {
        // Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum
        double lon = (mx / originShift) * 180.0;
        double lat = (my / originShift) * 180.0;
        lat = -180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);

        return new double[]{lat, lon};
    }

    private double Resolution(double zoom) {
        // Resolution (meters/pixel) for given zoom level (measured at Equator)
        return initialResolution / Math.pow(2, zoom);
    }

    public static final int DEFAULT_TILE_SIZE = 256;
    public static final int RADIUS_EARTH_METERS = 6378137;
    public static final String TAG = "MapTile";
    private static final boolean DEBUG = false;

}
