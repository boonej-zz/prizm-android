package co.higheraltitude.prizm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import co.higheraltitude.prizm.models.User;

public class ImageCropActivity extends AppCompatActivity {

    public static final String EXTRA_ASPECT_X = "extra_aspect_x";
    public static final String EXTRA_ASPECT_Y = "extra_aspect_y";
    private static final int REQUEST_EXTERNAL_STORAGE = 101;
//    public static final String EXTRA_URI= "extra_uri";

    private Uri mInputUri;
    private int mAspectX;
    private int mAspectY;

    private ImageCropView mImageCropView;
    private ImageButton mDoneButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PrizmBlack);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_crop);
        loadIntentData();
        configureToolbar();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        } else {
            loadViews();
        }
    }

    protected void configureToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        mDoneButton = (ImageButton)findViewById(R.id.action_done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mImageCropView.isChangingScale()) {
                    Bitmap b = mImageCropView.getCroppedImage();
                    File file = bitmapConvertToFile(b);
                    Uri fileUri = Uri.fromFile(file);
                    finishUp(fileUri);
                }
            }
        });
        setTitle("");
    }

    private void finishUp(Uri fileUri) {
        Intent intent = new Intent();
        intent.setData(fileUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    public File bitmapConvertToFile(Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        File bitmapFile = null;
        try {
            bitmapFile= new File(getCacheDir(), UUID.randomUUID().toString() + ".jpg");



            fileOutputStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//            MediaScannerConnection.scanFile(this, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
//                @Override
//                public void onMediaScannerConnected() {
//
//                }
//
//                @Override
//                public void onScanCompleted(String path, Uri uri) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(getApplicationContext(), "file saved", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                }
            }
        }

        return bitmapFile;
    }

    protected void loadIntentData() {
        Intent intent = getIntent();
        mInputUri = intent.getData();
        mAspectX = intent.getIntExtra(EXTRA_ASPECT_X, 1);
        mAspectY = intent.getIntExtra(EXTRA_ASPECT_Y, 1);
    }

    protected void loadViews() {
        mImageCropView = (ImageCropView)findViewById(R.id.image);
        mImageCropView.setImageFilePath(mInputUri.getPath());
        mImageCropView.setAspectRatio(mAspectX, mAspectY);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
       loadViews();
    }

}
