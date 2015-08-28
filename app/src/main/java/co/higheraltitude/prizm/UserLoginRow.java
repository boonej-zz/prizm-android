package co.higheraltitude.prizm;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.InputType;


/**
 * TODO: document your custom view class.
 */
public class UserLoginRow extends RelativeLayout {

    public String textValue;
    private View view;
    private LayoutInflater inflater;
    private Context context;
    private TextView textView;
    private ImageView imageView;
    public EditText editText;

    public UserLoginRow(Context context) {
        this(context, null);
    }

    public UserLoginRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserLoginRow(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.user_login_row, this, true);
        imageView = (ImageView)findViewById(R.id.loginrow_icon_view);
        textView = (TextView)findViewById(R.id.loginrow_text_view);
        editText = (EditText)findViewById(R.id.loginrow_edit_text);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserLoginRow,
                0, 0
        );
        try {
            String text = a.getString(R.styleable.UserLoginRow_rowTitle);
            textView.setText(text);

            String imageType = a.getString(R.styleable.UserLoginRow_rowImage);
            imageView.setImageURI(null);
            Drawable drawable = null;
            if (imageType.equals("email")) {
                drawable = ContextCompat.getDrawable(context, R.drawable.username);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if (imageType.equals("password")) {
                drawable = ContextCompat.getDrawable(context, R.drawable.lockpassword);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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