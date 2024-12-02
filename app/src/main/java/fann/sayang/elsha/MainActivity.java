package fann.sayang.elsha;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView tvUrl;
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private static final int REQUEST_CODE_IMAGE_PICK = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        tvUrl = findViewById(R.id.tv_url);
        ImageView ivCopy = findViewById(R.id.iv_copy);
        ImageView ivOpen = findViewById(R.id.iv_open);
        Button btnScanQR = findViewById(R.id.btn_scan_qr);
        Button btnPickImage = findViewById(R.id.btn_pick_image);

        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewCaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        });

        ivCopy.setOnClickListener(v -> {
            String url = tvUrl.getText().toString();
            if (!url.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied URL", url);
                clipboard.setPrimaryClip(clip);
            }
        });

        ivOpen.setOnClickListener(v -> {
            String url = tvUrl.getText().toString();
            if (!url.isEmpty() && isValidUrl(url)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "URL tidak valid.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (resultCode == RESULT_OK && data != null){
                String scannedData = data.getStringExtra("SCAN_RESULT");
                String processedUrl = processBase64String(scannedData);
                tvUrl.setText(processedUrl);
                // Toast.makeText(this, "Hasil QR Code:" + scannedData, Toast.LENGTH_LONG).show();
            } else {
                tvUrl.setText("Pemindaian dibatalkan.");
                // Toast.makeText(this, "Pemindaian dibatalkan.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_IMAGE_PICK) {
            if (resultCode == RESULT_OK && data != null) {
                // Toast.makeText(this, "proceed", Toast.LENGTH_SHORT).show();
                Uri scannedData = data.getData();
                //decodeQRCodeFromImage(scannedData);
                processImage(scannedData);
            } else {
                tvUrl.setText("Kamu tidak memilih gambar apapun.");
                // Toast.makeText(this, "Kamu tidak memilih gambar apapun.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String qrData = barcode.getRawValue();
                            String processed = processBase64String(qrData);
                            tvUrl.setText(processed);
                        }
                    })
                    .addOnFailureListener(e -> {
                       e.printStackTrace();
                       tvUrl.setText(e.toString());
                    });
        } catch (Exception e) {
            e.printStackTrace();
            tvUrl.setText("Gagal melakukan scan.");
        }
    }
    /*
    private void decodeQRCodeFromImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)){
            if (inputStream == null) {
                tvUrl.setText("Gambar tidak valid");
                // Toast.makeText(this, "Gambar tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new QRCodeReader();
            Result result = reader.decode(binaryBitmap);

            String processedUrl = processBase64String(result.getText());
            tvUrl.setText(processedUrl);
            // Toast.makeText(this, "Hasil QR Code:" + result.getText(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Terjadi kesalahan saat memproses gambar tersebut.", Toast.LENGTH_SHORT).show();
        }
    }
*/
    private String processBase64String(String encodedString) {
        String decodedString = encodedString;
        int maxAttempts = 999;

        for (int i = 0; i < maxAttempts; i++) {
            try {
                decodedString = new String(Base64.decode(decodedString, Base64.DEFAULT), "UTF-8");

                if (isValidUrl(decodedString)) {
                    return decodedString;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encodedString;
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}