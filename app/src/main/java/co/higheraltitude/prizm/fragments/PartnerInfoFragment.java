package co.higheraltitude.prizm.fragments;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class PartnerInfoFragment extends Fragment {

    private TextView mInfoView;
    private TextView mWebsiteView;
    private TextView mBlurbView;
    private User mUser;

    private static View mView;


    public PartnerInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_partner_info, container, false);
        if (mUser != null) {
            setUser(mUser);
        }
        return mView;
    }

    private void configureViews(View view){
        mInfoView = (TextView)view.findViewById(R.id.profile_info_text);
        mWebsiteView = (TextView)view.findViewById(R.id.profile_info_website);
        mBlurbView = (TextView)view.findViewById(R.id.profile_info_blurb);

    }

    public void setUser(User user) {
        mUser = user;
        if (mView != null) {
            configureViews(mView);
            mInfoView.setText(mUser.info);
            mWebsiteView.setText(mUser.website);
            String blurb = null;
            if (user.dateFounded != null) {
                blurb = "Founded " + user.dateFounded;
            }
            if (user.mascot != null && !user.mascot.isEmpty()) {
                if (blurb != null) {
                    blurb = blurb + " • " + user.mascot;
                } else {
                    blurb = user.mascot;
                }
            }
            if (user.population > 0) {
                if (blurb != null) {
                    blurb = blurb + " • Pop " + user.population;
                } else {
                    blurb = "Pop " + user.population;
                }
            }
            mBlurbView.setText(blurb);
        }
    }
    private static class AvatarImageHandler extends Handler
    {
        private ImageView mImageView;

        public AvatarImageHandler(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null && message.obj instanceof Bitmap) {
                Bitmap bitmap = (Bitmap)message.obj;
                AvatarDrawableFactory af = new AvatarDrawableFactory(mImageView.getResources());
                Drawable d = af.getRoundedAvatarDrawable(bitmap);
                mImageView.setImageDrawable(d);
            }
        }
    }



}
