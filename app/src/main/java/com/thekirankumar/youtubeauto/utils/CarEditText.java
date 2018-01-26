package com.thekirankumar.youtubeauto.utils;

import android.content.Context;
import android.support.car.input.CarRestrictedEditText;
import android.util.AttributeSet;

import com.google.android.gms.car.input.CarEditable;
import com.google.android.gms.car.input.CarEditableListener;

/**
 * Created by kiran.kumar on 26/01/18.
 */

public class CarEditText extends CarRestrictedEditText implements CarEditable {
    public CarEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void setCarEditableListener(final CarEditableListener carEditableListener) {
        super.setCarEditableListener(new android.support.car.input.CarEditableListener() {
            @Override
            public void onUpdateSelection(int i, int i1, int i2, int i3) {
                carEditableListener.onUpdateSelection(i, i1, i2, i3);
            }
        });
    }

}
