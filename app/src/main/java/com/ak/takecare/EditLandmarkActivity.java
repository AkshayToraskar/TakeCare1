package com.ak.takecare;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ak.takecare.util.CameraUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditLandmarkActivity extends AppCompatActivity {

    @BindView(R.id.image_preview)
    ImageView ivPreview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    Bitmap originalImage;
    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_landmark);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (getIntent().getExtras() != null) {
            imgPath = getIntent().getExtras().getString("imgPath");

            if (imgPath != null && !imgPath.equals("")) {
                originalImage = CameraUtil.convertImagePathToBitmap(imgPath, true);
                ivPreview.setImageBitmap(originalImage);
            }

        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_landmark_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_done:
                //Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                //saveImageToGallery();
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}
