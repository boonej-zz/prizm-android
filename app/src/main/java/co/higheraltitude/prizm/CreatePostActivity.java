package co.higheraltitude.prizm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.custom.ToggleButton;
import co.higheraltitude.prizm.custom.ToggleImageButton;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.TagWatcher;
import co.higheraltitude.prizm.listeners.UserTagClickListener;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HashTagView;
import co.higheraltitude.prizm.views.UserTagView;


public class CreatePostActivity extends AppCompatActivity implements TagWatcher.TagWatcherDelegate,
        UserTagClickListener.UserTagClickListenerDelegate{

    public static final int GALLERY_CAPTURE = 994;
    public static final int CAMERA_CAPTURE = 995;
    public static final int RESULT_CROPPED_IMAGE = 999;
    public static final int REQUEST_CAMERA_PERMISSIONS = 122;
    public static final int REQUEST_LOCATION_PERMISSIONS = 123;
    public static final int RESULT_CREATE_POST = 834;
    private Uri contentInUri;
    private Uri contentOutUri;

    private EditText mPostText;

    // Buttons
    private ToggleButton mAspirationButton;
    private ToggleButton mPassionButton;
    private ToggleButton mExperienceButton;
    private ToggleButton mAchievementButton;
    private ToggleButton mInspirationButton;
    private ToggleButton mPrivateButton;

    private ImageView mPostImageView;

    private ToggleImageButton mVisibilityButton;
    private ImageButton mDoneButton;


    private ArrayList<ToggleButton> mButtonList;

    private String mCategory;
    private String mText;
    private String mFilePath;
    private String mVisibility = Post.VISIBILITY_PUBLIC;
    private String mCurrentTag;

    private UserTagAdapter mUserTagAdapter;
    private HashTagAdapter mHashTagAdapter;
    private ListView mTagPickerList;
    private UserTagClickListener mTagListener;
    private HashTagClickListener mHashListener;

    private double mLocationLatitude;
    private double mLocationLongitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        loadIntentData();
        configureToolbar();
        configureViews();
    }

    protected void loadIntentData() {
        File outImageFile = new File(getCacheDir(), UUID.randomUUID().toString() + ".jpg");
        contentOutUri = Uri.fromFile(outImageFile);
    }

    protected void configureToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.backarrow_icon);
        toolbar.setNavigationOnClickListener(new BackClickListener(this));
        mDoneButton = (ImageButton)findViewById(R.id.action_done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post.createPost(getParameters(), new PostHandler(CreatePostActivity.this));
            }
        });
    }

    protected void configureViews() {
        mAspirationButton = (ToggleButton)findViewById(R.id.aspiration_button);
        mPassionButton = (ToggleButton)findViewById(R.id.passion_button);
        mExperienceButton = (ToggleButton)findViewById(R.id.experience_button);
        mAchievementButton = (ToggleButton)findViewById(R.id.achievement_button);
        mInspirationButton = (ToggleButton)findViewById(R.id.inspiration_button);
        mPrivateButton = (ToggleButton)findViewById(R.id.private_button);
        mButtonList = new ArrayList<>();
        mButtonList.add(mAspirationButton);
        mButtonList.add(mPassionButton);
        mButtonList.add(mAchievementButton);
        mButtonList.add(mInspirationButton);
        mButtonList.add(mExperienceButton);
        mButtonList.add(mPrivateButton);
        mVisibilityButton = (ToggleImageButton)findViewById(R.id.visibility_button);
        mVisibilityButton.setSelected(true);
        mPostText = (EditText)findViewById(R.id.post_text);
        mPostText.addTextChangedListener(new TagWatcher(mPostText, this));
        mPostText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mText = mPostText.getText().toString();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });
        mPostImageView = (ImageView)findViewById(R.id.preview_image);
        mUserTagAdapter = new UserTagAdapter(getApplicationContext(), new ArrayList<User>());
        mTagPickerList = (ListView)findViewById(R.id.tag_picker_list);

        mHashTagAdapter = new HashTagAdapter(getApplicationContext(), new ArrayList<String>());
        mTagListener = new UserTagClickListener(getApplicationContext(), mPostText, this);
        mHashListener = new HashTagClickListener();
    }

    private MultiValueMap<String, String> getParameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        if (mText != null && !mText.isEmpty()) {
            parameters.add("text", mText);
        }
        parameters.add("category", mCategory);
        parameters.add("scope", mVisibility);
        parameters.add("creator", User.getCurrentUser().uniqueID);
        if (mFilePath != null) {
            parameters.add("file_path", mFilePath);
        }
        if (mLocationLongitude != 0) {
            parameters.add("location_latitude", String.valueOf(mLocationLatitude));
        }
        if (mLocationLongitude != 0) {
            parameters.add("location_longitude", String.valueOf(mLocationLongitude));
        }
        return parameters;
    }

    private void deselectAll(View view) {
        for (ToggleButton button : mButtonList) {
            if (button != view) {
                button.setSelected(false);
            }
        }
    }

    private void validateForm() {
        boolean hasText = (mPostText.getText() != null && !mPostText.getText().toString().isEmpty())
                || mFilePath != null;
        boolean hasCategory = mCategory != null;
        if (hasText && hasCategory) {
            mDoneButton.setVisibility(View.VISIBLE);
        } else {
            mDoneButton.setVisibility(View.GONE);
        }
    }

    private void requestCamera() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSIONS);
    }

    public void locationButtonClicked(View view) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        } else {
            Location location = MainActivity.lastLocation();
            mLocationLatitude = location.getLatitude();
            mLocationLongitude = location.getLongitude();
        }
    }

    public void aspirationButtonClicked(View view) {
        deselectAll(view);
        mCategory = view.isSelected()?Post.CATEGORY_ASPIRATION:null;
        validateForm();
    }

    public void inspirationButtonClicked(View view) {
        deselectAll(view);
        mCategory = view.isSelected()?Post.CATEGORY_INSPIRATION:null;
        validateForm();
    }

    public void passionButtonClicked(View view) {
        mCategory = view.isSelected()?Post.CATEGORY_PASSION:null;
        deselectAll(view);
        validateForm();
    }

    public void experienceButtonClicked(View view) {
        mCategory = view.isSelected()?Post.CATEGORY_EXPERIENCE:null;
        deselectAll(view);
        validateForm();
    }

    public void achievementButtonClicked(View view) {
        mCategory = view.isSelected()?Post.CATEGORY_ACHIEVEMENT:null;
        deselectAll(view);
        validateForm();
    }

    public void privateButtonClicked(View view) {
        deselectAll(view);
        mCategory = view.isSelected()?Post.CATEGORY_PRIVATE:null;
        if (view.isSelected()) {
            mVisibilityButton.setSelected(false);
            mVisibility = Post.VISIBILITY_PRIVATE;
        } else {
            mVisibility = Post.VISIBILITY_TRUST;
        }
        validateForm();
    }

    public void visibilityButtonClicked(View view) {
        if (mCategory == Post.CATEGORY_PRIVATE) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_scope_private),
                    Toast.LENGTH_SHORT).show();
            mVisibilityButton.setSelected(false);
        } else {
            mVisibility = view.isSelected()?Post.VISIBILITY_PUBLIC:Post.VISIBILITY_TRUST;
        }
        validateForm();
    }

    public void tagButtonClicked(View view){
        mPostText.append("@");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPostText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void photoButtonClicked(View view) {

        final AlertDialog.Builder getImageFrom = new AlertDialog.Builder(CreatePostActivity.this);
        getImageFrom.setTitle(getString(R.string.title_photo_chooser));

        getImageFrom.setItems(R.array.photo_locations, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                boolean requestCamera = false;
                int reqType = 0;
                switch (which) {
                    case 0:

                        if (ContextCompat.checkSelfPermission(CreatePostActivity.this,
                                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestCamera = true;
                        } else {
                            intent = new Intent(getApplicationContext(), CameraActivity.class);
                            reqType = CAMERA_CAPTURE;
                        }
                        break;
//                        File croppedImageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//                                UUID.randomUUID() + ".jpg");
//                        contentInUri = Uri.fromFile(croppedImageFile);

//                            requestCamera = true;
//                        } else {
//                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentInUri);
//                            reqType = CAMERA_CAPTURE;
//                        }
//                        break;
                    case 1:
                        intent = new Intent();
                        intent.setType("image/*");
                        reqType = GALLERY_CAPTURE;
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        break;
                    default:
                        break;
                }
                if (intent != null) {
                    startActivityForResult(intent, reqType);
                }
                if (requestCamera) {
                    requestCamera();
                }
            }
        });
        getImageFrom.show();
    }

    @Override
    public void noTagsPresent() {
        mUserTagAdapter.clear();
        mHashTagAdapter.clear();
    }

    @Override
    public void userTagsFound(String tag) {
        mCurrentTag = tag;
        User.getAvailableTags(tag.substring(1), new UserTagDelegate());
        mTagPickerList.setAdapter(mUserTagAdapter);
    }

    @Override
    public void hashTagsFound(String tag) {
        mCurrentTag = tag;
        if (tag.length() > 1) {
            Post.searchHashtags(tag.substring(1), new HashTagDelegate());
            mTagPickerList.setAdapter(mUserTagAdapter);
        }
    }

    @Override
    public void fieldHasText(Boolean hasText) {
        validateForm();
        if (hasText) {
            mText = mPostText.getText().toString();
        }
        if (!hasText) {
            mCurrentTag = "";
        }
    }

    @Override
    public String currentTag() {
        return mCurrentTag;
    }

    @Override
    public void tagSelected() {
        mUserTagAdapter.clear();
    }

    private void cropImage(Uri inputUri, Uri outputUri) {
//        CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(1, 1, 600, 600, outputUri);
//        intentBuilder.setOutlineColor(0xFF03A9F4);
//        intentBuilder.setSourceImage(inputUri);
//        startActivityForResult(intentBuilder.getIntent(getApplicationContext()), RESULT_CROPPED_IMAGE);
        Intent intent = new Intent(getApplicationContext(), ImageCropActivity.class);
        intent.setData(inputUri);
        intent.putExtra(ImageCropActivity.EXTRA_ASPECT_X, 1);
        intent.putExtra(ImageCropActivity.EXTRA_ASPECT_Y, 1);
        startActivityForResult(intent, RESULT_CROPPED_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageHelper imageHelper = ImageHelper.getInstance();
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE) {
                Uri selectedImage = data.getData();
                mPostImageView.setImageURI(selectedImage);
                mFilePath = ImageHelper.getInstance().uploadPostImage(ImageHelper.getInstance().bitmapFromUri(selectedImage), null);
            } else if (requestCode == GALLERY_CAPTURE) {
                Uri selectedImage = data.getData();
                File copiedFile = new File(getCacheDir(), UUID.randomUUID().toString() + ".jpg");
                Bitmap bmp = imageHelper.bitmapFromUri(selectedImage);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(copiedFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (Exception e) {
                        }
                    }
                }

                cropImage(Uri.fromFile(copiedFile), contentOutUri);
            } else if (requestCode == RESULT_CROPPED_IMAGE) {
                Uri outputImage = data.getData();
                Bitmap bmp = imageHelper.bitmapFromUri(outputImage);
                mPostImageView.setImageBitmap(bmp);
                mFilePath = ImageHelper.getInstance().uploadPostImage(bmp, null);
            }
        }
        validateForm();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSIONS:
                    Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                    startActivityForResult(intent, CAMERA_CAPTURE);
                    break;
                case REQUEST_LOCATION_PERMISSIONS:
                    locationButtonClicked(mPostImageView);
                    break;
                default:
                    break;
            }
        }
    }

    private static class PostHandler extends Handler {
        private Activity mActivity;
        private PostHandler(Activity activity) {
            mActivity = activity;
        }
        public void handleMessage(Message msg) {
            mActivity.setResult(RESULT_OK);
            mActivity.finish();
        }
    }

    private class UserTagDelegate implements PrizmDiskCache.CacheRequestDelegate {

        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<User> users = (ArrayList<User>)object;
                mUserTagAdapter.clear();
                mUserTagAdapter.addAll(users);
                mTagPickerList.setAdapter(mUserTagAdapter);
                mTagPickerList.setOnItemClickListener(mTagListener);
            }
        }

    }

    private class HashTagDelegate implements PrizmDiskCache.CacheRequestDelegate {

        @Override
        public void cached(String path, Object object) {

        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<String> tags = (ArrayList<String>)object;
                ArrayList<String> newTags = new ArrayList<>();
                mHashTagAdapter.clear();
                for (String tag : tags) {
                    String newTag = "#" + tag;
                    newTags.add(newTag);
                }
                mHashTagAdapter.addAll(newTags);
                mTagPickerList.setAdapter(mHashTagAdapter);
                mTagPickerList.setOnItemClickListener(mHashListener);
            }
        }

    }

    private class UserTagAdapter extends ArrayAdapter<User> {

        private UserNameFilter filter;
        private ArrayList<User> baseList;
        private ArrayList<User> userList;

        public UserTagAdapter(Context c, List<User> users) {
            super(c, 0, users);
            this.baseList = new ArrayList<>();
            this.baseList.addAll(users);
            this.userList = new ArrayList<>();
            this.userList.addAll(users);
        }

        @Override
        public int getCount() {
            int count = super.getCount();
            count = count > 4?4:count;
            return count;
        }

        public void setBaseList(ArrayList<User> list) {
            baseList = new ArrayList<>();
            baseList.addAll(list);
            userList = new ArrayList<>();
            userList.addAll(list);
            notifyDataSetChanged();
            clear();
            addAll(baseList);
            notifyDataSetInvalidated();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserTagView view = (UserTagView)convertView;
            if (view == null) {
                view = UserTagView.inflate(parent);
            }
            view.setUser(getItem(position));
            return view;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new UserNameFilter();
            }
            return filter;
        }

        private class UserNameFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase().substring(1);
                FilterResults results = new FilterResults();
                Iterator<User> iterator = baseList.iterator();
                ArrayList<User> filt = new ArrayList<>();

                if (constraint != null && constraint.toString().length() > 0) {
                    while (iterator.hasNext()) {
                        User u = iterator.next();
                        if (u.name.toLowerCase().contains(constraint)) {
                            filt.add(u);
                        }
                    }
                }
                results.values = filt;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                userList = (ArrayList<User>)results.values;
                notifyDataSetChanged();
                clear();
                Iterator<User> iterator = userList.iterator();
                while (iterator.hasNext()) {
                    add(iterator.next());
                }
                notifyDataSetInvalidated();
            }
        }
    }

    private static class HashTagAdapter extends ArrayAdapter<String> {

        public HashTagAdapter(Context c, List<String> hashTags) {
            super(c, 0, hashTags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HashTagView view = (HashTagView)convertView;
            if (view == null) {
                view = HashTagView.inflate(parent);
            }
            view.setHashTag(this.getItem(position));
            return view;
        }
    }

    private class HashTagClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashTagView tagView = (HashTagView)view;
            String tag = tagView.getHashTag();

            Editable text = mPostText.getEditableText();
            int start = text.toString().indexOf(mCurrentTag);
            text.replace(start, start + mCurrentTag.length(), tag + " ");

            mCurrentTag = "";



            mPostText.setSelection(text.length());
            mHashTagAdapter.clear();

        }
    }
}
