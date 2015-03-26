package com.champion.mipis;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("NewApi")
public class SettingFragment extends Fragment {

    private static final String TAG = "ContractFragment";

    private Context mContext;

    private View mFragmentView;

    ImageView mHeadiconImageView;

    RelativeLayout mInfoLayout;

    RadioButton maleButton;

    RadioButton famaleButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        mFragmentView = inflater.inflate(R.layout.settings, container, false);

        Log.d(TAG, "SettingFragment onCreateView");

        mContext = getActivity();

        getActivity().setTitle("设置");

        return mFragmentView;
    }
    

    private void initView() {

        mHeadiconImageView = (ImageView) mFragmentView.findViewById(R.id.head_imageview);

        mHeadiconImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                settingHeaderIcon();
            }
        });

        mInfoLayout = (RelativeLayout) mFragmentView.findViewById(R.id.information_layout);

        mInfoLayout.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {

                }
            }
        });
        
        RadioGroup mRadioGroup = (RadioGroup) mFragmentView.findViewById(R.id.gender_radioGroup);
        maleButton = (RadioButton) mFragmentView.findViewById(R.id.radio_male);
        famaleButton = (RadioButton) mFragmentView.findViewById(R.id.radio_female);

        mRadioGroup.setOnCheckedChangeListener(mRadioButtonListener);
    }

    OnCheckedChangeListener mRadioButtonListener;
    
    private void settingHeaderIcon() {
        Toast.makeText(mContext, "改变头像，功能还在继续中，敬请期待！", Toast.LENGTH_SHORT).show();
    }

}
