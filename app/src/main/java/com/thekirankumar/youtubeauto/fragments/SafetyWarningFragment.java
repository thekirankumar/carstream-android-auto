package com.thekirankumar.youtubeauto.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thekirankumar.youtubeauto.BuildConfig;
import com.thekirankumar.youtubeauto.R;

/**
 * Created by kiran.kumar on 14/01/18.
 */

public class SafetyWarningFragment extends Fragment {
    private FragmentInteractionListener listener;
    private View continueButton;
    private TextView appInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.safety_warning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appInfo = view.findViewById(R.id.app_info_view);
        appInfo.setText(getString(R.string.app_name)+ " v" + BuildConfig.VERSION_NAME);
        continueButton = view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReadyToExitSafetyInstructions(SafetyWarningFragment.this);
            }
        });
        continueButton.setFocusable(true);
        continueButton.setFocusableInTouchMode(true);
        continueButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    continueButton.requestFocus();
                }
            }
        });
        continueButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onReadyToExitSafetyInstructions(SafetyWarningFragment.this);
                    return true;
                } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                {
                    //disable focus lose
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        continueButton.requestFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof FragmentInteractionListener) {
            listener = (FragmentInteractionListener) parentFragment;
        }
    }

    public interface FragmentInteractionListener {
        void onReadyToExitSafetyInstructions(SafetyWarningFragment warningFragment);
    }
}
