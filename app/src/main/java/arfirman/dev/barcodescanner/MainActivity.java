package arfirman.dev.barcodescanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context myContext = this;
    private SurfaceView mainCamera;
    private TextView mainInfo;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private String barcodeMessage = "";
    private Button mainRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decView();
        setBarcodeScanner();
        setCamera();
    }

    private void setCamera() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels / 2;
        int width = displaymetrics.widthPixels;

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(height, width)
                .build();

        mainCamera.getHolder().addCallback(new MainCameraCallback());
    }

    private void setBarcodeScanner() {
        int barcodeType = Barcode.QR_CODE;

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(barcodeType)
                .build();

        barcodeDetector.setProcessor(new BarcodeProcessor());
    }

    private void decView() {
        mainCamera = (SurfaceView) findViewById(R.id.main_camera);
        mainInfo = (TextView) findViewById(R.id.main_info);
        mainRefresh = (Button) findViewById(R.id.main_refresh);

        mainRefresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_refresh:
                restartCamera();
                break;
            default:
                break;
        }
    }

    private void restartCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                Log.e("MainActivity", "Did not get a permission access Camera");
            else cameraSource.start(mainCamera.getHolder());
            mainRefresh.setVisibility(View.GONE);
        } catch (IOException e) {
            Log.e("MainActivity", "Camera Error - " + e.toString());
            e.printStackTrace();
        }
    }

    public static void startIntent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
        ((Activity) context).finish();
    }

    private class MainCameraCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    Log.e("MainActivity", "Did not get a permission access Camera");
                else cameraSource.start(holder);
            } catch (IOException e) {
                Log.e("MainActivity", "Camera Error - " + e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            cameraSource.stop();

        }
    }

    private class BarcodeProcessor implements com.google.android.gms.vision.Detector.Processor<Barcode> {

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            final SparseArray<Barcode> barcodeList = detections.getDetectedItems();

            if (barcodeList.size() != 0) {
                Log.d("MainActivity", "barcode size = " + barcodeList.size());
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                final Barcode barcodeValue = barcodeList.valueAt(0);
                Log.d("MainActivity", gson.toJson(barcodeValue));
//                barcodeMessage = barcodeValue.displayValue;
                barcodeMessage += gson.toJson(barcodeValue) + "\n\n";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*cameraSource.stop();
                        mainRefresh.setVisibility(View.VISIBLE);*/
                        mainInfo.setText(barcodeMessage);
                    }
                });
            }

            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainInfo.setText(barcodeMessage);
                }
            });*/
        }
    }
}
