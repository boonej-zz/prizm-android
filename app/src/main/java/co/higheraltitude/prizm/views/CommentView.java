package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Comment;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;

/**
 * TODO: document your custom view class.
 */
public class CommentView extends RelativeLayout {

    private ImageView mAvatarView;
    private TextView mCreatorName;
    private TextView mCommentText;
    private ImageView mCommentLikeIcon;
    private TextView mCommentLikeText;
    private TextView mCommentTimeAgo;

    private String mInstanceId;
    private Comment mComment;

    private static Pattern userTag;
    private static Pattern hashTag;
    private PrizmDiskCache mCache;

    private CommentViewDelegate mDelegate;


    public static CommentView inflate(ViewGroup parent) {
        CommentView commentView = (CommentView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_view, parent, false);
        return commentView;
    }

    public CommentView(Context context){
        this(context, null);
    }

    public CommentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.group_view_children, this, true);
    }

    public void setDelegate(CommentViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setComment(Comment comment) {
        mComment = comment;
        mCache = PrizmDiskCache.getInstance(getContext());
        if (userTag == null) {
            userTag = Pattern.compile("@\\([^)]+\\)");
        }

        if (hashTag == null) {
            hashTag = Pattern.compile("#[\\S]+");
        }
        PrizmCache cache = PrizmCache.getInstance();
        mInstanceId = mComment.uniqueId;
        setViews();
        String text = mComment.text;
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
                    if (mDelegate != null) {
                        mDelegate.tagClicked(CommentViewDelegate.TAG_TYPE_USER, id);
                    }
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
                    if (mDelegate != null) {
                        mDelegate.tagClicked(CommentViewDelegate.TAG_TYPE_HASH, match);
                    }
                }
            };
            int idx = spanText.toString().indexOf(match);
            spanText.setSpan(span, idx, idx + match.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mCommentText.setText(spanText);
        mCommentText.setMovementMethod(LinkMovementMethod.getInstance());
        mCreatorName.setText(mComment.creatorName);
        mAvatarView.setImageResource(R.drawable.user_missing_avatar);

        mCommentTimeAgo.setText(String.format("%s ago - Like", mComment.timeSince));
        if (mComment.likesCount > 0) {
            mCommentLikeText.setText(String.valueOf(mComment.likesCount));
            mCommentLikeText.setVisibility(VISIBLE);
        } else {
            mCommentLikeText.setVisibility(INVISIBLE);
        }


        ImageHandler ih = new ImageHandler(this, mAvatarView, mInstanceId, ImageHandler.PEEP_IMAGE_TYPE_AVATAR);
        mCache.fetchBitmap(mComment.creatorProfilePhotoUrl, 125, ih);
//
//        avatarView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                User u = new User();
//                u.uniqueID = mPeep.creatorId;
//                u.name = mPeep.creatorName;
//                u.type = mPeep.creatorType;
//                u.subtype = mPeep.creatorSubtype;
//                u.profilePhotoURL = mPeep.creatorProfilePhotoUrl;
//                mDelegate.avatarClicked(u);
//            }
//        });

    }

    public Comment getComment() {
        return mComment;
    }

    private void setViews() {
        mAvatarView = (ImageView) findViewById(R.id.comment_avatar);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    User user = new User();
                    user.uniqueID = mComment.creatorId;
                    user.type = mComment.creatorType;
                    user.subtype = mComment.creatorSubtype;
                    user.profilePhotoURL = mComment.creatorProfilePhotoUrl;
                    mDelegate.avatarClicked(user);
                }
            }
        });
        mCreatorName = (TextView)findViewById(R.id.comment_creator_name);
        mCommentText = (TextView)findViewById(R.id.comment_text);
        mCommentLikeIcon = (ImageView)findViewById(R.id.comment_like_image);
        mCommentLikeIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.likeButtonClicked(mComment);
                }
            }
        });
        mCommentLikeText = (TextView)findViewById(R.id.comment_like_count);
        mCommentLikeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.likeButtonClicked(mComment);
                }
            }
        });
        mCommentTimeAgo = (TextView)findViewById(R.id.comment_time);
        mCommentTimeAgo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.likeButtonClicked(mComment);
                }
            }
        });
        if (mComment.isLiked) {
            mCommentLikeIcon.setImageResource(R.drawable.like_comment_selected_icon);
        } else {
            mCommentLikeIcon.setImageResource(R.drawable.like_commentsm_icon);
        }


    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private CommentView mPeepView;
        private ImageView mImageView;
        private int mType;
        public static int PEEP_IMAGE_TYPE_AVATAR = 0;
        public static int PEEP_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(CommentView view, ImageView iv, String id, int type) {
            mInstanceId = id;
            mPeepView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
            if (mPeepView.mInstanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                if (mType == PEEP_IMAGE_TYPE_AVATAR) {
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mPeepView.getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                    mImageView.setImageDrawable(avatarDrawable);
                } else if (mType == PEEP_IMAGE_TYPE_IMAGE) {
                    mImageView.setImageBitmap(bmp);
                }
            }
        }
    }

    private static class LikeHandler extends Handler {
        private CommentView mPeepView;

        public LikeHandler(CommentView view, String id) {
            mPeepView = view;
        }

        public void handleMessage(Message msg) {
            Peep mPeep = (Peep)msg.obj;

        }
    }


    public interface CommentViewDelegate {
        int TAG_TYPE_USER = 1;
        int TAG_TYPE_HASH = 0;

        void avatarClicked(User creator);
        void tagClicked(int tagType, String tag);
        void likeButtonClicked(Comment comment);
    }

}
