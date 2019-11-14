package com.microsoft.projectoxford.face.samples.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Accessory;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FacialHair;
import com.microsoft.projectoxford.face.contract.Hair;
import com.microsoft.projectoxford.face.contract.HeadPose;
import com.microsoft.projectoxford.face.contract.Makeup;
import com.microsoft.projectoxford.face.samples.R;
import com.microsoft.projectoxford.face.samples.babypredict.ConnectAPICustomBabyPredict;
import com.microsoft.projectoxford.face.samples.helper.ImageHelper;
import com.microsoft.projectoxford.face.samples.helper.SampleApp;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BabyPredictActivity extends AppCompatActivity {

    // Background task of Baby Predict.
    private class BabyPredict extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.FacialHair,
                                FaceServiceClient.FaceAttributeType.Emotion,
                                FaceServiceClient.FaceAttributeType.HeadPose,
                                FaceServiceClient.FaceAttributeType.Accessories,
                                FaceServiceClient.FaceAttributeType.Blur,
                                FaceServiceClient.FaceAttributeType.Exposure,
                                FaceServiceClient.FaceAttributeType.Hair,
                                FaceServiceClient.FaceAttributeType.Makeup,
                                FaceServiceClient.FaceAttributeType.Noise,
                                FaceServiceClient.FaceAttributeType.Occlusion
                        });


            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            if (check == 0) {
                setUiAfterDetection(result, mSucceed, 0);
            } else if (check == 1) {
                setUiAfterDetection(result, mSucceed, 1);
            }
        }
    }

    // check number image
    private int check = 0;

    //Gender of baby wanted
    private String genderOfBaby = "boy";

    //Skin of baby wanted
    private String skinOfBaby = "light";

    ListView listFaceDetected;
    public ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();


    // Check two image input are detected face
    private boolean foundFace1 = false;
    private boolean foundFace2 = false;

    // Check gender must difference
    private int genderFace1 = 0;

    // Url Image API
    private String urlImage = "";

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE_1 = 0;

    private static final int REQUEST_SELECT_IMAGE_2 = 1;

    // The URI of the image selected to detect.
    private Uri mImageUri1;
    private Uri mImageUri2;

    // The image selected to detect.
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;

    // Progress dialog popped up when communicating with server.
    ProgressDialog mProgressDialog;


    // When the activity is created, set all the member variables to initial state.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_predict);
        listFaceDetected = (ListView) findViewById(R.id.list_detected_faces);
