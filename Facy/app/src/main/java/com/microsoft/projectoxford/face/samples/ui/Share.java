package com.microsoft.projectoxford.face.samples.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Share extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Intent share(String urlImage) {
        if(urlImage != "") {
            try {
                Intent mIntentFacebook = new Intent();
                mIntentFacebook.setClassName("com.facebook.katana",
                        "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias");
                mIntentFacebook.setAction("android.intent.action.SEND");
                mIntentFacebook.setType("text/plain");
                mIntentFacebook.putExtra("android.intent.extra.TEXT", urlImage);
                return mIntentFacebook;
            } catch (Exception e) {
                e.printStackTrace();
                Intent mIntentFacebookBrowser = new Intent(Intent.ACTION_SEND);
                String mStringURL = "https://www.facebook.com/sharer/sharer.php?u=" + urlImage;
                mIntentFacebookBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(mStringURL));
                return mIntentFacebookBrowser;
            }
        }
        return null;
    }
}
