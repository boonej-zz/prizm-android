package co.higheraltitude.prizm.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.higheraltitude.prizm.R;


public class RegistrationField extends LinearLayout {
    public static final int REGISTRATION_TYPE_TEXT = 0;
    public EditText editText;

    private String mTitleString = "Title";
    private int mType = REGISTRATION_TYPE_TEXT;
    private Context mContext;
    private TextView mTextView;
    private View view;
    private LayoutInflater inflater;

    public RegistrationField(Context context) {
        this(context, null);
    }

    public RegistrationField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegistrationField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        view = inflater.inflate(R.layout.registration_field, this, false);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RegistrationField, defStyle, 0);

        mTitleString = a.getString(
                R.styleable.RegistrationField_field_title);
        mType = a.getInteger(
                R.styleable.RegistrationField_field_type, 0);

        a.recycle();

        mTextView = (TextView)findViewById(R.id.registration_field_title);
        editText = (EditText)findViewById(R.id.registration_field_edittext);
        mTextView.setText(mTitleString);
    }




}
