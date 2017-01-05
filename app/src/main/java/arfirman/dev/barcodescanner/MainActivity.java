package arfirman.dev.barcodescanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context myContext = this;
    private SurfaceView mainCamera;
    private ImageView mainPicture;
    private TextView mainInfo;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private String barcodeMessage;
    private Button mainRefresh;
    private List<Point> barcodePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decView();

        int barcodeType = Barcode.QR_CODE;

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(barcodeType)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(600, 600)
                .build();

        mainCamera.getHolder().addCallback(new MainCameraCallback());

        barcodeDetector.setProcessor(new BarcodeProcessor());
    }

    private void decView() {
        mainCamera = (SurfaceView) findViewById(R.id.main_camera);
        mainInfo = (TextView) findViewById(R.id.main_info);
        mainRefresh = (Button) findViewById(R.id.main_refresh);
        mainPicture = (ImageView) findViewById(R.id.main_picture);

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
            if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "Did not get a permission access Camera");
            } else cameraSource.start(mainCamera.getHolder());
            mainRefresh.setVisibility(View.GONE);
        } catch (IOException e) {
            Log.e("MainActivity", "Camera Error - " + e.toString());
            e.printStackTrace();
        }
    }

    private class MainCameraCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("MainActivity", "Did not get a permission access Camera");
                } else cameraSource.start(holder);
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
            Log.d("MainActivity", "Barcode Released");
        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            final SparseArray<Barcode> barcodes = detections.getDetectedItems();

            if (barcodes.size() != 0) {
                Log.d("MainActivity", "barcode size = " + barcodes.size());
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                final Barcode barcodeValue = barcodes.valueAt(0);
                Log.d("MainActivity", gson.toJson(barcodeValue));
                barcodeMessage = barcodeValue.displayValue;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraSource.stop();
                        mainInfo.setText(barcodeMessage);
                        mainRefresh.setVisibility(View.VISIBLE);
//                        mainPicture.setImageBitmap(mainCamera.getDrawingCache(true));
                        /*mainPicture.setImageResource(R.mipmap.ic_launcher);*/
                        /*barcodePosition = new ArrayList<>();
                        int start;
                        int stop;
                        Point[] barcodeCornerPoints = barcodeValue.cornerPoints;
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(3);
                        paint.setColor(Color.BLUE);
                        paint.setStrokeWidth(10);
                        for (int i = 0; i < barcodeCornerPoints.length; i++) {
                            start = i;
                            if (i == barcodeCornerPoints.length - 1) stop = 0;
                            else stop = i + 1;
                            canvas.drawLine(barcodeCornerPoints[start].x, barcodeCornerPoints[start].y, barcodeCornerPoints[stop].x, barcodeCornerPoints[stop].y, paint);
                        }*/
                    }
                });
            } else barcodeMessage = "No Detection Result";

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainInfo.setText(barcodeMessage);
                }
            });
        }
    }
}
