package co.higheraltitude.prizm.listeners;

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * Created by boonej on 9/30/15.
 */
public class BackClickListener implements View.OnClickListener {

    private Activity mActivity;

    public BackClickListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        mActivity.finish();
    }
}