package com.crakac.com;

import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class MainActivityTest {
    @Test
    public void hasCollectAppName() {
        String appName = new MainActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Sleeping( ˘ω˘)Syar"));
    }
}
