package riaza.puravida.com.riaza;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TakePictureActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "takepicture";

    private ImageView mImageView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    static private Camera mCamera;

    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean take = getIntent().getExtras().getBoolean("take");
        if( take ) {
            mImageView = (ImageView) findViewById(R.id.imageView);
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
        }else{
            AlarmReceiver.cancelSchedule(this);
            finish();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
            Camera.PictureCallback mCall = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "Riaza");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Imagen capturada automaticamente");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                    Uri uriTarget = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    OutputStream imageFileOS;
                    try {
                        imageFileOS = getContentResolver().openOutputStream(uriTarget);
                        imageFileOS.write(data);
                        imageFileOS.flush();
                        imageFileOS.close();

                        Toast.makeText(TakePictureActivity.this,
                                "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //mCamera.startPreview();

                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    mImageView.setImageBitmap(bmp);

                    AlarmReceiver.scheduleNext(TakePictureActivity.this);
                    TakePictureActivity.this.finish();
                }
            };
            mCamera.takePicture(null, null, mCall);
    }

    int getFrontCameraId() {
        CameraInfo ci = new CameraInfo();
        for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) return i;
        }
        return -1; // No front-facing camera found
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        int index = getFrontCameraId();
        if (index == -1){
            Toast.makeText(getApplicationContext(), "No front camera", Toast.LENGTH_LONG).show();
        }
        else
        {
            mCamera = Camera.open(index);
            mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                Toast.makeText(getApplicationContext(), "With front camera", Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Error "+exception.getMessage(), Toast.LENGTH_LONG).show();
                mCamera.release();
                mCamera = null;
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
