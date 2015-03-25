package com.champion.mipis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class SettingActivity extends Activity {

    ImageView mHeadiconImageView;

    RelativeLayout mInfoLayout;
    

    RadioButton maleButton;

    RadioButton famaleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        initView();
    }

    private void initView() {

        mHeadiconImageView = (ImageView) findViewById(R.id.head_imageview);

        mHeadiconImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                settingHeaderIcon();
            }
        });

        mInfoLayout = (RelativeLayout) findViewById(R.id.information_layout);

        mInfoLayout.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {

                }
            }
        });
        
        RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.gender_radioGroup);
        maleButton = (RadioButton) findViewById(R.id.radio_male);
        famaleButton = (RadioButton) findViewById(R.id.radio_female);

        mRadioGroup.setOnCheckedChangeListener(mRadioButtonListener);
    }

    OnCheckedChangeListener mRadioButtonListener;
    
    private void settingHeaderIcon() {
        Toast.makeText(getApplicationContext(), "改变头像，功能还在继续中，敬请期待！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    private void saveData() {

    }

}
