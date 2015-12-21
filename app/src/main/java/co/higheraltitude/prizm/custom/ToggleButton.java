package co.higheraltitude.prizm.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by boonej on 12/8/15.
 */
public class ToggleButton extends Button {

    private Boolean mIsToggled = false;

    public ToggleButton(Context context) {
        super(context);
    }

    public ToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ToggleButton(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    public boolean performClick() {
        setSelected(!isSelected());
        return super.performClick();
    }
}