//        listImageBaby = (ListView) findViewById(R.id.list_image_baby);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));

        // Disable button "share" and "show baby" as the image to detect is not selected.
        setShowAndShareButtonStatus(false);

    }

    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("ImageUri1", mImageUri1);
        outState.putParcelable("ImageUri2", mImageUri2);
    }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mImageUri1 = savedInstanceState.getParcelable("ImageUri1");
        if (mImageUri1 != null) {
            mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri1, getContentResolver());
        }

        mImageUri2 = savedInstanceState.getParcelable("ImageUri2");
        if (mImageUri2 != null) {
            mBitmap2 = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri2, getContentResolver());
        }
    }


    // Called when the "Select Image" button is clicked.
    public void loadImage1(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_1);
    }

    public void loadImage2(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_2);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE_1:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri1 = null;
                    mBitmap1 = null;
                    mImageUri1 = data.getData();
                    mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri1, getContentResolver());
                    if (mBitmap1 != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.image_0);
                        imageView.setImageBitmap(mBitmap1);

                    }
                    // Clear the information panel.
                    setInfo("");

                    // Enable button "show baby" as the image to detect is not selected.
                    setShowButtonsEnabledStatus(true);
                }
                break;
            case REQUEST_SELECT_IMAGE_2:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri2 = null;
                    mBitmap2 = null;
                    mImageUri2 = data.getData();
                    mBitmap2 = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri2, getContentResolver());
                    if (mBitmap2 != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.image_1);
                        imageView.setImageBitmap(mBitmap2);

                    }
                    // Clear the information panel.
                    setInfo("");

                    // Enable button "show baby" as the image to detect is not selected.
                    setShowButtonsEnabledStatus(true);
                }
                break;
            default:
                break;
        }
    }

    // Called when the "showBaby" button is clicked.
    public void showBaby(View view) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        mBitmap1.compress(Bitmap.CompressFormat.JPEG, 100, output1);
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(output1.toByteArray());

        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        mBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, output2);
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(output2.toByteArray());

        // Start a background task to detect faces in the image.
        new BabyPredictActivity.BabyPredict().execute(inputStream1);
        new BabyPredictActivity.BabyPredict().execute(inputStream2);


        // Prevent button click during detecting.
        setAllButtonsEnabledStatus(false);
    }

    // Called when the "share" button is clicked.
    public void share(View view) {
        if (urlImage != "") {
            try {
//                Intent mIntentFacebook = new Intent();
//                mIntentFacebook.setClassName("com.facebook.katana",
//                        "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias");
//                mIntentFacebook.setAction("android.intent.action.SEND");
//                mIntentFacebook.setType("text/plain");
//                mIntentFacebook.putExtra("android.intent.extra.TEXT", urlImage);
//                startActivity(mIntentFacebook);
                List<SharePhoto> photos = new ArrayList<SharePhoto>();

                for (int i = 0; i < bitmapArray.size(); i++) {
                    photos.add((new SharePhoto.Builder().setBitmap(
                            bitmapArray.get(i)
                    ).build()));
                }

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .setPhotos(photos)
                        .build();
                Log.i("as", bitmapArray.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Intent mIntentFacebookBrowser = new Intent(Intent.ACTION_SEND);
                String mStringURL = "https://www.facebook.com/sharer/sharer.php?u=" + urlImage;
                mIntentFacebookBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(mStringURL));
                startActivity(mIntentFacebookBrowser);
            }
        }
    }


    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    // Set whether the buttons are enabled.
    private void setShowAndShareButtonStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        Button share = (Button) findViewById(R.id.share);

        showBaby.setEnabled(isEnabled);
        share.setEnabled(isEnabled);
    }

    // Set whether the show button is enabled.
    private void setShowButtonsEnabledStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        showBaby.setEnabled(isEnabled);
    }

    // Set whether the share button is enabled.
    private void setShareButtonsEnabledStatus(boolean isEnabled) {
        Button share = (Button) findViewById(R.id.share);
        share.setEnabled(isEnabled);
    }

    // Set whether the buttons are enabled.
    private void setAllButtonsEnabledStatus(boolean isEnabled) {
        Button showBaby = (Button) findViewById(R.id.showBaby);
        Button share = (Button) findViewById(R.id.share);
        Button loadImg1 = (Button) findViewById(R.id.loadImg1);
        Button loadImg2 = (Button) findViewById(R.id.loadImg2);

        showBaby.setEnabled(isEnabled);
        share.setEnabled(isEnabled);
        loadImg1.setEnabled(isEnabled);
        loadImg2.setEnabled(isEnabled);
    }

    Face[] face1, face2;
    int count = 0;

    // Show the result on screen when detection is done.
    private void setUiAfterDetection(Face[] result, boolean succeed, int checkImage) {

        // Detection is done, hide the progress dialog.
        mProgressDialog.dismiss();

        // Enable all the buttons.
        setAllButtonsEnabledStatus(true);

        // Disable button "detect" as the image has already been detected.
        setShareButtonsEnabledStatus(false);
        setShowButtonsEnabledStatus(false);
        if (checkImage == 0) {
            if (succeed) {
                check = 1;
                // The information about the detection result.
                if (result.length != 0) {
                    face1 = result;
                    foundFace1 = true;
                    // Show the detected faces on original image.
                    ImageView imageView = (ImageView) findViewById(R.id.image_0);
                    imageView.setImageBitmap(ImageHelper.drawFaceRectanglesOnBitmap(
                            mBitmap1, result, true));
                    genderFace1 = face1[0].faceAttributes.gender.startsWith("male") ? 0 : 1;

                }
            }
        } else {
            if (succeed) {
                // The information about the detection result.
                check = 0;
                if (result.length != 0) {
                    face2 = result;
                    foundFace2 = true;
                    // Show the detected faces on original image.
                    ImageView imageView = (ImageView) findViewById(R.id.image_1);
                    imageView.setImageBitmap(ImageHelper.drawFaceRectanglesOnBitmap(
                            mBitmap2, result, true));

                }
            }

            checkInputImage(face1, face2);
            count = 0;


        }
    }

    private void checkInputImage(Face[] face1, Face[] face2) {
        String detectionResult;
        if (!foundFace1 || !foundFace2) {
            setShareButtonsEnabledStatus(false);
            FaceListAdapter faceListAdapter = new FaceListAdapter(null);
            // Show the detailed list of detected faces.
            listFaceDetected.setAdapter(faceListAdapter);
            foundFace1 = foundFace2 = false;
            genderFace1 = 0;
            detectionResult = getString(R.string.not_found_baby);
            setInfo(detectionResult);
            return;
        } else if (face1.length > 1) {
            setShareButtonsEnabledStatus(false);
            FaceListAdapter faceListAdapter = new FaceListAdapter(null);
            // Show the detailed list of detected faces.
            listFaceDetected.setAdapter(faceListAdapter);
            foundFace1 = foundFace2 = false;
            genderFace1 = 0;
            detectionResult = getString(R.string.check_image1);
            setInfo(detectionResult);
            return;
        } else if (!checkGender(face2)) {
            setShareButtonsEnabledStatus(false);
            FaceListAdapter faceListAdapter = new FaceListAdapter(null);
            // Show the detailed list of detected faces.
            listFaceDetected.setAdapter(faceListAdapter);
            foundFace1 = foundFace2 = false;
            genderFace1 = 0;
            detectionResult = getString(R.string.same_gender);
            setInfo(detectionResult);
            return;
        } else {
            setShareButtonsEnabledStatus(true);
            foundFace1 = foundFace2 = false;
            FaceListAdapter faceListAdapter = new FaceListAdapter(face2);
            // Show the detailed list of detected faces.
            listFaceDetected.setAdapter(faceListAdapter);


//             getImageBaby();
            detectionResult = getString(R.string.found_baby) + " " + count + " Baby" +
                    (count != 1 ? "s" : "");
            setInfo(detectionResult);
            genderFace1 = 0;
            return;
        }
    }


    private boolean checkGender(Face[] face2) {
        for (Face f2 : face2) {
            if (f2.faceAttributes.gender.startsWith("male") && genderFace1 == 1
                    || f2.faceAttributes.gender.startsWith("female") && genderFace1 == 0) {
                return true;
            }
        }

        return false;
    }

    public void onRadioButtonGenderClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_male:
                if (checked)
                    genderOfBaby = "boy";
                break;
            case R.id.radioButton_female:
                if (checked)
                    genderOfBaby = "girl";
                break;
        }
    }

    public void onRadioButtonSkinClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_light:
                if (checked)
                    skinOfBaby = "light";
                break;
            case R.id.radioButton_dark:
                if (checked)
                    skinOfBaby = "dark";
                break;
        }
    }

    // The adapter of the GridView which contains the details of the detected faces.
    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces2;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;
        int index = 0;

        // Initialize with detection result.
        FaceListAdapter(Face[] face2) {
            faces2 = new ArrayList<>();
            faceThumbnails = new ArrayList<>();

            if (face2 != null) {
                faces2 = Arrays.asList(face2);
                if (genderFace1 == 0) {
                    for (Face face : faces2) {
                        try {
                            // Crop face thumbnail with five main landmarks drawn from original image.
                            if (face.faceAttributes.gender.startsWith("female")) {
                                faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                        mBitmap2, face.faceRectangle));
                                count++;
                            }
                        } catch (IOException e) {
                            // Show the exception when generating face thumbnail fails.
                            setInfo(e.getMessage());
                        }
                    }
                } else {
                    for (Face face : faces2) {
                        try {
                            // Crop face thumbnail with five main landmarks drawn from original image.
                            if (face.faceAttributes.gender.startsWith("male")) {
                                faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                        mBitmap2, face.faceRectangle));
                                count++;
                            }
                        } catch (IOException e) {
                            // Show the exception when generating face thumbnail fails.
                            setInfo(e.getMessage());
                        }
                    }
                }
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return faces2.size();
        }

        @Override
        public Object getItem(int position) {
            return faces2.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_image_face, parent, false);
            }
            while (index < faceThumbnails.size()) {
                convertView.setId(index);

                // Show the face thumbnail.
                ((ImageView) convertView.findViewById(R.id.face_thumbnail)).setImageBitmap(
                        faceThumbnails.get(index));
                urlImage = new ConnectAPICustomBabyPredict().connectAPI(urlImage, genderOfBaby, skinOfBaby);
                try {
                    URL url = new URL(urlImage);
                    Log.i("URL", url.toString());
                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    bitmapArray.add(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Picasso
                        .get()
                        .load(urlImage)
                        .fit() // will explain later
                        .into((ImageView) convertView.findViewById(R.id.face_thumbnai2));
                index++;
                break;
            }

            return convertView;
        }


    }
}
