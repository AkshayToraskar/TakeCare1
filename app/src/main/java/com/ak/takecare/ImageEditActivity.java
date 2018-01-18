package com.ak.takecare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.takecare.fragment.EditImageFragment;
import com.ak.takecare.fragment.FiltersListFragment;
import com.ak.takecare.model.ImageData;
import com.ak.takecare.util.BitmapUtils;
import com.ak.takecare.util.CameraUtil;
import com.github.shchurov.horizontalwheelview.HorizontalWheelView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/*import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;*/

public class ImageEditActivity extends AppCompatActivity {

    public static String TAG = ImageEditActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image_preview)
    ImageView imagePreview;
    /*@BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;*/
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private EditImageFragment.EditImageFragmentListener listener;

    //@BindView(R.id.seekbar_aging)
    //SeekBar seekBarAging;

    @BindView(R.id.lblAge)
    TextView tvAge;

    @BindView(R.id.horizontalWheelView)
    HorizontalWheelView horizontalWheelView;

    Bitmap originalImage;
    //Bitmap filteredImage;
    Bitmap finalImage;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    Realm realm;


    // load native image filters library
   /* static {
        System.loadLibrary("NativeImageProcessor");
    }*/

    private FaceDetector detector;
    String imagePath;
    Bitmap editedBitmap;
    public long imgid;
    //  public static final String IMAGE_NAME = "dog.jpg";

    SparseArray<Face> faces;
    Bitmap bitmapWrinkles;
    Bitmap bitmapTeeth;
    Paint alphaPaint;
    Canvas canvas;
    Paint paint;
    Bitmap bmpEditedface;

    MediaPlayer mediaPlayer;
    public static int tickMark = 8;

    ImageData imgData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        mediaPlayer = MediaPlayer.create(this, R.raw.woosh);

        detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //setupViewPager(viewPager);
        //tabLayout.setupWithViewPager(viewPager);


        if (getIntent().getExtras() != null) {
            imagePath = getIntent().getExtras().getString("imgurl");
            imgid = getIntent().getExtras().getLong("imgid", 0);

            if (imgid != 0) {
                imgData = realm.where(ImageData.class).equalTo("id", imgid).findFirst();
            }


            if (!imagePath.equals("") && imagePath != null) {

                //Bitmap bitmap = CameraUtil.convertImagePathToBitmap(imagePath, true);
                //imagePreview.setImageBitmap(bitmap);
                originalImage = CameraUtil.convertImagePathToBitmap(imagePath, true);
                imagePreview.setImageBitmap(originalImage);

                try {
                    scanFaces();
                    //scanFacesDlib();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.toString());
                }
            } else
                Toast.makeText(this, R.string.capture_image_failed, Toast.LENGTH_SHORT).show();
        }

        horizontalWheelView.setEndLock(true);

        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                Log.v("rotation radiant", " sdaf " + (int) (horizontalWheelView.getDegreesAngle() / 3.6));
                //listener.onAgeChanged((int) (horizontalWheelView.getDegreesAngle() / 3.6));
                int age = (int) (horizontalWheelView.getDegreesAngle() / 3.6);

                if (tickMark < age + 24 || tickMark > age - 24) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.woosh);
                        }
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tickMark = age + 8;

                }

                resetFaceValues(Math.abs(age));
                Log.v("age", " " + age);
                updateText();
            }

        });

        if (imgData != null) {
            horizontalWheelView.setDegreesAngle((imgData.getAge() * 3.6));
        }


    }


    private void updateText() {


        Double d = horizontalWheelView.getDegreesAngle() / 3.6;

        String text = String.valueOf(d.intValue());
        tvAge.setText(text);
    }


   /* private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        //  filtersListFragment.setListener(this);

        // adding edit image fragment
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(editImageFragment, getString(R.string.lbl_aging));
        //adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));


        viewPager.setAdapter(adapter);
    }*/


   /* @Override
    public void onFilterSelected(Filter filter) {
        // reset image controls
        resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }*/

    /*@Override
    public void onAgeChanged(int age) {
        resetFaceValues(Math.abs(age));

        Log.v("age", " " + age);

    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        //   final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

    }*/


    /*class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }*/


    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
  /*  private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_save:
                //Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                saveImageToGallery();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    public void resetFaceValues(int ageData) {
        editedBitmap.recycle();
        bitmapTeeth.recycle();


        initFaceValued(ageData);
        setFaceData(ageData);
    }

    public static int range(int num) {
        if (0 <= num && num < 5)
            return 1;
        if (5 <= num && num < 10)
            return 2;
        if (10 <= num && num < 15)
            return 3;
        if (15 <= num && num < 20)
            return 4;
        if (20 <= num && num < 40)
            return 5;
        if (40 <= num && num < 60)
            return 6;
        if (60 <= num && num < 80)
            return 7;


        return 8;

    }

    private void scanFaces() throws Exception {

        if (detector.isOperational() && originalImage != null) {

            initFaceValued(42);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            faces = detector.detect(frame);
            setFaceData(42);
        } else {
            // scanResults.setText("Could not set up the detector!");
        }
    }


    public void initFaceValued(int ageData) {
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        editedBitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage
                .getHeight(), originalImage.getConfig());
        float scale = getResources().getDisplayMetrics().density;


        switch (range(ageData)) {
            case 1:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t1);
                break;

            case 2:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t2);
                break;

            case 3:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t3);
                break;

            case 4:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t4);
                break;

            case 5:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t5);
                break;

            case 6:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t6);
                break;

            case 7:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t7);
                break;

            case 8:
                bitmapTeeth = BitmapFactory.decodeResource(getResources(), R.drawable.teeth_t8);
                break;
        }


        bitmapWrinkles = BitmapFactory.decodeResource(getResources(), R.drawable.wrinkles2);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 61, 61));
        paint.setTextSize((int) (14 * scale));
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);


        canvas = new Canvas(editedBitmap);
        canvas.drawBitmap(originalImage, 0, 0, paint);
    }


    public void setFaceData(int ageVal) {


        alphaPaint = new Paint();
        alphaPaint.setAlpha((ageVal / 2));


        for (int index = 0; index < faces.size(); ++index) {
            Face face = faces.valueAt(index);

            //float facewidth = face.getPosition().x + face.getWidth();
            //float faceheight = face.getPosition().y + face.getHeight();

           /* canvas.drawRect(
                    face.getPosition().x,
                    face.getPosition().y,
                    facewidth,
                    faceheight, paint);*/


            Log.v("Smile Probability", " " + face.getIsSmilingProbability());
            bmpEditedface = Bitmap.createScaledBitmap(bitmapWrinkles, (int) face.getWidth(), (int) face.getHeight(), true);


            // alphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
            alphaPaint.setShader(new BitmapShader(bmpEditedface, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));


            canvas.drawBitmap(bmpEditedface, face.getPosition().x, face.getPosition().y + 30, alphaPaint);

            //  canvas.drawRect(face.getPosition().x, face.getPosition().y + 20,facewidth,faceheight,alphaPaint);


            PointF pointRightMouth = null, pointLeftMouth = null, pointBottomMouth = null;


            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                Log.v("Landmark type", "type " + landmark.getType());


                // canvas.drawCircle(cx, cy, 5, paint);

                if (landmark.getType() == Landmark.RIGHT_MOUTH) {
                    pointRightMouth = landmark.getPosition();
                    //canvas.drawBitmap(bitmapTeeth, landmark.getPosition().x, landmark.getPosition().y-10, paint);
                } else if (landmark.getType() == Landmark.LEFT_MOUTH) {
                    pointLeftMouth = landmark.getPosition();
                } else if (landmark.getType() == Landmark.BOTTOM_MOUTH) {
                    pointBottomMouth = landmark.getPosition();
                }

            }


            Bitmap bmpTeeth = Bitmap.createScaledBitmap(bitmapTeeth, (int) pointLeftMouth.x - (int) pointRightMouth.x, ((int) pointBottomMouth.y - (int) pointLeftMouth.y) + 30, true);
            canvas.drawBitmap(bmpTeeth, (int) pointRightMouth.x, (int) pointRightMouth.y - (int) (bmpTeeth.getHeight() / 3), paint);

        }

        if (faces.size() == 0) {
            //  scanResults.setText("Scan Failed: Found nothing to scan");
        } else {
            imagePreview.setImageBitmap(editedBitmap);

        }
    }


    /*
   * saves image to camera gallery
   * */
    private void saveImageToGallery() {


        //finalImage = editedBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), editedBitmap, System.currentTimeMillis() + "_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {


                                Uri uri = Uri.parse(path);
                                final String finalImagepath = CameraUtil.getPath(getApplicationContext(), uri);
                                Log.v("asdf", " " + finalImagepath);


                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {

                                        if (imgid == 0) {

                                            ImageData imageData = realm.createObject(ImageData.class, new Date().getTime());
                                            imageData.setPrevPath(imagePath);
                                            imageData.setEditedPath(finalImagepath);
                                            imageData.setDateTime(String.valueOf(new Date()));

                                            Double d = Double.valueOf(tvAge.getText().toString());
                                            imageData.setAge(d.intValue());

                                            realm.copyToRealmOrUpdate(imageData);
                                        } else {
                                            ImageData imageData = realm.where(ImageData.class).equalTo("id", imgid).findFirst();
                                            imageData.setEditedPath(finalImagepath);
                                            imageData.setDateTime(String.valueOf(new Date()));

                                            Double d = Double.valueOf(tvAge.getText().toString());
                                            imageData.setAge(d.intValue());
                                            realm.copyToRealmOrUpdate(imageData);
                                        }
                                    }
                                });


                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG);
                                       /* .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });*/

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }


    public void onBtnClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_edit_landmark:
                Intent i = new Intent(this, EditLandmarkActivity.class);
                i.putExtra("imgPath",imagePath);
                startActivity(i);
                break;

            case R.id.btn_effect:

                break;
        }
    }


}
