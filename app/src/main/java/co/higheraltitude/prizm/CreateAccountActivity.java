package co.higheraltitude.prizm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.fragments.NewPartnerFragment;
import co.higheraltitude.prizm.fragments.NewUserFragment;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.network.PrizmAPIService;

public class CreateAccountActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 932;
    public static final int RESULT_COVER_IMAGE = 933;
    private ImageView profilePhotoImageView;
    private ImageView coverPhotoImageView;
    public static String profilePhotoUrl = "";
    public static String coverPhotoUrl = "";
    private Bundle baseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        profilePhotoImageView = (ImageView)findViewById(R.id.edit_profile_avatar_image);
        coverPhotoImageView = (ImageView)findViewById(R.id.edit_profile_cover_photo);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.registration_tabs);
        ViewPager viewPager = (ViewPager)findViewById(R.id.registration_view_pager);
        viewPager.setAdapter(new RegistrationPager(getSupportFragmentManager(), CreateAccountActivity.this));
        tabLayout.setupWithViewPager(viewPager);
        baseUser = getIntent().getBundleExtra(LoginActivity.EXTRA_PROFILE_BASE);
        if (baseUser != null && !baseUser.getString("profile_photo_url", "").isEmpty()) {
            profilePhotoUrl = baseUser.getString("profile_photo_url");
            PrizmCache.getInstance().fetchDrawable(profilePhotoUrl, new AvatarDownloadHandler(getApplicationContext(),
                    profilePhotoImageView));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void back(View view) {
        finish();
    }

    public void uploadClicked(View view) {

//        final List<Intent> cameraIntents = new ArrayList<Intent>();
//        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        final PackageManager packageManager = getPackageManager();
//        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
//        for(ResolveInfo res : listCam) {
//            final String packageName = res.activityInfo.packageName;
//            final Intent intent = new Intent(captureIntent);
//            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//            intent.setPackage(packageName);
//            cameraIntents.add(intent);
//        }
//        final Intent galleryIntent = new Intent();
//        galleryIntent.setType("image/*");
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
//
//        startActivityForResult(chooserIntent, RESULT_IMAGE);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_IMAGE);

    }

    public void uploadCoverPhotoClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_COVER_IMAGE);
    }

    public class RegistrationPager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String[] titles = new String[] {getString(R.string.registration_individual),
                getString(R.string.registration_partner)};
        private Context context;

        public RegistrationPager(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return NewUserFragment.newInstance(baseUser);
            } else {
                return NewPartnerFragment.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String fieldName;
            if (requestCode == RESULT_IMAGE) {
                fieldName = "profile_photo_url";
            } else if (requestCode == RESULT_COVER_IMAGE) {
                fieldName = "profile_cover_photo_url";
            }
            if (data != null) {
                Uri selectedImage = data.getData();
                ImageHelper ih = ImageHelper.getInstance();
                Bitmap bmp = ih.bitmapFromUri(selectedImage);
                if (requestCode == RESULT_IMAGE) {
                    bmp = ih.cropToSquare(bmp);
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                    profilePhotoImageView.setImageDrawable(avatarDrawable);
                } else if (requestCode == RESULT_COVER_IMAGE) {
                    bmp = ih.cropCoverPhoto(bmp);
                    coverPhotoImageView.setImageBitmap(bmp);
                }
                String path = ih.uploadImage(bmp);

                if (requestCode == RESULT_IMAGE) {
                    profilePhotoUrl = path;
                } else if (requestCode == RESULT_COVER_IMAGE) {
                    coverPhotoUrl = path;
                }
            }


        }
    }

    private static class AvatarDownloadHandler extends Handler {
        private ImageView mImageView;
        private Context mContext;

        public AvatarDownloadHandler(Context context, ImageView imageView) {
            mContext = context;
            mImageView = imageView;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null && message.obj instanceof Bitmap) {
                Bitmap bitmap = (Bitmap)message.obj;
                AvatarDrawableFactory avatarDrawableFactory =
                        new AvatarDrawableFactory(mContext.getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bitmap);
                mImageView.setImageDrawable(avatarDrawable);
            }
        }
    }

}
