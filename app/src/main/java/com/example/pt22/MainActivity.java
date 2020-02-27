package com.example.pt22;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout.LayoutParams params;   //widget

    boolean left;
    static final String TAG = "test";
    ScrollView scrollView;
    Drawable drawableLeft, drawableRight;
    ImageButton camera;
    ImageView photoResult;
    Boolean bGranted = false;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private static final int MY_PERMISSIONS_REQUESTS = 70;

    static final int REQUEST_IMAGE_CAPTURE = 1;


    public void onClickAfegir(View v) {

        EditText userText = (EditText) findViewById(R.id.yourchat);

        if (!userText.getText().toString().equals("")) {

            LinearLayout lay = (LinearLayout) findViewById(R.id.chat);

            drawableLeft = getResources().getDrawable(getResources().getIdentifier("bubbs", "drawable", getPackageName()));
            drawableRight = getResources().getDrawable(getResources().getIdentifier("textbubdos", "drawable", getPackageName()));
            TextView tv = new TextView(this);
            tv.setText(userText.getText().toString());
            if (left) {

                tv.setBackground(drawableLeft);
            }  //leftbuble ha de ser format 9patch    leftbublle.9.png per tal que s'adapti al textView
            else { //clicar a imatge botó dreta, sota, create 9 patch file, clica sobre fitxer i edita'l.
                tv.setBackground(drawableRight);
            }
            // tv.setBackground(drawableLeft);
            userText.getText().clear();

            //scrollDown();


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = left ? Gravity.LEFT : Gravity.RIGHT;

            lay.addView(tv, params);
            left = !left;
            // scrollDown();
        }

    }

    public void scrollDown() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        left = false;
        scrollView = (ScrollView) findViewById(R.id.scrollViewChat);
        camera = (ImageButton) findViewById(R.id.cameracat);
        camera.setOnClickListener(this);

        try {
            int cameraId;

            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = null; //Camera.open(cameraId);
                Log.d(TAG, "onCreate: " + String.valueOf(cameraId));
            }
            Log.d("test", "CameraId: " + cameraId);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + e.getCause() + e.getMessage());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermissions() {

        //demana permisos i dona explicació si laprimera vegada nega algun d ells


        int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        int permCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        if (!(permCheck1 == PackageManager.PERMISSION_GRANTED) | (!(permCheck2 == PackageManager.PERMISSION_GRANTED))) {

            requestSDReadWritePermissions();


        }
        else bGranted = true;


    }

    private void requestSDReadWritePermissions() {

        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)))) {

            // asincrona:no bloquejar el thread esperant la seva resposta
            // Bona pràctica, try again to request the permission.
            // explicar a l usuari per què calen aquests permisos
            new AlertDialog.Builder(this)
                    .setTitle("Es necessita permís d'accés a disc de lectura i escriptura")
                    .setMessage("Per a accedir a càmera, necessitem els dos permisos")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this
                                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUESTS);
                        }
                    })
                    .setNegativeButton("cancel.lar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();


        } else {
            // request the permission.
            // CALLBACK_NUMBER is a integer constants
            //   Toast.makeText(this, "demana permis, no rationale ", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
            // The callback method gets the result of the request.
            // Log.d(TAG, "startRecording: no rationale");
        }

    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //Log.d("test", "Camera found"+cameraId);
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    //Codi per amagar el teclat al clicar fora d'ell
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = left ? Gravity.LEFT : Gravity.RIGHT;

        if (id == R.id.emojiCat){
            LinearLayout lay = (LinearLayout) findViewById(R.id.chat);

            Random rn = new Random();
            int answer = rn.nextInt(4) + 1;
            Drawable drawable = null;
            switch (answer) {
                case 1:
                    drawable = getResources().getDrawable(getResources().getIdentifier("comfycat", "drawable", getPackageName()));
                    break;
                case 2:
                    drawable = getResources().getDrawable(getResources().getIdentifier("grumpycat", "drawable", getPackageName()));
                    break;
                case 3:
                    drawable = getResources().getDrawable(getResources().getIdentifier("smilecat", "drawable", getPackageName()));
                    break;
                case 4:
                    drawable = getResources().getDrawable(getResources().getIdentifier("sneakycat", "drawable", getPackageName()));
                    break;


            }

            ImageView imageView = new ImageView(this);
            imageView.setBackground(drawable);
            lay.addView(imageView, params);


        } else if (id == R.id.cameracat){
            checkPermissions();
            photoResult = new ImageView(this);
            try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        LinearLayout lay = (LinearLayout) findViewById(R.id.chat);
                        lay.addView(photoResult,params);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

        }

        left = !left;
        scrollDown();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoResult.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              //  Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
