package co.higheraltitude.prizm;

import android.app.ActionBar;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.handlers.NonScrollListView;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.TagWatcher;
import co.higheraltitude.prizm.listeners.UserTagClickListener;
import co.higheraltitude.prizm.models.Comment;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.CommentView;
import co.higheraltitude.prizm.views.UserTagView;

public class FullBleedPostActivity extends AppCompatActivity
        implements CommentView.CommentViewDelegate, TagWatcher.TagWatcherDelegate,
        UserTagClickListener.UserTagClickListenerDelegate {

    public static final String EXTRA_POST = "extra_post";
    public static final String EXTRA_POST_ID = "extra_post_id";

    private Post mPost;

    private ImageView mPostImageView;
    private ImageView mAvatarView;
    private TextView mCreatorTextView;
    private TextView mDateAgoTextView;
    private TextView mPostViaText;
    private TextView mLikesCount;
    private TextView mCommentCount;
    private View mLikesButton;
    private ImageView mLikesImageView;
    private ImageView mCategoryImageView;
    private TextView mHashTagsTextView;
    private View mCommentButton;
    private PrizmDiskCache mCache;
    private TextView mPostTextCreator;
    private TextView mPostText;
    private View mPostTextArea;
    private ImageView mPostTextAvatar;
    private NonScrollListView mCommentsList;
    private CommentsListAdapter mAdapter;
    private ScrollView mScrollView;
    private EditText mCreateCommentText;
    private UserTagAdapter mUserTagAdapter;
    private ListView mTagPickerList;
    private String mCurrentTag;
    private String mPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_bleed_post);
        configureActionBar();
        mCache = PrizmDiskCache.getInstance(getApplicationContext());
        mPost = getIntent().getParcelableExtra(EXTRA_POST);
        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        configureViews();

        configureAdapter();
        if (mPost != null) {
            layoutPost();
            Comment.fetchComments(mPost, new CommentDelegate());
        } else if (mPostId != null && !mPostId.isEmpty()) {
            Post.fetchPost(mPostId, new PostDelegate());
        }
    }

    private void configureActionBar()
    {
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();
    }

    private void showCreatorProfile()
    {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        User u = new User();
        u.uniqueID = mPost.creatorId;
        u.type = mPost.creatorType;
        u.subtype = mPost.creatorSubtype;
        u.profilePhotoURL = mPost.creatorProfilePhotoUrl;
        intent.putExtra(LoginActivity.EXTRA_PROFILE, u);
        startActivity(intent);
    }

    private void configureViews()
    {
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatorProfile();
            }
        });
        mPostViaText = (TextView)findViewById(R.id.post_via_text);
        mCreatorTextView = (TextView)findViewById(R.id.creator_name);
        mLikesCount = (TextView)findViewById(R.id.likes_count);
        mLikesButton = findViewById(R.id.likes_button);
        mLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeButtonClicked();
            }
        });
        mLikesImageView = (ImageView)findViewById(R.id.likes_image);
        mDateAgoTextView = (TextView)findViewById(R.id.date_text);
        mCategoryImageView = (ImageView)findViewById(R.id.category_image);
        mPostImageView = (ImageView)findViewById(R.id.post_image_view);
        mPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mCommentCount = (TextView)findViewById(R.id.comment_count);
        mPostText = (TextView)findViewById(R.id.post_text);
        mPostTextCreator = (TextView)findViewById(R.id.post_text_creator_name);
        mPostTextAvatar = (ImageView)findViewById(R.id.post_text_avatar);
        mPostTextAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatorProfile();
            }
        });
        mPostTextArea = findViewById(R.id.post_text_area);
        mCommentsList = (NonScrollListView)findViewById(R.id.comments_list);
        mScrollView = (ScrollView)findViewById(R.id.comments_scroll_view);
        mCreateCommentText = (EditText)findViewById(R.id.new_comment_text);
        mCreateCommentText.addTextChangedListener(new TagWatcher(mCreateCommentText, this));
        mCreateCommentText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Comment.createComment(mPost, mCreateCommentText.getText().toString(),
                        new CommentLikeHandler(FullBleedPostActivity.this));
                mCreateCommentText.setText(null);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });
        mTagPickerList = (ListView)findViewById(R.id.tag_picker_list);
        mHashTagsTextView = (TextView)findViewById(R.id.hash_tag_view);


    }

    private void configureAdapter()
    {
        mAdapter = new CommentsListAdapter(getApplicationContext(), new ArrayList<Comment>());
        mCommentsList.setAdapter(mAdapter);
        mUserTagAdapter = new UserTagAdapter(getApplicationContext(), new ArrayList<User>());
        mTagPickerList.setAdapter(mUserTagAdapter);
        mTagPickerList.setOnItemClickListener(new UserTagClickListener(getApplicationContext(),
                mCreateCommentText, this));
    }

    private void layoutPost()
    {
        if (mPost.externalProvider != null && !mPost.externalProvider.isEmpty()) {
            String provider = mPost.externalProvider.substring(0, 1).toUpperCase() +
                    mPost.externalProvider.substring(1);
            mPostViaText.setText(String.format("Post via %s", provider));
        }
        mCreatorTextView.setText(mPost.creatorName);
        mDateAgoTextView.setText(String.format("%s ago", mPost.timeSince));
        setCategoryImage();
        mCache.fetchBitmap(mPost.creatorProfilePhotoUrl, mAvatarView.getWidth(),
                new ImageHandler(this, mAvatarView, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        mCache.fetchBitmap(mPost.filePath, mPostImageView.getWidth(),
                new ImageHandler(this, mPostImageView, ImageHandler.POST_IMAGE_TYPE_IMAGE));
        mCommentCount.setText(String.valueOf(mPost.commentsCount));
        mLikesCount.setText(String.valueOf(mPost.likesCount));
        mHashTagsTextView.setText(mPost.hashTags);
        if (mPost.commentsCount == 0) {
            mCommentCount.setVisibility(View.INVISIBLE);
        }
        if (mPost.likesCount == 0) {
            mLikesCount.setVisibility(View.INVISIBLE);
        }
        if (mPost.isLiked) {
            mLikesImageView.setImageResource(R.drawable.like_selected_icon);
        } else {
            mLikesImageView.setImageResource(R.drawable.like_icon);
        }
        setPostText();
    }

    public void likeButtonClicked() {
        if (mPost.ownPost) {
            Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
            intent.putExtra(LikesActivity.EXTRA_POST, mPost);
            startActivity(intent);
        } else {
            if (mPost.isLiked) {
                Post.unlikePost(mPost, new LikeHandler(this));
            } else {
                Post.likePost(mPost, new LikeHandler(this));
            }
        }
    }

    private void setCategoryImage()
    {
        if (mPost.category.equals("aspiration")) {
            mCategoryImageView.setImageResource(R.drawable.aspitation_active_icon);
        } else if (mPost.category.equals("passion")) {
            mCategoryImageView.setImageResource(R.drawable.passion_active_icon);
        } else if (mPost.category.equals("experience")) {
            mCategoryImageView.setImageResource(R.drawable.experience_active_icon);
        } else if (mPost.category.equals("inspiration")) {
            mCategoryImageView.setImageResource(R.drawable.inspiration_active_icon);
        } else if (mPost.category.equals("personal")) {
            mCategoryImageView.setImageResource(R.drawable.private_active_icon);
        } else if (mPost.category.equals("achievement")) {
            mCategoryImageView.setImageResource(R.drawable.achievement_active_icon);
        }
    }

    private void setPostText()
    {
        if (mPost.text != null && !mPost.text.isEmpty()) {
            Pattern userTag = Pattern.compile("@\\([^)]+\\)");
            Pattern hashTag = Pattern.compile("#[\\S]+");
            String text = mPost.text;
            Matcher matcher = userTag.matcher(text);
            ArrayList<Object> matches = new ArrayList<>();
            while (matcher.find()) {
                String match = matcher.group();
                if (match != null) {
                    String m = match.substring(2, match.length() - 1);
                    final String[] split = m.split("\\|");
                    matches.add(split);
                    text = text.replace(match, "@" + split[0]);
                }
            }
            SpannableString spanText = new SpannableString(text);
            Iterator i = matches.iterator();
            while (i.hasNext()) {
                String [] m = (String[])i.next();
                int idx = text.indexOf("@" + m[0]);
                int len = m[0].length() + 1;
                final String id = m[1];
                ClickableSpan span = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        showProfileFromTag(id);
                    }
                };
                spanText.setSpan(span, idx, idx + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            Matcher hashMatcher = hashTag.matcher(spanText);
            while (hashMatcher.find()) {
                final String match = hashMatcher.group();
                ClickableSpan span = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
//                    if (mDelegate != null) {
//                        mDelegate.tagClicked(PeepViewListener.TAG_TYPE_HASH, match);
//                    }
                    }
                };
                int idx = spanText.toString().indexOf(match);
                spanText.setSpan(span, idx, idx + match.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mPostTextArea.setVisibility(View.VISIBLE);
            mPostText.setText(spanText);
            mPostTextCreator.setText(mPost.creatorName);
            mCache.fetchBitmap(mPost.creatorProfilePhotoUrl, mPostTextAvatar.getWidth(),
                    new ImageHandler(this, mPostTextAvatar, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }
    }

    private void showProfileFromTag(String tag) {
        User user = new User();
        user.uniqueID = tag;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }



    @Override
    public void avatarClicked(User creator) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, creator);
        startActivity(intent);
    }

    @Override
    public void tagClicked(int tagType, String tag) {
        if (tagType == CommentView.CommentViewDelegate.TAG_TYPE_USER) {
            showProfileFromTag(tag);
        }
    }

    @Override
    public void likeButtonClicked(Comment comment){
        if (comment.ownComment) {
            Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
            intent.putExtra(LikesActivity.EXTRA_POST, mPost);
            intent.putExtra(LikesActivity.EXTRA_COMMENT, comment);
            startActivity(intent);
        } else {
            if (comment.isLiked) {
                Comment.unlikeComment(mPost, comment, new CommentLikeHandler(this));
            } else {
                Comment.likeComment(mPost, comment, new CommentLikeHandler(this));
            }
        }
    }

    @Override
    public void noTagsPresent() {
        mUserTagAdapter.clear();
    }

    @Override
    public void userTagsFound(String tag) {
        mCurrentTag = tag;
        User.getAvailableTags(tag.substring(1), new UserTagDelegate());
    }

    @Override
    public void hashTagsFound(String tag) {

    }

    @Override
    public void fieldHasText(Boolean hasText) {
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
        private FullBleedPostActivity mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(FullBleedPostActivity view, ImageView iv, int type) {
            mPostView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
            Bitmap bmp = (Bitmap)msg.obj;
            if (mType == POST_IMAGE_TYPE_AVATAR) {
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mPostView.getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                mImageView.setImageDrawable(avatarDrawable);
            } else if (mType == POST_IMAGE_TYPE_IMAGE) {
                mImageView.setImageBitmap(bmp);
            }
        }
    }

    private class PostDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
        }

        private void process(Object object) {
            if (object != null && object instanceof Post) {
                if (mPost == null) {
                    mPost = (Post) object;
                    layoutPost();
                    Comment.fetchComments(mPost, new CommentDelegate());
                } else {
                    mPost = (Post) object;
                    layoutPost();
                }

            }
        }
    }

    private class CommentDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Comment> commentList = (ArrayList<Comment>)object;
                mAdapter.addAll(commentList);
                mAdapter.notifyDataSetChanged();
            }
        }

        private void update(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Comment> commentList = (ArrayList<Comment>)object;
                for (Comment c : commentList) {
                    int len = mAdapter.getCount();
                    for (int i = 0; i != len; ++i) {
                        Comment p = mAdapter.getItem(i);
                        if (c.uniqueId.equals(p.uniqueId)) {
                            mAdapter.remove(p);
                            mAdapter.insert(c, i);
                            break;
                        }
                    }
                    mAdapter.add(c);
                }
                mAdapter.notifyDataSetChanged();
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
            }
        }

    }

    private class CommentsListAdapter extends ArrayAdapter<Comment> {
        public CommentsListAdapter(Context c, List<Comment> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommentView view = (CommentView)convertView;
            if (convertView == null) {
                view = CommentView.inflate(parent);
            }
            view.setComment(getItem(position));
            view.setDelegate(FullBleedPostActivity.this);

            return view;
        }
    }

    private static class LikeHandler extends Handler
    {
        private FullBleedPostActivity mActivity;

        public LikeHandler(FullBleedPostActivity activity) {
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message message) {
            if (message.obj != null && message.obj instanceof  Post) {
                Post p = (Post)message.obj;
                mActivity.mPost = p;
                mActivity.layoutPost();
            }
        }
    }

    private static class CommentLikeHandler extends Handler
    {
        private FullBleedPostActivity mActivity;

        public CommentLikeHandler(FullBleedPostActivity activity) {
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message message) {
            if (message.obj != null && message.obj instanceof ArrayList) {
                ArrayList<Comment> comments= (ArrayList<Comment>)message.obj;
                mActivity.mAdapter.clear();
                mActivity.mAdapter.addAll(comments);
                mActivity.mAdapter.notifyDataSetChanged();
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
}
