package com.ilyzs.tinkerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    Button tinkerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request();
        tinkerBtn = findViewById(R.id.btn_main);
        tinkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/tinker/patch_signed_7zip.apk");
                Toast.makeText(MainActivity.this,Environment.getExternalStorageDirectory().getAbsolutePath() + "/tinker/patch1_signed_7zip.apk",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void request(){
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
