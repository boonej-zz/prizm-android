package co.higheraltitude.prizm.handlers;

import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import co.higheraltitude.prizm.fragments.MessageGroupFragment;

/**
 * Created by boonej on 10/15/15.
 */
public class OrganizationCountHandler extends Handler {

    private MessageGroupFragment mFragment;

    public OrganizationCountHandler(MessageGroupFragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.obj != null) {
            if (msg.obj instanceof JSONObject) {
                JSONObject object = (JSONObject)msg.obj;
                try {
                    int orgCount = object.getInt("member_count");
                    mFragment.ORGANIZATION_MEMBER_COUNT = orgCount;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }
}
