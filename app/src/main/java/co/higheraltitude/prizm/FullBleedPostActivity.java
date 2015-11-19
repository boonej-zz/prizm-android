package co.higheraltitude.prizm;

import android.app.ActionBar;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
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
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Comment;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.CommentView;

public class FullBleedPostActivity extends AppCompatActivity implements CommentView.CommentViewDelegate {

    public static final String EXTRA_POST = "extra_post";

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
    private View mCommentButton;
    private PrizmDiskCache mCache;
    private TextView mPostTextCreator;
    private TextView mPostText;
    private View mPostTextArea;
    private ImageView mPostTextAvatar;
    private ListView mCommentsList;
    private CommentsListAdapter mAdapter;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_bleed_post);
        configureActionBar();
        mCache = PrizmDiskCache.getInstance(getApplicationContext());
        mPost = getIntent().getParcelableExtra(EXTRA_POST);
        configureViews();
        layoutPost();
        configureAdapter();
        Comment.fetchComments(mPost, new CommentDelegate());
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
        mCommentsList = (ListView)findViewById(R.id.comments_list);
        mScrollView = (ScrollView)findViewById(R.id.comments_scroll_view);


    }

    private void configureAdapter()
    {
        mAdapter = new CommentsListAdapter(getApplicationContext(), new ArrayList<Comment>());
        mCommentsList.setAdapter(mAdapter);
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
        if (comment.isLiked) {
            Comment.unlikeComment(mPost, comment, new CommentLikeHandler(this));
        } else {
            Comment.likeComment(mPost, comment, new CommentLikeHandler(this));
        }
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
}
