package com.microsoft.projectoxford.face.samples.babypredict;

import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ConnectAPICustomBabyPredict {
    public String connectAPI(String urlImage, String genderOfBaby, String skinOfBaby) {
        urlImage = "https://babypredict.herokuapp.com/baby?" +
                "gender=" + genderOfBaby
                + "&skin=" + skinOfBaby;
        return  urlImage;
    }
}
