package com.allenc.awakedaydream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;

/**
 * The following ColorPickerActivity snippet was taken from an online post and adapted to fit my application.
 * The original source can be found at: http://stackoverflow.com/questions/16363235/android-color-picker-to-be-included-in-the-activity
 */
public class ColorPickerActivity extends Activity implements ColorPickerView.OnColorChangedListener, View.OnClickListener {

    private ColorPickerView	mColorPickerView;
    private ColorPanelView	mOldColorPanelView;
    private ColorPanelView  mNewColorPanelView;

    private Button mOkButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);

        setContentView(R.layout.activity_color_picker);

        init();

    }

    /**
     *
     */
    private void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int initialColor = prefs.getInt(Settings.KEY_FONT_COLOR, 0xFF000000);

        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpickerview__color_picker_view);
        mOldColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_old);
        mNewColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_new);

        mOkButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);


        ((LinearLayout) mOldColorPanelView.getParent()).setPadding(
                mColorPickerView.getPaddingLeft(), 0,
                mColorPickerView.getPaddingRight(), 0);


        mColorPickerView.setOnColorChangedListener(this);
        mColorPickerView.setColor(initialColor, true);
        mOldColorPanelView.setColor(initialColor);

        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

    }

    @Override
    public void onColorChanged(int newColor) {
        mNewColorPanelView.setColor(mColorPickerView.getColor());
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.okButton:
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                edit.putInt(Settings.KEY_FONT_COLOR, mColorPickerView.getColor());
                edit.commit();

                finish();
                break;
            case R.id.cancelButton:
                finish();
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Daydream.setInForeground(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Daydream.setInForeground(true);
    }


}
