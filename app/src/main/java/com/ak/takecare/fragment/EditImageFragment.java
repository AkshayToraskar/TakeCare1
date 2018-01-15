package com.ak.takecare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ak.takecare.R;
import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditImageFragment extends Fragment {

    private EditImageFragmentListener listener;

    //@BindView(R.id.seekbar_aging)
    //SeekBar seekBarAging;

    @BindView(R.id.lblAge)
    TextView tvAge;

    @BindView(R.id.horizontalWheelView)
    HorizontalWheelView horizontalWheelView;

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public EditImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_image, container, false);

        ButterKnife.bind(this, view);

        //horizontalWheelView.setProgress(42);
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {

                Log.v("rotation radiant", " sdaf " + (int) (horizontalWheelView.getDegreesAngle() / 3.6));
                listener.onAgeChanged((int) (horizontalWheelView.getDegreesAngle() / 3.6));
                updateText();
            }

        });


        return view;
    }


    private void updateText() {


        String text = String.format(Double.toString(horizontalWheelView.getDegreesAngle() / 3.6));
        tvAge.setText(text);
    }

    /*@Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (listener != null) {

            if (seekBar.getId() == R.id.seekbar_aging) {
                // brightness values are b/w -100 to +100
                listener.onAgeChanged(progress);
            }


        }
    }*/

   /* @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditCompleted();
    }*/

    /*public void resetControls() {
        seekBarAging.setProgress(30);

    }*/

    public interface EditImageFragmentListener {

        void onAgeChanged(int age);

        void onEditStarted();

        void onEditCompleted();
    }
}
