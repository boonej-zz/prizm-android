package co.higheraltitude.prizm.listeners;

import android.view.Gravity;
import android.view.View;

import co.higheraltitude.prizm.MessageGroupsActivity;

/**
 * Created by boonej on 9/30/15.
 */
public class MenuClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        MessageGroupsActivity.mDrawerLayout.openDrawer(Gravity.LEFT);
    }
}
