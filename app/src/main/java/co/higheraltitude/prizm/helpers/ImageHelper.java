package co.higheraltitude.prizm.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/15/15.
 */
public class ImageHelper {
    private static ImageHelper instance = null;
    private static Context mContext;
    private static CognitoCredentialsProvider mCredentialsProvider;
    private static AmazonS3Client s3;
    private static TransferUtility mTransferUtility;
    private static Object [] stack = {};


    private ImageHelper() {

    }

    public static ImageHelper getInstance() {
        if (instance == null) {
            instance = new ImageHelper();
        }
        return instance;
    }

    public static void registerContext(Context context) {
        if (instance == null) {
            instance = new ImageHelper();
        }
        mContext = context;
//        mCredentialsProvider = new CognitoCachingCredentialsProvider(
//                mContext,
//                "us-east-1:a35e6d9a-6cac-4625-93d1-7a2bdb7e2bbf",
//                Regions.US_EAST_1 // Region
//        );
//        ClientConfiguration configuration = new ClientConfiguration();
//        configuration.setConnectionTimeout(50000);
//        configuration.setMaxConnections(500);
//        configuration.setSocketTimeout(50000);
//        configuration.setMaxErrorRetry(10);
//        s3 = new AmazonS3Client(mCredentialsProvider, configuration);
//        s3.setRegion(Region.getRegion(Regions.US_EAST_1));

//        mTransferUtility = new TransferUtility(s3, mContext);
    }

    private static AmazonS3Client s3Client() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(50000);
        configuration.setMaxConnections(500);
        configuration.setSocketTimeout(50000);
        configuration.setMaxErrorRetry(10);

        String key = mContext.getString(R.string.aws_access_key);
        String secret = mContext.getString(R.string.aws_secret);
        AWSCredentials credentials = new BasicAWSCredentials(key, secret);
        AmazonS3Client client = new AmazonS3Client(credentials, configuration);
        client.setRegion(Region.getRegion(Regions.US_EAST_1));
        return client;
    }


    public Bitmap bitmapFromUri(Uri uri) {
        Bitmap bmp = null;
        try {
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            bmp = BitmapFactory.decodeStream(is);
            ExifInterface exif = new ExifInterface(uri.toString());
            Matrix m = new Matrix();
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    m.preRotate(270);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    m.preRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    m.preRotate(180);
                    break;
            }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return bmp;
        }
    }

    public Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }

    public Bitmap cropCoverPhoto(Bitmap bitmap) {
        int width = bitmap.getWidth();
        double ratio = 640.00/width;
        int height = (int)(ratio * (double)bitmap.getHeight());
        int paddingTop = (height - 376)/2;
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, 640, height);
        resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, paddingTop, 640, 376);
        return resizedBitmap;
    }

//    private void resizeAndUpload(Bitmap bitmap, String rootPath, Boolean crop) {
//        int [] resizeArray = new int[]{2, 4};
//        if (crop) {
//            bitmap = cropToSquare(bitmap);
//        }
//        uploadImage(bitmap, rootPath + ".jpg");
//        for (int i = 0; i != resizeArray.length; ++i) {
//            int width = bitmap.getWidth()/resizeArray[i];
//            int height = bitmap.getHeight()/resizeArray[i];
//            Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//            uploadImage(resized, rootPath + "_" + resizeArray[i] + ".jpg");
//        }

//    }

//    private static Handler uploadHandler = new Handler(){
//        @Override
//        public void handleMessage(Peep message) {
//            File out = (File)message.obj;
//
//            TransferUtility transferUtility = new TransferUtility(s3Client, mContext);
//            TransferObserver observer = transferUtility.upload("higheraltitude.prism", path, out);
//            observer.setTransferListener(new TransferListener() {
//                @Override
//                public void onStateChanged(int id, TransferState state) {
//                    Log.d("DEBUG", state.toString());
//                }
//
//                @Override
//                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                    Log.d("DEBUG", "Bytes transferred: " + bytesCurrent + "/" + bytesTotal);
//                }
//
//                @Override
//                public void onError(int id, Exception ex) {
//                    ex.printStackTrace();
//                }
//            });
//        }
//    };

//    private void uploadImage(final Bitmap image, final String path) {
//        if (s3Client == null) {
//            String key = mContext.getString(R.string.aws_access_key);
//            String secret = mContext.getString(R.string.aws_secret);
//            AWSCredentials credentials = new BasicAWSCredentials(key, secret);
//            s3Client = new AmazonS3Client(credentials);
//            Region region = Region.getRegion(Regions.DEFAULT_REGION);
//            s3Client.setRegion(region);
//        }
//
//        Thread request = new Thread() {
//            @Override
//            public void run() {
//                final String filePath = path;
//                try {
//                    File out = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
//                    FileOutputStream os = new FileOutputStream(out);
//                    image.compress(Bitmap.CompressFormat.JPEG, 100, os);
//                    Peep message = uploadHandler.obtainMessage(1, out);
//                    uploadHandler.sendMessage(message);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        };
//
//        request.start();
//    }

