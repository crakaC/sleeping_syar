package com.crakac.ofuton.activity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.crakac.ofuton.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class LicenseActivity extends FinishableActionbarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        TextView textView = (TextView) findViewById(R.id.text);
        try {
            InputStream is = getAssets().open("license.text");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            String text = new String(buf);
            textView.setText(Html.fromHtml(text.replace("\n", "<br/>")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
