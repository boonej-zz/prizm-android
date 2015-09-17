package co.higheraltitude.prizm.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import co.higheraltitude.prizm.R;


/**
 * TODO: document your custom view class.
 */
public class UserLoginRow extends RelativeLayout {

    public String textValue;
    private View view;
    private LayoutInflater inflater;
    private ImageView imageView;
    public EditText editText;

    private UserLoginRow row;

    public UserLoginRow(Context context) {
        this(context, null);
    }

    public UserLoginRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserLoginRow(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        row = this;
        inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.user_login_row, this, true);
        imageView = (ImageView)findViewById(R.id.loginrow_icon_view);
        editText = (EditText)findViewById(R.id.loginrow_edit_text);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserLoginRow,
                0, 0
        );
        try {
            String text = a.getString(R.styleable.UserLoginRow_rowTitle);
            editText.setHint(text);

            String imageType = a.getString(R.styleable.UserLoginRow_rowImage);
            imageView.setImageURI(null);
            Drawable drawable = null;
            if (imageType.equals("email")) {
                drawable = ContextCompat.getDrawable(context, R.drawable.user_icon);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if (imageType.equals("password")) {
                drawable = ContextCompat.getDrawable(context, R.drawable.pasword_icon);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setTypeface(Typeface.DEFAULT);
            }
            imageView.setImageDrawable(drawable);

        } catch(Exception e) {
            Log.d("DEBUG", e.getLocalizedMessage());
        } finally {
            a.recycle();
        }
    }

    public String getText(){
        return editText.getText().toString();
    }
}