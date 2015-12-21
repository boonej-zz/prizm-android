package co.higheraltitude.prizm.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.JsonToken;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONTokener;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import co.higheraltitude.prizm.network.PrizmAPIService;


/**
 * Created by boonej on 10/26/15.
 */
public class PrizmDiskCache {
    private static Context mContext;
    private static PrizmDiskCache mInstance;
    private SharedPreferences preferences;

    private static final String CACHE_SUBDIR = "data";
    private static final String SHARED_PREFS_NAME = "PrizmPrefs";


    public PrizmDiskCache(Context context) {
        preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mInstance = this;
        mContext = context;
    }

    public static PrizmDiskCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PrizmDiskCache(context);
        }
        return mInstance;
    }

    public void saveObject(String path, String text) {
        new ObjectWriterTask().execute(path, text);
    }


    public void readObject(String path, CacheRequestDelegate delegate) {
        new ObjectReaderTask().execute(path, delegate);
    }

    public void loadImage(String path, CacheRequestDelegate delegate) {
        Bitmap img = loadCachedImage(path);
        if (img == null) {
            new BitmapReaderTask().execute(path, delegate);
        } else {
            delegate.cached(path, img);
        }

    }

    public void loadBestImage(String path, CacheRequestDelegate delegate) {
        Bitmap img = loadCachedImage(path);
        if (img == null) {
            new BestAvailableTask().execute(path, delegate);
        } else {
            delegate.cached(path, img);
        }
    }

    public Bitmap loadCachedImage(String path) {
        String key = AeSimpleSHA1.SHA1(path);
        Bitmap bitmap = null;
        if (key != null) {
            File file = new File(mContext.getCacheDir(), key);
            if (file.exists()) {
                byte[] fileData = new byte[(int) file.length()];
                try {
                    DataInputStream is = new DataInputStream(new FileInputStream(file));
                    is.readFully(fileData);
                    is.close();
                    bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public void storeString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String readString(String key) {
        return preferences.getString(key, "");
    }

    public void storeInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int readInt(String key) {
        return preferences.getInt(key, -1);
    }

    public void clearValue(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }


    private static class StringWriterTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String path = params[0];
            String content = params[1];
            String key = AeSimpleSHA1.SHA1(path);

            File file = new File(mContext.getFilesDir(), key);
            Boolean fileExists = true;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception ex) {
                    fileExists = false;
                    ex.printStackTrace();
                }
            }
            if (fileExists) {
                try {
                    FileWriter fileWriter = new FileWriter(file, false);
                    fileWriter.write(content);
                    fileWriter.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }
    }

    private static class ObjectWriterTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            String path = (String)params[0];
            Object content = params[1];
            String key = AeSimpleSHA1.SHA1(path);

            File file = new File(mContext.getFilesDir(), key);
            Gson gson = new Gson();
            String output = gson.toJson(content);
            Boolean fileExists = true;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception ex) {
                    fileExists = false;
                    ex.printStackTrace();
                }
            }
            if (fileExists) {
                try {
                    FileWriter fileWriter = new FileWriter(file, false);
                    fileWriter.write(output);
                    fileWriter.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }
    }

    private static class ObjectReaderTask extends AsyncTask<Object, Void, Object> {
        private String mPath;
        private CacheRequestDelegate mDelegate;
        protected Object doInBackground(Object... params) {
            String mPath = (String)params[0];
            mDelegate= (CacheRequestDelegate)params[1];
            String key = AeSimpleSHA1.SHA1(mPath);
            File file = new File(mContext.getFilesDir(), key);

            String value = null;
            if (file.exists()) {
                try {
                    BufferedReader inputReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file))
                    );
                    StringBuffer stringBuffer = new StringBuffer();
                    String currentLine;
                    while ((currentLine = inputReader.readLine()) != null) {
                        stringBuffer.append(currentLine + "\n");
                    }
                    value = stringBuffer.toString();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Object json = null;
            Gson gson = new Gson();
            if (value != null) {
                try {
                    json = new JSONTokener(value).nextValue();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return json;
        }

        @Override
        protected void onPostExecute(Object result) {
            mDelegate.cached(mPath, result);
        }
    }

    private static class BestAvailableTask extends AsyncTask<Object, Void, Bitmap> {
        private CacheRequestDelegate mDelegate;
        private String mPath;
        private String mBestPath;

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            mPath = (String)params[0];
            mBestPath = PrizmDiskCache.getInstance(null).bestAvailableImage(mPath);
            String key = AeSimpleSHA1.SHA1(mBestPath);
            mDelegate = (CacheRequestDelegate)params[1];
            if (key != null) {
                File file = new File(mContext.getCacheDir(), key);
                if (file.exists()) {
//                    byte[] fileData = new byte[(int) file.length()];
                    try {
                        DataInputStream is = new DataInputStream(new FileInputStream(file));

                        bitmap = BitmapFactory.decodeStream(is);
                        Log.d("DEBUG", "Loading image from cache");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    InputStream is = null;
                    Boolean validUrl = true;
                    try {
                        is = (InputStream) (new URL(mBestPath).getContent());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        validUrl = false;
                    }
                    if (validUrl && is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        if (bitmap.getWidth() > 1000) {
                            double ratio = 1000.00 / (double) bitmap.getWidth();
                            int height = (int) ((double) bitmap.getHeight() * ratio);
                            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, height, false);
                        }
                        try {
                            file.createNewFile();
                            FileOutputStream os = new FileOutputStream(file);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            os.write(stream.toByteArray());
                            os.close();
                            stream.close();
                            Log.d("DEBUG", "Downloaded new image.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mDelegate.cached(mPath, bitmap);
        }
    }

    private static class BitmapReaderTask extends AsyncTask<Object, Void, Bitmap> {
        private CacheRequestDelegate mDelegate;
        private String mPath;

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            mPath = (String)params[0];
            String key = AeSimpleSHA1.SHA1(mPath);
            mDelegate = (CacheRequestDelegate)params[1];
            if (key != null) {
                File file = new File(mContext.getCacheDir(), key);
                if (file.exists()) {
                    byte[] fileData = new byte[(int) file.length()];
                    try {
                        DataInputStream is = new DataInputStream(new FileInputStream(file));
                        is.readFully(fileData);
                        is.close();
                        bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
                        Log.d("DEBUG", "Loading image from cache");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    InputStream is = null;
                    Boolean validUrl = true;
                    try {
                        is = (InputStream) (new URL(mPath).getContent());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        validUrl = false;
                    }
                    if (validUrl && is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        if (bitmap.getWidth() > 1000) {
                            double ratio = 1000.00 / (double) bitmap.getWidth();
                            int height = (int) ((double) bitmap.getHeight() * ratio);
                            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, height, false);
                        }
                        try {
                            file.createNewFile();
                            FileOutputStream os = new FileOutputStream(file);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            os.write(stream.toByteArray());
                            os.close();
                            stream.close();
                            Log.d("DEBUG", "Downloaded new image.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mDelegate.cached(mPath, bitmap);
        }
    }

    private static class StringReaderTask extends AsyncTask<Object, Void, String> {
        private String mPath;
        private CacheRequestDelegate mDelegate;
        protected String doInBackground(Object... params) {
            String mPath = (String)params[0];
            mDelegate= (CacheRequestDelegate)params[1];
            String key = AeSimpleSHA1.SHA1(mPath);
            File file = new File(mContext.getFilesDir(), key);

            String value = null;
            if (file.exists()) {
                try {
                    BufferedReader inputReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file))
                    );
                    StringBuffer stringBuffer = new StringBuffer();
                    String currentLine;
                    while ((currentLine = inputReader.readLine()) != null) {
                        stringBuffer.append(currentLine + "\n");
                    }
                    value = stringBuffer.toString();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return value;
        }

        @Override
        protected void onPostExecute(String result) {
            mDelegate.cached(mPath, result);
        }
    }

    public static class AeSimpleSHA1 {
        private static String convertToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (byte b : data) {
                int halfbyte = (b >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                    halfbyte = b & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public static String SHA1(String text) {
            String encoded = text;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(text.getBytes("iso-8859-1"), 0, text.length());
                byte[] sha1hash = md.digest();
                encoded = convertToHex(sha1hash);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return encoded;
        }
    }


    public static interface CacheRequestDelegate {
        void cached(String path, Object object);
        void cacheUpdated(String path, Object object);
    }

    public void performCachedRequest(String path, MultiValueMap<String, String> params,
                                       HttpMethod method, final CacheRequestDelegate delegate) {
        new ObjectReaderTask().execute(path, delegate);
        PrizmAPIService service = PrizmAPIService.getInstance();
        service.performAuthorizedRequest(path, params, method,
                new CachedRequestHandler(path, delegate), true);
    }

    private static class CachedRequestHandler extends Handler {
        private CacheRequestDelegate mDelegate;
        private String mPath;

        public CachedRequestHandler(String path, CacheRequestDelegate delegate) {
            mDelegate = delegate;
            mPath = path;
        }

        @Override
        public void handleMessage(Message message) {
            Object object = message.obj;
            if (object != null) {
                if (mPath != null && mDelegate != null) {
                    new ObjectWriterTask().execute(mPath, object);
                }
            }
            mDelegate.cacheUpdated(mPath, object);
        }
    }

    public class BitmapHandlerDelegate implements CacheRequestDelegate
    {
        private Handler mHandler;
        private int mWidth;

        public BitmapHandlerDelegate(int width, Handler handler) {
            mHandler = handler;
            mWidth = width;
        }

        @Override
        public void cached(String path, Object object) {
            if (object != null && object instanceof Bitmap) {
                Bitmap bmp = (Bitmap) object;
                if (bmp != null) {
                    if (bmp.getWidth() > mWidth && mWidth > 0) {
                        double ratio = (double)mWidth/(double)bmp.getWidth();
                        int height = (int) (((double)bmp.getHeight()) * ratio);
                        bmp = Bitmap.createScaledBitmap(bmp, mWidth, height, false);
                    }
                    Message message = mHandler.obtainMessage(1, bmp);
                    mHandler.sendMessage(message);
                }
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {

        }
    }

    public class BitmapViewDelegate implements CacheRequestDelegate {

        private ImageView mView;

        public BitmapViewDelegate(ImageView view) {
            mView = view;
        }

        @Override
        public void cached(String path, Object object) {
            if (object != null) {
                Bitmap bmp = (Bitmap) object;
                if (bmp != null) {
                    int width = mView.getWidth();
                    if (bmp.getWidth() > width && width > 0 ) {
                        Log.d("FUTURE WIDTH", String.valueOf(width));
                        Log.d("CURRENT WIDTH", String.valueOf(bmp.getWidth()));
                        Log.d("CURRENT HEIGHT", String.valueOf(bmp.getHeight()));
                        double ratio = (double)width/ (double) bmp.getWidth();
                        int height = (int) ((double) bmp.getHeight() * ratio);
                        bmp = Bitmap.createBitmap(bmp, 0, 0,width, height);
                    }
                    mView.setImageBitmap(bmp);
                }
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {

        }
    }

    public String bestAvailableImage(String path) {
        String baseUrl = path.substring(0, path.length() - 4);
        String finalUrl = String.format("%s_%s.jpg", baseUrl, String.valueOf(4));
        return finalUrl;
    }

    public void fetchBestBitmap(final String path, int width, final Handler handler) {
        loadImage(bestAvailableImage(path), new BitmapHandlerDelegate(width, handler));
    }

    public void fetchBitmap(final String path, int width, final Handler handler) {
        loadImage(path, new BitmapHandlerDelegate(width, handler));
    }

    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public void fetchBitmap(final String path, ImageView view) {
        loadImage(path, new BitmapViewDelegate(view));
    }
}
