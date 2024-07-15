package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget

fun Context.displayCircularImageFromUrl(imageUrl: String?, imageView: ImageView) {
    Glide.with(this)
        .asBitmap()
        .apply(RequestOptions().centerCrop().dontAnimate())
        .load(imageUrl)
        .into(object : BitmapImageViewTarget(imageView) {
            override fun setResource(resource: Bitmap?) {
                val circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resources, resource)
                circularBitmapDrawable.isCircular = true
                imageView.setImageDrawable(circularBitmapDrawable)
            }
        })
}
