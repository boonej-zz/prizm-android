package co.higheraltitude.prizm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import co.higheraltitude.prizm.R;

/**
 * Created by boonej on 9/24/15.
 */
public class FooterButtonView extends RelativeLayout {

    private Button mButton;


    public static FooterButtonView inflate(ViewGroup parent) {
        FooterButtonView itemView = (FooterButtonView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.footer_button_view, parent, false);
        return itemView;
    }

    public FooterButtonView(Context context){
        this(context, null);
    }

    public FooterButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mButton = (Button)findViewById(R.id.footer_button);
    }


    public void setOnClickListener(OnClickListener listener) {
        setViews();
        mButton.setOnClickListener(listener);
    }

}
