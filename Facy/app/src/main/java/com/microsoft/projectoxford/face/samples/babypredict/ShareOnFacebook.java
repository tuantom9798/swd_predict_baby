package com.microsoft.projectoxford.face.samples.babypredict;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;
import java.util.List;

public class ShareOnFacebook {
    private ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
    private Activity babyActivity;

    public ShareOnFacebook(ArrayList<Bitmap> bitmapArray, Activity babyActivity) {
        this.bitmapArray = bitmapArray;
        this.babyActivity = babyActivity;
    }

    public void share() {
        List<SharePhoto> photos = new ArrayList<SharePhoto>();

        for (int i = 0; i < bitmapArray.size(); i++) {
            if(bitmapArray.get(i) != null) {
                photos.add((new SharePhoto.Builder().setBitmap(
                        bitmapArray.get(i)
                ).build()));
            }
        }
        SharePhotoContent content = new SharePhotoContent.Builder()
                .setPhotos(photos)
                .build();
        ShareDialog shareDialog;
        if (ShareDialog.canShow(SharePhotoContent.class)) {
            shareDialog = new ShareDialog(this.babyActivity);
            shareDialog.show(content);
        }
    }
}
