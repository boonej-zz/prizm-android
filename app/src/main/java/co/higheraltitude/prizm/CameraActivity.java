package co.higheraltitude.prizm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import co.higheraltitude.prizm.custom.CameraPreview;
import co.higheraltitude.prizm.custom.ToggleImageButton;

public class CameraActivity extends AppCompatActivity {

    public static final int ASPECT_SQUARE = 0;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Camera mCamera;
    private CameraPreview mPreview;
    private int mAspect = ASPECT_SQUARE;

    private ToggleImageButton mFlashButton;
    private ToggleImageButton mCameraButton;

    private boolean mCanFlash = false;
    private boolean mCanFlip = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        loadViews();
        detectFeatures();
    }

    protected void startCamera(int cameraId) {
        final FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.removeAllViews();
        mCamera = getCameraInstance(cameraId);

        if (mCamera == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        mPreview = new CameraPreview(this, mCamera);

        if (mAspect == ASPECT_SQUARE) {
            final View overlay = findViewById(R.id.overlay);
            overlay.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
                    params1.setMargins(params1.leftMargin, preview.getWidth(), params1.rightMargin, params1.bottomMargin);
                    overlay.setLayoutParams(params1);
                    overlay.postInvalidate();
                }
            });

        }

        preview.addView(mPreview);
        checkFlashFeature();
    }

    private void loadViews() {
        mFlashButton = (ToggleImageButton)findViewById(R.id.flash_button);
        mCameraButton = (ToggleImageButton)findViewById(R.id.camera_button);
        mCameraButton.setSelected(true);
    }

    protected Camera getCameraInstance(int camId){
        Camera c = null;
        try {
            c = Camera.open(camId); // attempt to get a Camera instance
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_no_camera), Toast.LENGTH_SHORT).show();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = new File(getCacheDir(), UUID.randomUUID() + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getHeight(), bitmap.getHeight(), matrix, true);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                resized.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("DEBUG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("DEBUG", "Error accessing file: " + e.getMessage());
            }

            Intent intent = new Intent();
            intent.setData(Uri.fromFile(pictureFile));
            setResult(RESULT_OK, intent);
            finish();
        }
    };


    private void detectFeatures() {
        if (Camera.getNumberOfCameras() > 0) {
            startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCanFlip = Camera.getNumberOfCameras() > 1;
            mCameraButton.setSelected(mCanFlip);
        } else {
            mCanFlip = false;
        }
    }

    private void checkFlashFeature(){
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                mCanFlash = true;
                mFlashButton.setSelected(true);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else {
                mCanFlash = false;
                mFlashButton.setSelected(false);
            }
        }
    }

    public void captureClicked(View view) {
        mCamera.takePicture(null, null, mPicture);
    }

    public void flashButtonClicked(View view) {
        if (mCanFlash) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(view.isSelected() ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(parameters);
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_flash_unsupported), Toast.LENGTH_SHORT).show();
        }
    }

    public void cameraButtonClicked(View view) {
        if (mCanFlip) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mPreview.release();
            mPreview = null;
            startCamera(view.isSelected() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_camera_mode_unsupported), Toast.LENGTH_SHORT).show();
        }

    }

    public void hdrButtonClicked(View view) {
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_hdr_mode_unsupported), Toast.LENGTH_SHORT).show();
    }

    public void gridButtonClicked(View view) {
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_grid_mode_unsupported), Toast.LENGTH_SHORT).show();
    }

    public void timerButtonClicked(View view) {
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_timer_mode_unsupported), Toast.LENGTH_SHORT).show();
    }
}