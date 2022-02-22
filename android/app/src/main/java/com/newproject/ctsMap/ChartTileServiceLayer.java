/*
 * The MIT License (MIT)
 * Copyright (c) 2016 NOAA
 * 
 * Overzoom solution derived from Will Kamp's post at:
 * https://groups.google.com/forum/?fromgroups=#!topic/osmdroid/VhGVURv8-lQ
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
 */

package com.newproject.ctsMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ChartTileServiceLayer extends MBTilesLayer {
    private String mPath;

    public ChartTileServiceLayer(String path, Context context) {
        super(path, context);
        mPath = path;
    }

    @Override
    public byte[] getTile(int level, int col, int row) throws Exception {

        MapTile pTile = new MapTile(level, col, row);
        byte[] imageBytes = null;

        // For performance, it's important to first make sure that the requested tile's location is within the
        // bounding box of the the tileset.
        if (mLayerEnvelope.isIntersecting(pTile.getEnvelope())) {
            imageBytes = super.getTile(level, col, row);
            try {

                Drawable mapTile = null;
                if (imageBytes != null) {

                    mapTile = new BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes,
                            0, imageBytes.length));

                    // detect transparency in bitmap and merge/stack with a zoomed tile
                    final Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    mapTile.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    mapTile.draw(canvas);

                    if (bitmapHasTransparency(bitmap)) {
                        if (DEBUG) {
                            Log.i(TAG, "getMapTile: Transparency detected in tile, trying stackedZoomTilesTile (" + pTile + ")");
                        }
                        OverZoomResult overZoomResult = underlayTransparency(pTile, bitmap, 1);
                        mapTile = overZoomResult.getDrawable();
                        if (mapTile != null) {
                            Bitmap bitmap2 = ((BitmapDrawable)mapTile).getBitmap();
                            canvas = new Canvas(bitmap2);
                            if (DEBUG) {
                                Paint paint = new Paint();
                                paint.setColor(Color.BLUE);
                                paint.setTextSize(25);
                                canvas.drawLine(0, 0, 0, 255, paint);
                                canvas.drawLine(0, 255, 255, 255, paint);
                                canvas.drawLine(255, 255, 255, 0, paint);
                                canvas.drawLine(255, 0, 0, 0, paint);
                                canvas.drawText(pTile.toString(), 5, 25, paint);
                                canvas.drawText(overZoomResult.getRetries() + " retries", 5, 250, paint);
                            }
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            imageBytes = stream.toByteArray();
                        }

                    } else {
                        return imageBytes;
                    }
                }
                // If the tile request was not in the MBTiles database, there might be a tile at an
                // upper zoom level that can be rescaled for this tile request.
                else {
                    if (DEBUG) {
                        Log.i(TAG, "getMapTile: Tile not found, trying findOverZoomTile (" + pTile + ")");
                    }
                    OverZoomResult overZoomResult = findOverZoomTile(pTile, 1);
                    mapTile = overZoomResult.getDrawable();
                    if (mapTile != null) {
                        Bitmap bitmap = ((BitmapDrawable)mapTile).getBitmap();
                        Canvas canvas = new Canvas(bitmap);
                        if (DEBUG) {
                            Paint paint = new Paint();
                            paint.setColor(Color.RED);
                            paint.setTextSize(25);
                            canvas.drawLine(0, 0, 0, 255, paint);
                            canvas.drawLine(0, 255, 255, 255, paint);
                            canvas.drawLine(255, 255, 255, 0, paint);
                            canvas.drawLine(255, 0, 0, 0, paint);
                            canvas.drawText(pTile.toString(), 5, 25, paint);
                            canvas.drawText(overZoomResult.getRetries() + " retries", 5, 250, paint);
                        }
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        imageBytes = stream.toByteArray();
                    }
                }

            } catch (final Throwable e) {
                Log.e(TAG, "getMapTile: Error loading tile", e);
            }
        } else if (DEBUG) {
            Log.d(TAG, pTile + " is out of bounds for " + mPath);
        }

        return imageBytes != null ? imageBytes : mBlankTile;
    }

    private OverZoomResult underlayTransparency(MapTile pTile, Bitmap bitmap,
                                          int zoomTimes) {
        if (DEBUG) {
            Log.i(TAG, "underlayTransparency: tile = " + pTile);
        }
        final Bitmap newbitmap = Bitmap.createBitmap(
                TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(newbitmap);
        OverZoomResult overZoomResult = findOverZoomTile(pTile, zoomTimes);
        final Drawable bottomDrawable = overZoomResult.getDrawable();
        if (bottomDrawable != null) {
            final Bitmap bottomBmp = ((BitmapDrawable) bottomDrawable)
                    .getBitmap();
            canvas.drawBitmap(bottomBmp, 0f, 0f, null);
            canvas.drawBitmap(bitmap, 0f, 0f, null);
            // canvas.drawPaint(paint);
            return new OverZoomResult(new BitmapDrawable(newbitmap), overZoomResult.getRetries());
        } else {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
            return new OverZoomResult(new BitmapDrawable(newbitmap), overZoomResult.getRetries());
        }
    }

    private OverZoomResult findOverZoomTile(MapTile pTile, int zoomTimes) {
        if (DEBUG) {
            Log.d(TAG, "findOverZoomTile: tile = " + pTile + ", try " + String.valueOf(zoomTimes));
        }
        final int pTileSizePx = TILE_SIZE;
        if (zoomTimes >= MAX_RETRIES) {
            return new OverZoomResult(null, zoomTimes);
        }
        final int pZoomLevel = pTile.getZ();
        final int upperZoomLevel = pZoomLevel - zoomTimes;
        final int mDiff = Math.abs(pZoomLevel - upperZoomLevel);
        final int mTileSize_2 = pTileSizePx >> mDiff;
        final MapTile upperTile = new MapTile(upperZoomLevel,
                pTile.getX() >> mDiff, pTile.getY() >> mDiff);
        if (DEBUG) {
            Log.i(TAG, "findOverZoomTile: upperTile = " + upperTile);
        }
        try {
            final byte[] imageBytes = super.getTile(upperTile.getZ(), upperTile.getX(), upperTile.getY());
            if (imageBytes != null) {
                final Drawable oldDrawable = new BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes,
                        0, imageBytes.length));
                if (oldDrawable != null) {
                    final Rect mSrcRect = new Rect();
                    final Rect mDestRect = new Rect();
                    final int pX = pTile.getX();
                    final int pY = pTile.getY();
                    final int xx = (pX % (1 << mDiff)) * mTileSize_2;
                    final int yy = (pY % (1 << mDiff)) * mTileSize_2;
                    mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
                    mDestRect.set(0, 0, pTileSizePx, pTileSizePx);
                    final Bitmap bitmap = Bitmap.createBitmap(pTileSizePx,
                            pTileSizePx, Bitmap.Config.ARGB_8888);
                    final Canvas canvas = new Canvas(bitmap);
                    final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                    canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);

                    if (bitmapHasTransparency(bitmap)) {
                        if (DEBUG) {
                            Log.d(TAG, "findOverZoomTile: transparency detected in scaled tile: "
                                    + pTile + " upperTile: " + upperTile);
                            Log.d(TAG, "findOverZoomTile: stacking with upper level tile");
                        }
                        return underlayTransparency(pTile, bitmap, zoomTimes + 1);
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "findOverZoomTile: overzoom success scaling tile: "
                                    + pTile + " upperTile: " + upperTile);
                        }
                        return new OverZoomResult(new BitmapDrawable(bitmap), zoomTimes);
                    }
                } else {
                    if (DEBUG) {
                        Log.w(TAG, "findOverZoomTile: upperTile not found: " + upperTile);
                    }

                }
            }

        } catch (final Throwable e) {
            Log.e(TAG, "Error scaling tile", e);
        }

        if (DEBUG) {
            Log.w(TAG, "findOverZoomTile: zoomTimes =  " + zoomTimes + ", maxUpperZoom = " + MAX_RETRIES);
        }
        if (zoomTimes < MAX_RETRIES) {
            OverZoomResult overZoomResult = findOverZoomTile(pTile, zoomTimes + 1);
            return new OverZoomResult(overZoomResult.getDrawable(), overZoomResult.getRetries());
        } else {
            if (DEBUG) {
                Log.w(TAG, "findOverZoomTile: reached maxUpperZoom: " + pTile);
            }
            Log.i(TAG, "findOverZoomTile: reached maxUpperZoom: " + pTile);
        }

        if (DEBUG) {
            Log.w(TAG,
                    "findOverZoomTile: overzoom tile not found: " + pTile + ", try "
                            + String.valueOf(zoomTimes));
        }
        return new OverZoomResult(null, zoomTimes);
    }

    private boolean bitmapHasTransparency(Bitmap bitmap) {
        final int size = TILE_SIZE;
        // we only need to check corner pixels for transparency
        if (bitmap.getPixel(0, 0) == Color.TRANSPARENT)
            return true;
        if (bitmap.getPixel(0, size - 1) == Color.TRANSPARENT)
            return true;
        if (bitmap.getPixel(size - 1, 0) == Color.TRANSPARENT)
            return true;
        if (bitmap.getPixel(size - 1, size - 1) == Color.TRANSPARENT)
            return true;
        return false;
    }

    class OverZoomResult {
        private final Drawable drawable;
        private final int retries;

        public OverZoomResult(Drawable drawable, int retries) {
            this.drawable = drawable;
            this.retries = retries;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public int getRetries() {
            return retries;
        }
    }

    public byte[] getGrid(int z, int x, int y)
            throws Exception
    {
        byte[] gridBytes = null;
        Cursor cursor = this.mapDb.rawQuery("SELECT grid FROM grids WHERE zoom_level = " + Integer.toString(z) + " AND tile_column = " + Integer.toString(x) + " AND tile_row = " + Integer.toString((1 << z) - 1 - y), null);
        if (cursor != null && cursor.moveToFirst()) {
            gridBytes =  cursor.getBlob(0);
        }
        return gridBytes;
    }

    public Map<String, String> getGridData(int z, int x, int y)
            throws Exception
    {
        HashMap hashMap = new HashMap();
        Cursor cursor = this.mapDb.rawQuery("SELECT key_name, key_json FROM grid_data WHERE zoom_level = " + Integer.toString(z) + " AND tile_column = " + Integer.toString(x) + " AND tile_row = " + Integer.toString((1 << z) - 1 - y), null);
        while (cursor.moveToNext()) {
            hashMap.put(cursor.getString(0), cursor.getString(1));

        }
        cursor.close();
        return hashMap;
    }

    private static final String TAG = "ChartTileServiceLayer";
    private static final boolean DEBUG = false;
    private static final int MAX_RETRIES = 3;
    private static final int TILE_SIZE = 256;
}
