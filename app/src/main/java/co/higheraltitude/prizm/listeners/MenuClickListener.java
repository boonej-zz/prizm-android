package co.higheraltitude.prizm.listeners;

import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;

import co.higheraltitude.prizm.MessageGroupsActivity;

/**
 * Created by boonej on 9/30/15.
 */
public class MenuClickListener implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;

    public MenuClickListener(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    @Override
    public void onClick(View view) {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }
}
