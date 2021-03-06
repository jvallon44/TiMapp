package com.timappweb.timapp;

import android.app.Application;
import android.graphics.Bitmap;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.ResourceHelper;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Stephane on 17/08/2016.
 */
public class ResizePictureTest extends ApplicationTestCase<Application> {

    private static final String TAG = "UploadPictureTest";

    public ResizePictureTest() {
        super(Application.class);
    }

    @Before
    public void setUp(){

    }

    @Test
    public void testResizeFile() throws IOException {
        String resFile = "res/raw/big_light.jpg";
        File file =  ResourceHelper.getFile(this, resFile);
        assertNotNull(file);
        assertTrue("Make sure file '"+resFile+"' exists", file.exists());
        assertTrue("Make sure file '"+resFile+"' is a valid file", file.isFile());
        File newFile = PictureUtility.resize(file, 1000, 1000);

        Log.i(TAG, "File size after resizing: " + file.length() / (1024*1024) + "MB");
        assertTrue("Resizing the file should make the size smaller.", file.length() > newFile.length());
    }

    @Test
    public void testResizeBitmap() {
        // Both
        Bitmap bitmap = Bitmap.createBitmap(5000, 4000, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 400, 300);
        assertTrue(bitmap.getWidth() == 375);
        assertTrue(bitmap.getHeight() == 300);

        // Only height
        bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 400, 300);
        assertTrue(bitmap.getWidth() == 300);
        assertTrue(bitmap.getHeight() == 300);

        // Only width
        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 300, 200);
        assertTrue(bitmap.getWidth() == 300);
        assertTrue(bitmap.getHeight() == 150);
    }
}
