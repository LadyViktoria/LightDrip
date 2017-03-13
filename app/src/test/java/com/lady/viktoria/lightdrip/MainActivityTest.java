package com.lady.viktoria.lightdrip;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    private Activity cut;

    @Before
    public void setUp() throws Exception {
        cut = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void onCreate() throws Exception {

    }

    @Test
    public void showText() throws Exception {

    }

    @Test
    public void onResume() throws Exception {

    }

    @Test
    public void onPause() throws Exception {

    }

    @Test
    public void onDestroy() throws Exception {

    }

    @Test
    public void onBackPressed() throws Exception {

    }

    @Test
    public void onTrayPreferenceChanged() throws Exception {

    }

    @Test
    public void startRealmListener() throws Exception {

    }

    @Test
    public void onClick() throws Exception {

    }

    @Test
    public void updateBatLevel() throws Exception {

    }

}