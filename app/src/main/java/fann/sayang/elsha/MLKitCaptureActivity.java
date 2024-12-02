package fann.sayang.elsha;

import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class MLKitCaptureActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_mlkit_captaure
        );

        surfaceView = findViewById(R.id.surfaceView);

        cameraSource = new CameraSource(
                this,
                new CameraSource.Callback() {
                    @Override
                    public void onImageAvailable(byte[] imageData, int rotationDegrees) {
                        // TODO
                    }
                }
        );

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                cameraSource.start(holder);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                cameraSource.updatePreviewSize(new Size(width, height));
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    private void processImage(byte[] imageData, int rotationDegrees) {
        try {
            InputImage image = InputImage.fromByteArray(
                    imageData,
                    cameraSource.getPreviewSize().getWidth(),
                    cameraSource.getPreviewSize().getHeight(),
                    rotationDegrees,
                    InputImage.IMAGE_FORMAT_NV21
            );

            BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            if (!barcodes.isEmpty()) {
                                Barcode barcode = barcodes.get(0);
                                String qrData = barcode.getRawValue();

                                if (qrData != null) {
                                    setResult(RESULT_OK, getIntent().putExtra("SCAN_RESULT", qrData));
                                    finish();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MLKitCaptureActivity.this, "Gagal memproses QR Code.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
