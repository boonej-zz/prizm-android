package co.higheraltitude.prizm.cache;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.os.Handler;


import com.google.gson.Gson;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCache;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCacheBuilder;
import com.vincentbrison.openlibraries.android.dualcache.lib.SizeOf;
import com.vincentbrison.openlibraries.android.dualcache.lib.Serializer;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import co.higheraltitude.prizm.network.PrizmAPIService;

/**
 * Created by boonej on 8/31/15.
 */
public class PrizmCache {
    private static final PrizmCache instance = new PrizmCache();
    private static final String objectCacheName = "co.higheraltitude.prizm.cache.object";
    private static final String imageCacheName = "co.higheraltitude.prizm.cache.image";
    public static DualCache<Object> objectCache;
    public static DualCache<Bitmap> bitmapCache;
    private static ArrayList<String> pendingImageUrls;
    private static ArrayList<ImageView> pendingImageViews;
    private static ArrayList<String> pendingKeys;
    private static Boolean isUpdatingImages = false;
    private static Thread processThread;



    private PrizmCache() {
        long availableMemory = Runtime.getRuntime().maxMemory();
        long cacheSize = availableMemory/3;
//        long bitmapCacheSize = cacheSize * 2;
        long storageSize = cacheSize * 10;
        if (objectCache == null) {

            objectCache = new DualCacheBuilder<Object>(objectCacheName, 1, Object.class)
                    .useDefaultSerializerInRam((int)cacheSize)
                    .useDefaultSerializerInDisk((int) storageSize, true);
        }
        if (bitmapCache == null) {
            bitmapCache = new DualCacheBuilder<Bitmap>(imageCacheName, 1, Bitmap.class)
                    .useReferenceInRam((int)cacheSize, new SizeOfBitmap())
//                    .useCustomSerializerInDisk((int)storageSize *2, true, new BitmapSerializer());
                    .useDefaultSerializerInDisk((int)storageSize * 2, true);
        }
    }

    public static PrizmCache getInstance() {
        return instance;
    }

    private static class SizeOfBitmap implements SizeOf<Bitmap> {

        @Override
        public int sizeOf(Bitmap object) {
            return object.getByteCount();
        }
    }

    private static class PrizmSerializer implements Serializer<Object> {
        @Override
        public String toString(Object object) {
            Gson gson = new Gson();
            String string = gson.toJson(object);
            return gson.toJson(object);
        }

        public Object fromString(String string) {
            Gson gson = new Gson();
            JSONObject object = gson.fromJson(string, JSONObject.class);
            return object;
        }
    }

    private static class BitmapSerializer implements Serializer<Bitmap> {

        @Override
        public Bitmap fromString(String data) {
            byte [] b = data.getBytes();
            return  BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        @Override
        public String toString(Bitmap object) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            object.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toString();
        }
    }

    public static void clearObjectCache() {
        instance.objectCache.invalidate();
    }

    private static class DrawableHandler extends Handler {
        public DrawableHandler(){}
        @Override
        public void handleMessage(Message message) {
            HashMap<String, Object> handlerData = (HashMap<String, Object>) message.obj;
            ImageView iv = (ImageView) handlerData.get("iv");
            Bitmap bmp = (Bitmap) handlerData.get("bitmap");
            iv.setImageBitmap(bmp);

        }
    }

    public void cancelImageView(final ImageView imageView) {
//        if (processThread != null) {
//            try {
//                processThread.wait();
//
//            } catch (Exception e) {
//                Log.d("DEBUG", "Thread interrupted.");
//            }
//            if (pendingImageViews != null) {
//                int index = pendingImageViews.indexOf(imageView);
//                if (index != -1) {
//                    pendingImageViews.remove(index);
//                    pendingImageUrls.remove(index);
//                    pendingKeys.remove(index);
//                }
//            }
//            processThread.notify();
//
//        }
    }

    private static class CachedRequestHandler extends Handler {
        private Handler mHandler;
        private String mKey;

        public CachedRequestHandler(String key, Handler handler) {
            mHandler = handler;
            mKey = key;
        }

        @Override
        public void handleMessage(Message message) {
            Object object = message.obj;
            if (object != null) {
                if (mKey != null && mHandler != null && object != null) {
                    try {
                        Gson gson = new Gson();
                        String value = gson.toJson(object);
                        objectCache.put(mKey, value);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Message mMessage = mHandler.obtainMessage(1, object);
                    mHandler.sendMessage(mMessage);
                }
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    public Object performCachedRequest(String path, MultiValueMap<String, String> params,
                                     HttpMethod method, Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        String key = null;
        try {
            key = AeSimpleSHA1.SHA1(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Object result = null;
        CachedRequestHandler cachedRequestHandler = null;
        if (key != null) {


        try {
            result = objectCache.get(key);
            if (result instanceof String) {
                result = new JSONTokener((String)result).nextValue();
            }
            if (result instanceof JSONObject) {
                if (((JSONObject)result).has("values")) {
                    result = ((JSONObject)result).get("values");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

            cachedRequestHandler = new CachedRequestHandler(key, handler);
        }
        if (cachedRequestHandler != null) {
            service.performAuthorizedRequest(path, params, method, cachedRequestHandler, true);
        } else {
            service.performAuthorizedRequest(path, params, method, handler, true);
        }

        return result;
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

        public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        }
    }


}
