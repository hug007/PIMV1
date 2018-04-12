package hop.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.R;

import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.bytedeco.javacpp.opencv_shape;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_xfeatures2d.*;

import org.json.JSONException;
import org.json.JSONObject;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_features2d.BFMatcher;
//import org.bytedeco.javacpp.opencv_features2d.DrawMatchesFlags;
//import static org.bytedeco.javacpp.opencv_features2d.drawKeypoints;
import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core.Mat;



public class Analyse extends AppCompatActivity implements View.OnClickListener {
private static String urlmarque;
    final String TAG = Analyse.class.getName();
    public void onClick(View view) {
    }

    ImageView logo;
    static Button websiteButton;
    static TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyse);

        logo = (ImageView) findViewById(R.id.logo);
        startAnalyseActivity(logo);
        websiteButton = (Button) findViewById(R.id.websiteButton);

        websiteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(urlmarque));
                startActivity(viewIntent);
            }
        });
    }

    public void startAnalyseActivity(ImageView logo) {
       Mat matrix = OpenImRead("image");
        //Mat matrix = imread(this.getCacheDir() + "/image");

        Log.i(TAG, "@W"+matrix);
        Mat[] tableauMatrices = new Mat[] {
                OpenImRead("coca1"),
                OpenImRead("coca2"),
                OpenImRead("coca3"),
                OpenImRead("coca4"),
                OpenImRead("coca5"),
                OpenImRead("coca6"),
                OpenImRead("coca7"),
                OpenImRead("coca8"),
                OpenImRead("coca9"),
                OpenImRead("coca10"),
                OpenImRead("coca11"),
                OpenImRead("pepsi1"),
                OpenImRead("pepsi2"),
                OpenImRead("pepsi3"),
                OpenImRead("pepsi4"),
                OpenImRead("pepsi5"),
                OpenImRead("pepsi6"),
                OpenImRead("pepsi7"),
                OpenImRead("pepsi8"),
                OpenImRead("pepsi9"),
                OpenImRead("pepsi10"),
                OpenImRead("pepsi11"),
                OpenImRead("pepsi12"),
                OpenImRead("sprite1"),
                OpenImRead("sprite2"),
                OpenImRead("sprite3"),
                OpenImRead("sprite4"),
                OpenImRead("sprite5"),
                OpenImRead("sprite6"),
                OpenImRead("sprite7"),
                OpenImRead("sprite8"),
                OpenImRead("sprite9"),
                OpenImRead("sprite10"),
                OpenImRead("sprite11"),
                OpenImRead("sprite12")
        };
        Log.i(TAG, "@test nok"+tableauMatrices[1]);
        int GroupeClassification[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 }; // liste qui associe un groupe à chaque image d'entrainement
        float ImageClassification[][] = new float[3][tableauMatrices.length];
        float ImageClassificationSort[][] = new float[3][tableauMatrices.length];

// -------------- Issu du TP4 --------------
        // Paramètres des points SIFT
        int nFeatures = 0;
        int nOctaveLayers = 3;
        double contrastThreshold = 0.03;
        int edgeThreshold = 10;
        double sigma = 1.6;

        Loader.load(opencv_calib3d.class);
        Loader.load(opencv_shape.class);

        // -------------- debut source TP4 --------------
        // Déclaration SIFT
        SIFT sift;
        sift = SIFT.create(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
        // -------------- Fin source TP4 --------------

        // Vecteurs Keypoints
        KeyPointVector[] keypoints = new KeyPointVector[tableauMatrices.length];
        Mat[] descriptors = new Mat[tableauMatrices.length];

        KeyPointVector ImageTestkeypoints = new KeyPointVector();
        Mat ImageTestdescriptors = new Mat();
        sift.detect(matrix, ImageTestkeypoints); // il permet de detecter les keypoints
        sift.compute(matrix, ImageTestkeypoints, ImageTestdescriptors); //il permet de calculer les descriptors

        // -------------- debut source TP4 --------------
        BFMatcher matcher = new BFMatcher(NORM_L2, false);
        DMatchVector matches = new DMatchVector();
        // -------------- Fin source TP4 --------------

        float distance = 0;
        // crée une boucle for {
        for (int i = 0; i < tableauMatrices.length; i++) {
            // pour chaque image du tableau il faut créer un nouveau keypoints et un nouveau
            // descriptor
            keypoints[i] = new KeyPointVector();
            descriptors[i] = new Mat();
            Log.i(TAG, "@W "+i);
            Log.i(TAG, "@W "+tableauMatrices[i] + " key "+keypoints[i]);
            // ensuite un detect et un compute
            sift.detect(tableauMatrices[i], keypoints[i]); // il permet de detecter les keypoints
            sift.compute(tableauMatrices[i], keypoints[i], descriptors[i]); //il permet de calculer les descriptors

            // ensuite une methode match (cf SIFT matching)
            matcher.match(ImageTestdescriptors, descriptors[i], matches); // Issu du TP4

            DMatchVector bestMatch = selectBest(matches, 30); // en paramètre, le nombre de points SIFT pour la
            // comparaison

            // on additionne chaque vecteur calculé par selectBest et stocké dans bestMatch
            // pour obtenir la distance
            for (int i1 = 0; i1 < bestMatch.size(); i1++) {
                distance += bestMatch.get(i1).distance();
            }

            // ensuite il faut calculer la distance moyenne
            distance = distance / bestMatch.size();
            ImageClassification[0][i] = i;
            ImageClassification[1][i] = GroupeClassification[i];
            ImageClassification[2][i] = distance;
            ImageClassificationSort[0][i] = i;
            ImageClassificationSort[1][i] = GroupeClassification[i];
            ImageClassificationSort[2][i] = distance;
        } // fin du for

        // Tri du tableau ImageClassificationSort
        Arrays.sort(ImageClassificationSort[2]);

        // Réorganisation du tableau ImageClassificationSort
        for (int i = 0; i < tableauMatrices.length; i++) {
            for (int j = 0; j < tableauMatrices.length; j++) {
                if (ImageClassificationSort[2][i] == ImageClassification[2][j]) {
                    ImageClassificationSort[0][i] = ImageClassification[0][j];
                    ImageClassificationSort[1][i] = ImageClassification[1][j];
                }
            }
        }

        int groups[] = { 0, 0, 0 };
        int group = 0;

        for (int i = 0; i < 5; i++) { // le nombre d'iterations du for correspond au KNN
            group = (int) ImageClassificationSort[1][i];
            groups[group - 1]++;
        }

        // On associe le groupe de l'image testée au groupe avec la plus petite distance moyenne
        int min = 0, finalGroup = 0;
        for (int i = 0; i < 3; i++) {
            if (groups[i] > min) {
                min = groups[i];
                finalGroup = i + 1;
            } else if (min == groups[i]) {
                min = groups[i];
                finalGroup = (int) ImageClassificationSort[1][0];
            }
        }

        Log.i(TAG, "@W final grp"+finalGroup);
        switch(finalGroup){
            case 1:
                photo("coca1");
                urlmarque="http://www.coca-cola.com/global/";
                break;
            case 2:
                photo("pepsi1");
                urlmarque="https://pepsi.fr/";
                break;
            case 3:
                photo("sprite1");
                urlmarque="https://www.sprite.com/";
                break;
        }
    }

    public void change(String brand){
        String ts = this.getCacheDir()+ "/"+ brand;
        File tfile = new File(ts);
        Log.i(TAG,"@W"+tfile.exists());
        String filePath = tfile.getPath();
        Log.i(TAG,"@W"+filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        //Log.i(TAG,"@W"+bitmap.getHeight()+" "+logo.toString());
       //Set logo.setImageBitmap(bitmap);
    }

    public Mat OpenImRead(String nameFile){
        Mat test = null;
        String pathFile = this.getCacheDir()+"/"+nameFile;
        File tfile = new File(pathFile);
        String filePath = tfile.getPath();
        if(tfile.exists()){
            try{
                test = imread(filePath);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return test;
    }

    // Méthode selectBest redigee a partir de la page 3 du TP4
    public static DMatchVector selectBest(DMatchVector matches, int numberToSelect) {
        DMatch[] sorted = toArray(matches);
        Arrays.sort(sorted, (a, b) -> {
            return a.lessThan(b) ? -1 : 1;
        });
        DMatch[] best = Arrays.copyOf(sorted, numberToSelect);

        return new DMatchVector(best);
    }

    // Méthode toArray redigee a partir de la page 3 du TP4
    public static DMatch[] toArray(DMatchVector matches) {
        assert matches.size() <= Integer.MAX_VALUE;
        int n = (int) matches.size();
        // Convert keyPoints to Scala sequence
        DMatch[] result = new DMatch[n];
        for (int i = 0; i < n; i++) {
            result[i] = new DMatch(matches.get(i));
        }
        return result;
    }

    public static void  setURL(String url){
        urlmarque =url;
    }

    public void photo (String image){
        Uri myUri = Uri.parse(this.getCacheDir() + "/" + image);
        logo.setImageURI(myUri);
        //Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.coca_1);
        //logo.setImageBitmap(bitmap);
       // logo.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.coca_1));
    }

    @Override
    public void onBackPressed() {
        Intent mainIntent;
        mainIntent = new Intent(Analyse.this, MainActivity.class);
        startActivity(mainIntent);
        return;
    }

    public static String getFileContents(final File file) throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder stringBuilder = new StringBuilder();
        boolean done = false;
        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }
        reader.close();
        inputStream.close();
        return stringBuilder.toString();
    }

}