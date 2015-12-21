package co.higheraltitude.prizm.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by boonej on 11/20/15.
 */
public class TagWatcher implements TextWatcher {
    public static final int TAG_TYPE_HASH = 0;
    public static final int TAG_TYPE_USER = 1;
    private int mLastLength;
    private EditText mEditText;
    private ArrayList<ImageSpan> mDeletedSpans = new ArrayList<>();
    private boolean mTypingTag = false;
    private int mTagStart;
    private int mTagType;
    private String mCurrentTag = "";

    private TagWatcherDelegate mDelegate;


    public TagWatcher(EditText editText, TagWatcherDelegate delegate) {
        mEditText = editText;
        mDelegate = delegate;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        int length = s.length();
        if (length < mLastLength) {
            Editable text = mEditText.getEditableText();
            int end = start + count;
            ImageSpan[] list = text.getSpans(start, end, ImageSpan.class);
            if (length < mLastLength) {
                for (ImageSpan span : list) {
                    int spanStart = text.getSpanStart(span);
                    int spanEnd = text.getSpanEnd(span);
                    if ((spanStart < end) && (spanEnd >= start))
                        mDeletedSpans.add(span);
                }
            }
        }
        mLastLength = length;
        if (s.length() > start) {
            if (s.charAt(start) == '@') {
                mTypingTag = true;
                mTagStart = start;
                mTagType = TAG_TYPE_USER;
            }
            if (s.charAt(start) == '#') {
                mTypingTag = true;
                mTagStart = start;
                mTagType = TAG_TYPE_HASH;
            }
            if (s.charAt(start) == ' ') {
                mTypingTag = false;
                mTagStart = -1;
                mCurrentTag = "";
            }
            if (s.charAt(s.length() - 1) == ' ') {
                mTypingTag = false;
                mTagStart = -1;
                mCurrentTag = "";
            }
        }
        if (mTypingTag) {
            if (start < s.length()) {
                mCurrentTag = mCurrentTag + s.charAt(start);
            } else {
                mCurrentTag = mCurrentTag.substring(0, mCurrentTag.length() - 1);
            }
            if (mCurrentTag.isEmpty()) {
                mTypingTag = false;
                mTagStart = - 1;
                mDelegate.noTagsPresent();
            }
            if (mTypingTag) {
                if (mTagType == TAG_TYPE_USER) {
                    mDelegate.userTagsFound(mCurrentTag);
                } else if (mTagType == TAG_TYPE_HASH) {
                    mDelegate.hashTagsFound(mCurrentTag);
                }
            }
        } else {
            mDelegate.noTagsPresent();
        }
    }
    @Override
    public void afterTextChanged(Editable s) {
        boolean hasText = mEditText.getText().length() > 0;
        mDelegate.fieldHasText(hasText);

        Editable text = mEditText.getEditableText();
        for (ImageSpan span : mDeletedSpans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            text.removeSpan(span);
            if (start != end) {
                text.delete(start, end);
            }
        }
        mDeletedSpans.clear();
    }

    public interface TagWatcherDelegate {

        void noTagsPresent();  // Clear adapters with this method
        void userTagsFound(String tag);
        void hashTagsFound(String tag);
        void fieldHasText(Boolean hasText);
    }
}





