package co.higheraltitude.prizm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.TagWatcher;
import co.higheraltitude.prizm.listeners.UserTagClickListener;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HashTagView;
import co.higheraltitude.prizm.views.UserTagView;

public class EditPostTextActivity extends AppCompatActivity implements TagWatcher.TagWatcherDelegate,
        UserTagClickListener.UserTagClickListenerDelegate {

    public static final int RESULT_EDIT_POST_TEXT = 932;
    public static final String EXTRA_POST = "extra_post";

    private Post mPost;

    private TextView mCreatorNameView;
    private TextView mTextView;
    private ImageView mAvatarView;
    private EditText mEditTextView;
    private ListView mTagPickerList;

    private UserTagAdapter mUserTagAdapter;
    private HashTagAdapter mHashTagAdapter;
    private UserTagClickListener mTagListener;
    private HashTagClickListener mHashListener;

    private String mText = "";
    private String mCurrentTag = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_text);
        mCreatorNameView = (TextView)findViewById(R.id.post_text_creator_name);
        mAvatarView = (ImageView)findViewById(R.id.post_text_avatar);
        mTextView = (TextView)findViewById(R.id.post_text);
        mEditTextView = (EditText)findViewById(R.id.edit_post_text);
        mEditTextView.addTextChangedListener(new TagWatcher(mEditTextView, this));
        mTagPickerList = (ListView)findViewById(R.id.tag_picker_list);
        mPost = getIntent().getParcelableExtra(EXTRA_POST);
        mUserTagAdapter = new UserTagAdapter(getApplicationContext(), new ArrayList<User>());
        mHashTagAdapter = new HashTagAdapter(getApplicationContext(), new ArrayList<String>());
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.backarrow_icon);
        toolbar.setNavigationOnClickListener(new BackClickListener(this));
        populateViews();
    }

    protected void populateViews() {
        if (mPost != null) {
            mCreatorNameView.setText(mPost.creatorName);
            mTextView.setText(mPost.text);
            mEditTextView.setText(mPost.text);
            mEditTextView.setSelected(true);
            PrizmDiskCache.getInstance(getApplicationContext()).fetchBitmap(
                    mPost.creatorProfilePhotoUrl, mAvatarView.getWidth(), new ImageHandler(this,
                            mAvatarView, ImageHandler.POST_IMAGE_TYPE_AVATAR, false));
        }
    }

    public void commitButtonClicked(View view) {
        String text = mEditTextView.getText().toString();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", text);
        Post.updatePost(mPost.uniqueId, params, new EditPostHandler(this));
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
        if (hasText) {
            mText = mEditTextView.getText().toString();
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

    private static class ImageHandler extends Handler {
        private Activity mActivity;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;
        private  boolean mMonochrome = false;

        public ImageHandler(Activity activity, ImageView iv, int type, boolean monochrome) {
            mActivity = activity;
            mImageView = iv;
            mType = type;
            mMonochrome = monochrome;
        }

        public void handleMessage(Message msg) {
            Bitmap bmp = (Bitmap)msg.obj;
            if (mType == POST_IMAGE_TYPE_AVATAR) {
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mActivity.getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                mImageView.setImageDrawable(avatarDrawable);
            } else if (mType == POST_IMAGE_TYPE_IMAGE) {
                if (mMonochrome) {
                    bmp = ImageHelper.monochromeBitmap(bmp);
                }
                mImageView.setImageBitmap(bmp);
            }
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

            Editable text = mEditTextView.getEditableText();
            int start = text.toString().indexOf(mCurrentTag);
            text.replace(start, start + mCurrentTag.length(), tag + " ");

            mCurrentTag = "";



            mEditTextView.setSelection(text.length());
            mHashTagAdapter.clear();

        }
    }

    private static class EditPostHandler extends Handler {

        private EditPostTextActivity mActivity;

        public EditPostHandler(EditPostTextActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof JSONObject) {
                try {
                    JSONObject obj = (JSONObject)msg.obj;
                    String text = obj.getString("text");
                    mActivity.mPost.text = text;
                    mActivity.mTextView.setText(text);
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_POST, mActivity.mPost);
                    mActivity.setResult(RESULT_OK, intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(mActivity.getApplicationContext(), "There was a problem editing " +
                            "your post.", Toast.LENGTH_SHORT).show();
                }
            }


        }

    }
}
