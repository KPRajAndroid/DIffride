package com.diff.user.app.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.diff.app.R;
import com.diff.user.app.Helper.AppHelper;
import com.diff.user.app.Utils.MyButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class DocumentUploadActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 100;
    private static String TAG = "DocumentUp";
    public static int deviceHeight;
    public static int deviceWidth;
    @BindView(R.id.profile)
    ImageView profile;
    @BindView(R.id.passport_front)
    ImageView passportFront;
    @BindView(R.id.passport_back)
    ImageView passportBack;
    @BindView(R.id.submit)
    MyButton submit;
    Boolean isImageChanged = false;

    ImageView currentImageView;
    byte[] Picture = null;
    byte[] PassportFront = null;
    byte[] PassportBack = null;

    Boolean isPermissionGivenAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_upload);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
        ButterKnife.bind(this);
    }

    @OnClick({R.id.profile, R.id.passport_front, R.id.passport_back, R.id.submit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profile:
                currentImageView = profile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkStoragePermission()) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    } else {
                        goToImageIntent();
                    }
                } else {
                    goToImageIntent();
                }
                break;
            case R.id.passport_front:
                currentImageView = passportFront;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkStoragePermission()) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    } else {
                        goToImageIntent();
                    }
                } else {
                    goToImageIntent();
                }
                break;
            case R.id.passport_back:
                currentImageView = passportBack;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkStoragePermission()) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    } else {
                        goToImageIntent();
                    }
                } else {
                    goToImageIntent();
                }
                break;
            case R.id.submit:
                Intent returnIntent = new Intent();
                Picture = AppHelper.getFileDataFromDrawable(profile.getDrawable());
                PassportFront = AppHelper.getFileDataFromDrawable(passportFront.getDrawable());
                PassportBack = AppHelper.getFileDataFromDrawable(passportBack.getDrawable());

                if (Picture.length == 0) {
                    Toast.makeText(this, "Please Select Profile", Toast.LENGTH_SHORT).show();
                    return;
                }

                returnIntent.putExtra("picture", Picture);

                if (PassportFront != null) {
                    returnIntent.putExtra("passport_front", PassportFront);
                }
                if (PassportBack != null) {
                    returnIntent.putExtra("passport_back", PassportBack);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGivenAlready) {
                        goToImageIntent();
                    }
                }
            }
        }
    }

    public void goToImageIntent() {
        isPermissionGivenAlready = true;
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);*/
        EasyImage.openChooserWithGallery(DocumentUploadActivity.this,"Select Picture", 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            Bitmap bitmap = null;

            try {
                isImageChanged = true;
                Bitmap resizeImg = getBitmapFromUri(this, uri);
                if (resizeImg != null) {
                    Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg, AppHelper.getPath(this, uri));
                    currentImageView.setImageBitmap(reRotateImg);
                    if (currentImageView.getTag().equals("profile")) {
                        InputStream iStream = getContentResolver().openInputStream(uri);
                        Picture = getBytes(iStream);
                    }
                    if (currentImageView.getTag().equals("passport_front")) {
                        InputStream iStream = getContentResolver().openInputStream(uri);
                        PassportFront = getBytes(iStream);
                    }
                    if (currentImageView.getTag().equals("passport_back")) {
                        InputStream iStream = getContentResolver().openInputStream(uri);
                        PassportBack = getBytes(iStream);
                    }
                    //profile_Image.setImageBitmap(reRotateImg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/


        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if(imageFiles.size() > 0){
                    File imgFile = imageFiles.get(0);
                    if(imgFile.exists()) {
                        isImageChanged = true;
                        Glide.with(DocumentUploadActivity.this).load(Uri.fromFile(imgFile)).into(currentImageView);
                    }
                }
            }
        });
    }


    private static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        Log.e(TAG, "getBitmapFromUri: Resize uri" + uri);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        Log.e(TAG, "getBitmapFromUri: Height" + deviceHeight);
        Log.e(TAG, "getBitmapFromUri: width" + deviceWidth);
        int maxSize = Math.min(deviceHeight, deviceWidth);
        if (image != null) {
            Log.e(TAG, "getBitmapFromUri: Width" + image.getWidth());
            Log.e(TAG, "getBitmapFromUri: Height" + image.getHeight());
            int inWidth = image.getWidth();
            int inHeight = image.getHeight();
            int outWidth;
            int outHeight;
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            return Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.valid_image), Toast.LENGTH_SHORT).show();
            return null;
        }

    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
