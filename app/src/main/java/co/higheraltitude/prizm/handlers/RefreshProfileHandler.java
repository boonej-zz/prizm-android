package co.higheraltitude.prizm.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.MessageGroupsActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 10/15/15.
 */
public class RefreshProfileHandler extends Handler {
    private Context mContext;
    private Activity mActivity;
    private ViewPager mViewPager;
    public RefreshProfileHandler(Context context, Activity activity){
        mContext = context;
        mActivity = activity;
    }
    @Override
    public void handleMessage(Message msg) {
        Object obj = msg.obj;
        if (obj != null) {
            User.setCurrentUser((User) obj);
            if (User.getCurrentUser().primaryOrganization != null && ! User.getCurrentUser().primaryOrganization.isEmpty()) {
                Intent intent = new Intent(mContext, MessageGroupsActivity.class);
                intent.putExtra(LoginActivity.EXTRA_PROFILE, (User) obj);

                mActivity.startActivity(intent);
                MainActivity.DID_START = true;
                MainActivity.MESSAGES_STARTED = true;
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.error_no_membership), Toast.LENGTH_LONG).show();
                User.setCurrentUser(null);
            }
        }
    }
}
