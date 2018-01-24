package com.thekirankumar.youtubeauto.service;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarActivityService;
import com.thekirankumar.youtubeauto.activity.MainCarActivity;

public class CarService extends CarActivityService {
    public Class<? extends CarActivity> getCarActivity() {
        return MainCarActivity.class;
    }
}
