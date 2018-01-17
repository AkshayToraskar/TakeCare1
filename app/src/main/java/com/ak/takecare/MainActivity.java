package com.ak.takecare;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ak.takecare.adapter.ImageHistoryAdapter;
import com.ak.takecare.model.ImageData;
import com.ak.takecare.util.BitmapUtils;
import com.ak.takecare.util.CameraUtil;
import com.ak.takecare.util.ImagePath_MarshMallow;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
//import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvImage)
    RecyclerView rvImages;

    public static int SETTING = 100;
    public static int CAPTURE_IMAGE = 101;
    public static int PICK_IMAGE = 102;

    BottomSheetDialog bottomSheetDialog;
    ImageHistoryAdapter imageHistoryAdapter;
    List<ImageData> imageDataList;
    Realm realm;


    public static String mPhotoPath;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        View viewSheet = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(viewSheet);
        realm = Realm.getDefaultInstance();

        imageDataList = new ArrayList<>();


        imageHistoryAdapter = new ImageHistoryAdapter(this, imageDataList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvImages.setLayoutManager(mLayoutManager);
        rvImages.setItemAnimator(new DefaultItemAnimator());
        rvImages.setAdapter(imageHistoryAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        imageDataList.clear();
        imageDataList.addAll(realm.where(ImageData.class).findAll());
        imageHistoryAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        bottomSheetDialog.dismiss();

        if (resultCode == RESULT_OK) {


            int maxWidth=100;
            int maxHeight=100;


            if (requestCode == CAPTURE_IMAGE) {
                // Toast.makeText(this, "capture", Toast.LENGTH_SHORT).show();
                String getImageUrl;

                //Check if device SDK is greater than 22 then we get the actual image path via below method
                if (Build.VERSION.SDK_INT > 22)
                    getImageUrl = ImagePath_MarshMallow.getPath(MainActivity.this, fileUri);
                else
                    //else we will get path directly
                    getImageUrl = fileUri.getPath();

                Log.v(TAG, " image path" + getImageUrl);

                Log.v(TAG,"image path1"+mPhotoPath);






               /* UCrop.of(fileUri, fileUri)
                        .withAspectRatio(16, 9)
                        .withMaxResultSize(maxWidth, maxHeight)
                        .start(this);*/


                editImage(mPhotoPath);
               // editImage(mPhotoPath);


            } else if (requestCode == PICK_IMAGE) {

                String getImageUrl = ImagePath_MarshMallow.getPath(MainActivity.this, data.getData());

                // Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show();
                Uri uri = data.getData();
                editImage(getImageUrl);

               /* UCrop.of(uri, fileUri)
                        .withAspectRatio(16, 9)
                        .withMaxResultSize(maxWidth, maxHeight)
                        .start(this);*/



            }/*else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                String imagePath=ImagePath_MarshMallow.getPath(MainActivity.this,resultUri);
                editImage(imagePath);

            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
            }*/




        }
    }


    public void onBtnClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.fab_capture:
                bottomSheetDialog.show();
                break;

            case R.id.btn_camera:
                getPermission();
                break;

            case R.id.btn_gallery:
                getGalleryImage();
                break;
        }


    }

    //capture image
    public void getPermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            if (isDeviceSupportCamera()) {
                                openCamera();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).onSameThread()
                .check();
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTING);
    }

    //select image from gallery
    public void getGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    private void openCamera() {

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File imageFile= BitmapUtils.getImageFile();

            fileUri = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile);
            mPhotoPath = imageFile.getAbsolutePath();
            galleryAddPic(fileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, CAPTURE_IMAGE);

            Log.v(TAG,"image path3"+imageFile.getPath());
            Log.v(TAG,"image path4"+imageFile.getCanonicalPath());

        } catch (Exception e) {
            // generic exception handling
            e.printStackTrace();
        }
    }

    public void editImage(String imageUrl) {
        Intent i = new Intent(this, ImageEditActivity.class);
        i.putExtra("imgurl", imageUrl);
        startActivity(i);
    }

    private boolean isDeviceSupportCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA))
            return true;
        else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.camera_not_supported), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void galleryAddPic(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //File f = new File(mPhotoPath);
        //Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
