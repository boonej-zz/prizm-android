package co.higheraltitude.prizm.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by boonej on 12/8/15.
 */
public class ToggleImageButton extends ImageButton {

    public ToggleImageButton(Context context) {
        super(context);
    }

    public ToggleImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ToggleImageButton(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    public boolean performClick() {
        setSelected(!isSelected());
        return super.performClick();
    }
}
