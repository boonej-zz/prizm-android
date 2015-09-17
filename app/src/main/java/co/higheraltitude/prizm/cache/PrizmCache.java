package co.higheraltitude.prizm.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.os.Handler;


import com.google.gson.Gson;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCache;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCacheBuilder;
import com.vincentbrison.openlibraries.android.dualcache.lib.SizeOf;
import com.vincentbrison.openlibraries.android.dualcache.lib.Serializer;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by boonej on 8/31/15.
 */
public class PrizmCache {
    private static final PrizmCache instance = new PrizmCache();
    private static final String objectCacheName = "co.higheraltitude.prizm.cache.object";
    private static final String imageCacheName = "co.higheraltitude.prizm.cache.image";
    public static DualCache<Object> objectCache;
    public static DualCache<Bitmap> bitmapCache;

    private PrizmCache() {
        if (objectCache == null) {
            objectCache = new DualCacheBuilder<Object>(objectCacheName, 1, Object.class)
                    .useDefaultSerializerInRam(1024)
                    .useDefaultSerializerInDisk(10240, true);
        }
        if (bitmapCache == null) {
            bitmapCache = new DualCacheBuilder<Bitmap>(imageCacheName, 1, Bitmap.class)
                    .useReferenceInRam(1024, new SizeOfBitmap())
                    .useDefaultSerializerInDisk(10240, true);
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

    public static void clearObjectCache() {
        instance.objectCache.invalidate();
    }

    public void fetchDrawable(final String path, final ImageView imageView) {
        String temp = "";
        if (path != null) {
            String[] aPath = path.split("/");
            temp = aPath[aPath.length - 1];
            temp = temp.replace(":", "");
            temp = temp.replace("/", "_");
            temp = temp.replace(".", "_");
            temp = temp.toLowerCase();
        }
        final String p = temp;
        try {
            if (path != null && !path.isEmpty()) {
                Object obj = bitmapCache.get(p);
                Bitmap bmp = bitmapCache.get(p);
                if (bmp != null) {
                    imageView.setImageBitmap(bmp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageBitmap((Bitmap) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Bitmap bmp = BitmapFactory.decodeStream((InputStream) (new URL(path).getContent()));
                    if (bmp.getWidth() > 1000) {
                        double ratio = 1000.00/(double)bmp.getWidth();
                        int height = (int)((double)bmp.getHeight() * ratio);
                        bmp = Bitmap.createBitmap(bmp, 0, 0, 1000, height);
                    }
                    Message message = handler.obtainMessage(1, bmp);
                    bitmapCache.put(p, bmp);
                    handler.sendMessage(message);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
        thread.start();

    }


}
