package com.thekirankumar.youtubeauto.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TopAlignedImageView extends ImageView {
    private Matrix mMatrix;
    private boolean mHasFrame;

    @SuppressWarnings("UnusedDeclaration")
    public TopAlignedImageView(Context context) {
        this(context, null, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TopAlignedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TopAlignedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHasFrame = false;
        mMatrix = new Matrix();
        // we have to use own matrix because:
        // ImageView.setImageMatrix(Matrix matrix) will not call
        // configureBounds(); invalidate(); because we will operate on ImageView object
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b)
    {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            mHasFrame = true;
            // we do not want to call this method if nothing changed
            setupScaleMatrix(r-l, b-t);
        }
        return changed;
    }

    private void setupScaleMatrix(int width, int height) {
        if (!mHasFrame) {
            // we have to ensure that we already have frame
            // called and have width and height
            return;
        }
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            // we have to check if drawable is null because
            // when not initialized at startup drawable we can
            // rise NullPointerException
            return;
        }
        Matrix matrix = mMatrix;
        final int intrinsicWidth = drawable.getIntrinsicWidth();
        final int intrinsicHeight = drawable.getIntrinsicHeight();

        float factorWidth = width/(float) intrinsicWidth;
        float factorHeight = height/(float) intrinsicHeight;
        float factor = Math.max(factorHeight, factorWidth);

        // there magic happen and can be adjusted to current
        // needs
        matrix.setTranslate(-intrinsicWidth/2.0f, 0);
        matrix.postScale(factor, factor, 0, 0);
        matrix.postTranslate(width/2.0f, 0);
        setImageMatrix(matrix);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight());
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight());
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight());
    }

    // We do not have to overide setImageBitmap because it calls 
    // setImageDrawable method

}