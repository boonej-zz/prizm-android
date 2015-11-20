package co.higheraltitude.prizm.listeners;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserTagView;

/**
 * Created by boonej on 11/20/15.
 */
public class UserTagClickListener implements AdapterView.OnItemClickListener {

    private EditText mEditText;
    private Context mContext;
    private UserTagClickListenerDelegate mDelegate;

    public UserTagClickListener(Context context, EditText editText,
                                UserTagClickListenerDelegate delegate) {
        mEditText = editText;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserTagView tagView = (UserTagView)view;
        User u = tagView.getUser();
        Editable text = mEditText.getEditableText();
        String name = String.format("@%s ", u.name);
        String tag = String.format("@%s", u.uniqueID);
        Paint paint = new Paint();
        paint.setTextSize(mEditText.getTextSize());
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(mEditText.getCurrentTextColor());
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float baseline = -paint.ascent();
        float width = paint.measureText(name) + 0.5f;
        float height =  baseline + paint.descent() + 0.5f;
        Bitmap tagImage = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tagImage);
        canvas.drawText(name, 0, baseline, paint);
        ImageSpan imageSpan = new ImageSpan(mContext, tagImage);
        int start = text.toString().indexOf(mDelegate.currentTag());
        text.replace(start, start + mDelegate.currentTag().length(), tag);
        int spanEnd = start + tag.length();
        text.setSpan(imageSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.append(" ");
        mEditText.setSelection(text.length());
        mDelegate.tagSelected();
    }

    public interface UserTagClickListenerDelegate {
        String currentTag();
        void tagSelected(); //clear and stop typing tag
    }
}