package com.christophesmet.colorpicker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.christophesmet.colorpicker.R;

public class MainActivity extends AppCompatActivity {

    private ColorPickerView mColorPickerView;
    private View mColorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpicker);
        mColorView = findViewById(R.id.result_color);
        loadListeners();
        //Set this to true, to enable visual debugging. To check the offset radius
        mColorPickerView.setDrawDebug(false);
    }

    private void loadListeners() {
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {
                mColorView.setBackgroundColor(color);
            }
        });
    }
}
