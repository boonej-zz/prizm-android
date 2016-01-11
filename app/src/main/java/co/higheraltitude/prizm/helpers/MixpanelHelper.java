package co.higheraltitude.prizm.helpers;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import co.higheraltitude.prizm.R;

/**
 * Created by boonej on 12/22/15.
 */
public class MixpanelHelper {
    private static MixpanelHelper mInstance;
    private MixpanelAPI mTracker;


    public static MixpanelHelper getInstance() {
        return mInstance;
    }

    public static void initialize(Context context) {
        MixpanelHelper helper = new MixpanelHelper();
        helper.mTracker = MixpanelAPI.getInstance(context,
                context.getString(R.string.mixpanel_token));
        mInstance = helper;
    }

    public static MixpanelAPI getTracker() {
        if (mInstance != null) {
            return  mInstance.mTracker;
        } else {
            return null;
        }
    }
}
