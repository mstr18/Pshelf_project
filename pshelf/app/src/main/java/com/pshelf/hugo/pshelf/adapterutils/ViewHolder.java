package com.pshelf.hugo.pshelf.adapterutils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.pshelf.hugo.pshelf.R;
import com.pshelf.hugo.pshelf.imageutils.ImageUtils;

/**
 * Created by Hugo Frederico on 27/03/2016.
 */
public class ViewHolder {

    public ImageView thumbImg;

    ViewHolder(View base) {

        thumbImg = (ImageView) base.findViewById(R.id.img);
    }
}
