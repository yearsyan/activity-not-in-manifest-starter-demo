package com.example.activityNotInManifestStarterDemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.activityNotInManifestStarterDemo.utils.ActivityStartHook;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);

        Button button = new Button(this);
        button.setText(R.string.start_new_activity_button_content);
        button.setBackgroundColor(Color.BLUE);
        button.setOnClickListener(l->{
            Intent intent = new Intent(MainActivity.this, AnotherActivity.class); // real Intent
            MainActivity.this.startActivity(
                    ActivityStartHook.buildWrapIntent(MainActivity.this, intent) // fake intent
            );
        });

        linearLayout.addView(button);
        linearLayout.setHorizontalGravity(Gravity.CENTER);
        linearLayout.setVerticalGravity(Gravity.CENTER);
        setContentView(linearLayout);

    }
}
