package com.sdhaxwoo.mathsolvercamera;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    private final int ANALYZED_PHOTO = R.raw.text_test;
    private byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamera = (Button) findViewById (R.id.btnCamera);
        imageView = (ImageView) findViewById(R.id.imageView);


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
//                startActivity(intent);

            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //turn bitmap into array of bytes --> jpg
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteArray = stream.toByteArray();
        VisionWork();

        imageView.setImageBitmap(bitmap);
    }

    private void VisionWork(){
        //sets up the Google Vision API
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        //Initializes and verifies the visionBuilder
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(APIKEY.API_KEY));
        Log.i("Info", "Initialized vision build w/ API key successful");

        //Builds the visionBuilder
        final Vision vision = visionBuilder.build();

        // Creates photoData
        final byte[][] photoData = new byte[1][1];
        Log.i("Info", "Creates photoData");

        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Convert photo to byte array
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
                try {
                    photoData[0] = IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Encoding Image
                Image inputImage = new Image();
                inputImage.encodeContent(photoData[0]);
                Log.i("Info", "Encoding image");

                // Distinguishes either FACE DETECTION or TEXT DETECTION
                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");
                Log.i("Info", "Sets desiredFeature successfully");

                // Requests the image to be analyzed
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                Log.i("Info", "Prepared image to be processed");

                BatchAnnotateImagesRequest batchRequest =
                        new BatchAnnotateImagesRequest();

                batchRequest.setRequests(Arrays.asList(request));

                BatchAnnotateImagesResponse batchResponse = null;
                Log.i("Info", "Batch prepares");

                try {
                    batchResponse = vision.images().annotate(batchRequest).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i("Info", "batchRequest is executed");
                assert batchResponse != null;
                final TextAnnotation text = batchResponse.getResponses()
                        .get(0).getFullTextAnnotation();
                Log.i("Info", "Successfully detects text");


                Log.d("Outtie", text.getText());

                Log.i("Info", "Shows the text");
            }
        });
    }
}