//    public String uploadToS3(Bitmap image, Boolean crop) {
//
//        String rootPath = "";
//        User currentUser = User.getCurrentUser();
//        if (currentUser != null) {
//            rootPath = "" + currentUser.uniqueID + "/";
//        }
//        rootPath = rootPath + "000stinkbomb";
//
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                mContext,
//                "us-east-1:a35e6d9a-6cac-4625-93d1-7a2bdb7e2bbf", // Identity Pool ID
//                Regions.US_EAST_1 // Region
//        );
//
//        String key = mContext.getString(R.string.aws_access_key);
//        String secret = mContext.getString(R.string.aws_secret);
//        AWSCredentials credentials = new BasicAWSCredentials(key, secret);
//        AmazonS3 s3 = new AmazonS3Client(credentials);
//        Region region = Region.getRegion(Regions.DEFAULT_REGION);
//        s3.setRegion(region);
//
//        int width = image.getWidth();
//        int height = image.getHeight();
//        if (width > 1600) {
//            double ratio = 1000.00/(double)width;
//            height = (int)((double)height * ratio);
//            image = Bitmap.createBitmap(image, 0, 0, 1000, height);
//        }
//        String path = null;
//        try {
//            File out = File.createTempFile("image", "png");
//            FileOutputStream os = new FileOutputStream(out);
//            image.compress(Bitmap.CompressFormat.PNG, 100, os);
//            os.close();
//            TransferUtility transferUtility = new TransferUtility(s3, mContext);
//            TransferObserver observer = transferUtility.upload("higheraltitude.prism", rootPath + ".png", out);
//
//            path = "https://s3.amazonaws.com/higheraltitude.prism/" + rootPath + ".jpg";
//            Log.d("DEBUG", path);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return path;
//    }

    private void saveTempFile(final Bitmap image, final Handler handler) {
        Thread fileProcess = new Thread(){
            @Override
            public void run() {
                ByteArrayOutputStream os = null;
//                File file = null;
                S3Object object = null;
                try {
//                    file = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
                    os = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 80, os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    object = new S3Object();

                    object.setObjectContent(is);
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentType("image/jpeg");

                    object.setObjectMetadata(metadata);
                    object.setBucketName("higheraltitude.prism.test");
                    object.setKey(UUID.randomUUID().toString() + ".jpg");

                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        os.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Message message = handler.obtainMessage(1, object);
                    handler.sendMessage(message);
                }
            }
        };
        fileProcess.start();
    }

    private static class UploadHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            final S3Object object = (S3Object)message.obj;
//            Thread uploadThread = new Thread(new Runnable() {
//                @Override
//                public void run() {

                    AmazonS3Client client = ImageHelper.s3Client();
//                    client.
                    TransferUtility transferUtility = new TransferUtility(client, mContext);
//                    TransferObserver observer = transferUtility.upload("higheraltitude.prizm.test", "00test.jpg", object);
//                    observer.setTransferListener(new
//                        TransferListener() {
//                            @Override
//                            public void onStateChanged ( int id, TransferState state){
//                                Log.d("DEBUG", "Transfer state changed");
//                            }
//
//                            @Override
//                            public void onProgressChanged ( int id, long bytesCurrent, long bytesTotal){
//                                Log.d("DEBUG", bytesCurrent + "/" + bytesTotal);
//                            }
//
//                            @Override
//                            public void onError ( int id, Exception ex){
//                                ex.printStackTrace();
//                            }
//                        });
//                    observer.cleanTransferListener();

//                    Log.d("DEBUG", "State: " + observer.getState());

//                }

//            });
//            uploadThread.start();
        }
    }

    public String uploadImage(final Bitmap image) {
        return uploadImage(image, null);
    }

    public String uploadImage(Bitmap bmp, final Handler handler) {
//        saveTempFile(image, new UploadHandler());

//                File file = null;

//                    file = File.createTempFile(UUID.randomUUID().toString(), ".jpg");

        if (bmp.getWidth() > 1000) {
            double ratio = 1000.00 / (double) bmp.getWidth();
            int height = (int) ((double) bmp.getHeight() * ratio);
            bmp = Bitmap.createBitmap(bmp, 0, 0, 1000, height);
        }
        final Bitmap image = bmp;
        final String path = UUID.randomUUID() + ".jpg";
        String finalPath = "https://s3.amazonaws.com/haandroid/"+ path;
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String fp = path;
                ByteArrayOutputStream os;
                try {
                    os = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 80, os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentType("image/jpeg");
                    metadata.setContentLength(os.size());

                    PutObjectRequest request = new PutObjectRequest("haandroid", fp, is, metadata);
                    AmazonS3Client client = s3Client();
                    client.putObject(request.withCannedAcl(CannedAccessControlList.PublicRead));
                    try {
                        os.close();
                        is.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            }
        });


        backgroundThread.start();
        return finalPath;

    }


}
