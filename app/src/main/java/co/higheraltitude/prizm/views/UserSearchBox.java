package co.higheraltitude.prizm.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import co.higheraltitude.prizm.R;


/**
 * Created by boonej on 9/30/15.
 */
public class UserSearchBox extends LinearLayout {

    private EditText mEditText;
    private ImageButton mVoiceButton;
    private UserSearchListener mSearchListener;

    public UserSearchBox(Context context){
        this(context, null);
    }

    public UserSearchBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserSearchBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.user_search_box, this, true);
        mEditText = (EditText)view.findViewById(R.id.search_users);
        mVoiceButton = (ImageButton)view.findViewById(R.id.voice_search_button);
        startListening();
    }

    private void startListening() {
        mEditText.addTextChangedListener(new SearchInputWatcher());
        mVoiceButton.setOnClickListener(new VoiceClickListener());
    }

    public void setSearchListener(UserSearchListener listener) {
        mSearchListener = listener;
    }

    public Editable getEditable() {
        return mEditText.getEditableText();
    }

    public void setText(String text) {
        mEditText.setText(text);
    }

    private class SearchInputWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mSearchListener != null) {
                mSearchListener.searchTextChanged(s.toString());
            }
        }

    }

    private class VoiceClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mSearchListener != null) {
                mSearchListener.voiceButtonTapped();
            }
        }
    }

    public interface UserSearchListener {
        public void searchTextChanged(String s);
        public void voiceButtonTapped();
    }



}
