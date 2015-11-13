package co.higheraltitude.prizm.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileMainFragment extends Fragment {

    private TextView mNameView = null;
    private TextView mLocationView = null;
    private ImageView mAvatarView = null;
    private ImageView mBadgeView = null;
    private View mLocationArea = null;
    private User mUser = null;

    private static View mView;


    public ProfileMainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_profile_main, container, false);
        if (mUser != null) {
            setUser(mUser);
        }
        return mView;
    }

    private void configureViews(View view){
        mNameView = (TextView)view.findViewById(R.id.profile_name);
        mAvatarView = (ImageView)view.findViewById(R.id.profile_avatar_photo);
        mLocationView = (TextView)view.findViewById(R.id.profile_location);
        mLocationArea = view.findViewById(R.id.profile_location_area);
        mLocationArea.setVisibility(View.INVISIBLE);
        mBadgeView = (ImageView) view.findViewById(R.id.avatar_badge);
    }

    public void setUser(User user) {
        mUser = user;
        if (mView != null) {
            configureViews(mView);
            if (mUser.role != null && user.role.equals("ambassador")) {
                mBadgeView.setImageResource(R.drawable.ambassador_badge);
            } else if (mUser.role != null && user.role.equals("leader")) {
                mBadgeView.setImageResource(R.drawable.leader_badge);
            } else if (mUser.subtype != null && user.subtype.equals("luminary")) {
                mBadgeView.setImageResource(R.drawable.luminary_badge);
            } else {
                mBadgeView.setVisibility(View.GONE);
            }
            mNameView.setText(mUser.name);
            String locationString = "";
            if (mUser.city != null) {
                locationString = mUser.city;
                if (mUser.state != null) {
                    locationString = locationString + ", " + mUser.state;
                }
            } else if (mUser.state != null) {
                locationString = user.state;
            }

            mLocationView.setText(locationString);
            if (!locationString.isEmpty()) {
                mLocationArea.setVisibility(View.VISIBLE);
            }

            if (mUser.profilePhotoURL != null && !mUser.profilePhotoURL.isEmpty()) {
                PrizmDiskCache.getInstance(getContext()).fetchBitmap(mUser.profilePhotoURL,
                        mAvatarView.getWidth(), new AvatarImageHandler(mAvatarView));
            }
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
