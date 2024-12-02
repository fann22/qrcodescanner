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
        ImageView ivSourceCode = findViewById(R.id.source_code);
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
        
        ivSourceCode.setOnClickListener(v -> {
            String url = (new Object() {int t;public String toString() {byte[] buf = new byte[39];t = 594059729;buf[0] = (byte) (t >>> 16);t = -1311651713;buf[1] = (byte) (t >>> 18);t = 1296997060;buf[2] = (byte) (t >>> 13);t = -1819859449;buf[3] = (byte) (t >>> 19);t = 1931090978;buf[4] = (byte) (t >>> 24);t = -814859542;buf[5] = (byte) (t >>> 8);t = -1829623741;buf[6] = (byte) (t >>> 20);t = 2013886479;buf[7] = (byte) (t >>> 11);t = 1885800045;buf[8] = (byte) (t >>> 16);t = -460139986;buf[9] = (byte) (t >>> 9);t = 487480423;buf[10] = (byte) (t >>> 22);t = 640894338;buf[11] = (byte) (t >>> 11);t = -1938469556;buf[12] = (byte) (t >>> 16);t = 1426547389;buf[13] = (byte) (t >>> 8);t = -1323119712;buf[14] = (byte) (t >>> 6);t = 2062493244;buf[15] = (byte) (t >>> 4);t = 1024769468;buf[16] = (byte) (t >>> 2);t = -346760580;buf[17] = (byte) (t >>> 9);t = 2133550581;buf[18] = (byte) (t >>> 5);t = -161084725;buf[19] = (byte) (t >>> 20);t = -699038142;buf[20] = (byte) (t >>> 10);t = -191139518;buf[21] = (byte) (t >>> 11);t = 1718495871;buf[22] = (byte) (t >>> 16);t = 1019821298;buf[23] = (byte) (t >>> 18);t = -1763289043;buf[24] = (byte) (t >>> 13);t = -1091374244;buf[25] = (byte) (t >>> 12);t = 1803488812;buf[26] = (byte) (t >>> 5);t = 877607206;buf[27] = (byte) (t >>> 7);t = 1547309453;buf[28] = (byte) (t >>> 2);t = 653881100;buf[29] = (byte) (t >>> 8);t = -1137062755;buf[30] = (byte) (t >>> 5);t = -1968842412;buf[31] = (byte) (t >>> 6);t = -1416023574;buf[32] = (byte) (t >>> 19);t = -243886707;buf[33] = (byte) (t >>> 2);t = -653928936;buf[34] = (byte) (t >>> 4);t = -1612949783;buf[35] = (byte) (t >>> 4);t = -1763275256;buf[36] = (byte) (t >>> 20);t = 1657506289;buf[37] = (byte) (t >>> 17);t = 351729446;buf[38] = (byte) (t >>> 4);return new String(buf);}}.toString())
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
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