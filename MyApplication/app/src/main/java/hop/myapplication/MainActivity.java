package hop.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.example.BuildConfig;
import com.example.R;

import org.bytedeco.javacpp.opencv_core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = MainActivity.class.getName();

    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    protected static final int ANALYSE_REQUEST_CODE = 5;
    final int captureActivityResult = 2;
    final int libraryActivityResult = 1;
    final int analyseActivityResult = 12;

    private Intent analysisIntent;
    /*
    final int analyseActivityResult = 300;

    final int photoRequestActivityResult = 400;
    final int resultCodeActivityResult = 500;
    */
    Button captureButton;
    Button libraryButton;
    Button analyseButton;

    ImageView photoView;

    private static final String SHARED_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analyseButton = (Button) findViewById(R.id.analyseButton);
        analyseButton.setOnClickListener(this);

        photoView = (ImageView) findViewById(R.id.imageAnalysed);

        analysisIntent = new Intent(MainActivity.this, Analyse.class);
        declareImages();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureButton.setEnabled(true); // If user doesn't have the permission the button is hidden
                Log.i(TAG, "Permission wasn't allowed but now is granted");
            }
        }
    }

    public void onClick(View view) {
        if (view == findViewById(R.id.captureButton)) {
            startCaptureActivity();
        } else if (view == findViewById(R.id.libraryButton)) {
            startLibraryActivity();
        } else if (view == findViewById(R.id.analyseButton)) {
            startAnalyseActivity();
        } else if (view == findViewById(R.id.websiteButton)) {
            startWebsiteActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri photoUri = data.getData();
        if (requestCode == libraryActivityResult && resultCode == RESULT_OK) {
            //Toast.makeText(this,data.getExtras().get("data").toString(),Toast.LENGTH_LONG).show();
            //Log.i(TAG, photoUri.toString());
            //photoUri.getScheme().toString();
            photoView.setImageURI(photoUri);
            //processPhotoLibraryResult(data);
            Uri imageUri = data.getData();
            // Flux pour lire les donnees de la carte SD
            try {
                InputStream inputStream;
                inputStream = getContentResolver().openInputStream(imageUri);
                // obtention d'une image Bitmap
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                ToCache(this, "/photoProjet", "image", image);
            }catch(Exception e){

            }
        }

        if (requestCode == captureActivityResult && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photoView.setImageBitmap(photo);
            Toast.makeText(this,data.getExtras().get("data").toString() ,Toast.LENGTH_LONG).show();
            ToCache(this,"/photoProjet","image", photo);
        }
        if (requestCode == ANALYSE_REQUEST_CODE && resultCode == RESULT_OK){
        }
    }

    protected void processPhotoLibrary(Intent intent) {
        Uri photoUri = intent.getData();
        String pathToPhoto = getRealPath(getApplicationContext(), photoUri);

        File pathToFile = new File(pathToPhoto);
        Bitmap photoBitmap = decodeFile(pathToFile); // err -> Maybe on path

        photoView.setImageBitmap(photoBitmap);

        Log.i(TAG, pathToPhoto);
    }

    protected void startAnalyseActivity() {
        startActivityForResult(analysisIntent, ANALYSE_REQUEST_CODE);
    }

    protected void startCaptureActivity() {
        //use standard intent to capture an image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //we will handle the returned data in onActivityResult
        startActivityForResult(intent, captureActivityResult);
    }

    protected void startLibraryActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select photo lib"), libraryActivityResult);
    }

    protected void startWebsiteActivity() {
        //lance internet
        Uri uri = Uri.parse("http://www.google.com/#"); //faire un tableau comprenant les differents sites web des marques, puis, selon la marque, aller chercher le bon site dans le tableau
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public  void ToCache(Context context, String Path, String fileName, Bitmap nfiles) {
        FileOutputStream output;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        nfiles.compress(Bitmap.CompressFormat.JPEG,100,bos);
        Path = this.getCacheDir() + "/" + fileName;
        Log.i(TAG,"@test"+Path);
        byte[] bitmapData = bos.toByteArray();
        try {
            output = new FileOutputStream(Path);
            output.write(bitmapData);
            output.close();
        } catch (IOException e) {
            Log.i(TAG,"@test lol");
            e.printStackTrace();
        }
        opencv_core.Mat matrix = imread(this.getCacheDir() + "/image");
        Log.i(TAG, "@tesMax"+matrix);

    }

    public void allPicturesToCache(String filename, int nb){
        FileOutputStream output;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bitmap;

        if(nb!=0){
            bitmap = BitmapFactory.decodeResource(getResources(), nb);
            if(bitmap.getWidth()>2000 || bitmap.getHeight()>3000 ){
                bitmap = getResizedBitmap( bitmap, bitmap.getWidth()/3,bitmap.getHeight()/3);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            byte[] bitmapData = bos.toByteArray();
            String Path = this.getCacheDir() + "/" + filename;

            try {
                Log.i(TAG,"@@W "+Path);
                output = new FileOutputStream(Path);
                output.write(bitmapData);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void declareImages(){
        allPicturesToCache("coca1",R.drawable.coca_1);
        allPicturesToCache("coca2",R.drawable.coca_2);
        allPicturesToCache("coca3",R.drawable.coca_3);
        allPicturesToCache("coca4",R.drawable.coca_4);
        allPicturesToCache("coca5",R.drawable.coca_5);
        allPicturesToCache("coca6",R.drawable.coca_6);
        allPicturesToCache("coca7",R.drawable.coca_7);
        allPicturesToCache("coca8",R.drawable.coca_8);
        allPicturesToCache("coca9",R.drawable.coca_9);
        allPicturesToCache("coca10",R.drawable.coca_10);
        allPicturesToCache("coca11",R.drawable.coca_11);
        allPicturesToCache("pepsi1",R.drawable.pepsi_1);
        allPicturesToCache("pepsi2",R.drawable.pepsi_2);
        allPicturesToCache("pepsi3",R.drawable.pepsi_3);
        allPicturesToCache("pepsi4",R.drawable.pepsi_4);
        allPicturesToCache("pepsi5",R.drawable.pepsi_5);
        allPicturesToCache("pepsi6",R.drawable.pepsi_6);
        allPicturesToCache("pepsi7",R.drawable.pepsi_7);
        allPicturesToCache("pepsi8",R.drawable.pepsi_8);
        allPicturesToCache("pepsi9",R.drawable.pepsi_9);
        allPicturesToCache("pepsi10",R.drawable.pepsi_10);
        allPicturesToCache("pepsi11",R.drawable.pepsi_11);
        allPicturesToCache("pepsi12",R.drawable.pepsi_12);
        allPicturesToCache("sprite1",R.drawable.sprite_1);
        allPicturesToCache("sprite2",R.drawable.sprite_2);
        allPicturesToCache("sprite3",R.drawable.sprite_3);
        allPicturesToCache("sprite4",R.drawable.sprite_4);
        allPicturesToCache("sprite5",R.drawable.sprite_5);
        allPicturesToCache("sprite6",R.drawable.sprite_6);
        allPicturesToCache("sprite7",R.drawable.sprite_7);
        allPicturesToCache("sprite8",R.drawable.sprite_8);
        allPicturesToCache("sprite9",R.drawable.sprite_9);
        allPicturesToCache("sprite10",R.drawable.sprite_10);
        allPicturesToCache("sprite11",R.drawable.sprite_11);
        allPicturesToCache("sprite12",R.drawable.sprite_12);
    }

    protected static String getRealPath(Context context, Uri uri) {
        Cursor cursor;

        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(dataIndex);
    }

    protected static Bitmap decodeFile(File file) {
        Bitmap bitmap = null;
        try {
            FileInputStream inputStream = new FileInputStream(file); // System.err
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaledWidth = ((float) newWidth)/width;
        float scaledHeight = ((float) newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaledWidth,scaledHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,width,height,matrix, false);
        return resizedBitmap;
    }
}