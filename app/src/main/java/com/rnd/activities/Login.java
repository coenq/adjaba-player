package com.rnd.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.rnd.R;
import com.rnd.utilities.TinyDB;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class Login extends AppCompatActivity implements View.OnClickListener {



    private Button loginbtn;
    Context context;
    Activity ac;
    LinearLayout bot_lay;
    RelativeLayout loginrootlayout;
    TinyDB tinyDb;
    Spinner spinner1;
    CheckBox   back_camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login);
        context = this;
        ac = this;
        tinyDb = new TinyDB( ac );

        findViews();
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tinyDb.putInt("Orientation",position);
              }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



    }

    private void findViews() {


        loginbtn = findViewById( R.id.loginbtn );
        bot_lay= findViewById( R.id.bot_lay );
        spinner1=findViewById( R.id.spinner1);

        back_camera=findViewById( R.id.back_camera);
        loginrootlayout = findViewById( R.id.loginrootlayout );
        tinyDb = new TinyDB( ac );
        loginbtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if (v == loginbtn) {

            if(back_camera.isChecked()){
             tinyDb.putBoolean("BackCamera", true);
            }else{
            tinyDb.putBoolean("BackCamera", false);
            }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    //Permission check and ask for required permission
                    Dexter.withActivity(ac)
                            .withPermissions(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {

                                    Intent intent = new Intent(getApplicationContext(), TestCamera.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                            }

                            // check for permanent denial of any permission
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                // permission is denied permenantly, navigate user to app settings
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(ac);
                                builder1.setMessage("App will not work properly, please allow all permission");
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }

                        }
                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }

                    }).check();

                }else {
                        Intent intent = new Intent(getApplicationContext(), TestCamera.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                      }
        }
    }

}
