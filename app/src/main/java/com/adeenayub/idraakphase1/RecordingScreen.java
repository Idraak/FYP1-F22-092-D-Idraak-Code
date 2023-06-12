package com.adeenayub.idraakphase1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingScreen extends AppCompatActivity implements TextToSpeech.OnInitListener{
ImageView record,camera,uploadpicture;
//video stuff
private static int CAMERA_PERMISSION_CODE = 100;
private static int VIDEO_RECORD_CODE = 101;
private Uri videoPath, imagePath;
VideoView uploadvideo;

//popup
private AlertDialog.Builder dialogBuilder;
private AlertDialog dialog;
private TextView text_popupTitle, text_popupDescription,help_popupTitle,help_popupDescription;
private Button text_cancelButton,help_cancelButton;

//text to speech
String selectedImagePath;
private static final int REQUEST_IMAGE_CAPTURE = 1;
Uri imageUri;
private TextToSpeech textToSpeech;
Button mButtonSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_screen);

        record = findViewById(R.id.record_button);
        camera = findViewById(R.id.camera_button);
        uploadpicture = findViewById(R.id.taken_picture);
        uploadvideo = findViewById(R.id.taken_video);

        mButtonSpeak = findViewById(R.id.speak);
        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this::onInit);


        camera.setOnClickListener(v -> {
            //using this for image capture for now
            Toast.makeText(this, "Camera clicked", Toast.LENGTH_SHORT).show();
            if (isCameraPresentInPhone()) {
                Log.i("Image_capture_tag", "Camera is detected");
                getCameraPermission();
                selectImage(v);
            } else {
                Log.i("Image_capture_tag", "Camera is not detected");
            }
        });
//
//        record.setOnClickListener(v -> {
//            Toast.makeText(this, "Record clicked", Toast.LENGTH_SHORT).show();
//            if (isCameraPresentInPhone()) {
//                Log.i("Video_record_tag", "Camera is detected");
//                getCameraPermission();
//                recordvideo();
//            } else {
//                Log.i("Video_record_tag", "Camera is not detected");
//            }
//        });
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.text_option:
                Toast.makeText(this, "Text option selected", Toast.LENGTH_SHORT).show();
                createTextPopup();
                return true;
            case R.id.audio_option:
                Toast.makeText(this, "Audio option selected", Toast.LENGTH_SHORT).show();
                Intent iaudio = new Intent(RecordingScreen.this, audio_player.class);
                startActivity(iaudio);
                return true;
            case R.id.dictionary_option:
                Toast.makeText(this, "Dictionary option selected", Toast.LENGTH_SHORT).show();
                Intent idic = new Intent(RecordingScreen.this, DictionaryContents.class);
                startActivity(idic);
                return true;
            case R.id.help_option:
                Toast.makeText(this, "Help option selected", Toast.LENGTH_SHORT).show();
                createHelpPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void createTextPopup(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View textPopupView = getLayoutInflater().inflate(R.layout.text_popup, null);
        text_popupTitle = textPopupView.findViewById(R.id.text_popup_title);
        text_popupDescription = textPopupView.findViewById(R.id.text_popup_description);
        text_cancelButton = textPopupView.findViewById(R.id.text_popup_button);

        dialogBuilder.setView(textPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        text_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void createHelpPopup(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View helpPopupView = getLayoutInflater().inflate(R.layout.help_popup, null);
        help_popupTitle = helpPopupView.findViewById(R.id.help_popup_title);
        help_popupDescription = helpPopupView.findViewById(R.id.help_popup_description);
        help_cancelButton = helpPopupView.findViewById(R.id.help_popup_button);

        dialogBuilder.setView(helpPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        help_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

//video stuff
    private boolean isCameraPresentInPhone() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    private void getCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void recordvideo(){
        Intent irecord=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(irecord,VIDEO_RECORD_CODE);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == VIDEO_RECORD_CODE) {
//            if (resultCode == RESULT_OK) {
//                //display video
//                uploadvideo.setVideoURI(data.getData());
//                //uploadvideo.start();
//                MediaController mediaController = new MediaController(this);
//                uploadvideo.setMediaController(mediaController);
//                mediaController.setAnchorView(uploadvideo);
//                //storage
//                videoPath = data.getData();
//                Log.i("Video_record_tag","Video recorded at path " + videoPath);
//                Toast.makeText(this, "Video recorded successfully", Toast.LENGTH_SHORT).show();
//            }
//            else if (resultCode == RESULT_CANCELED) {
//                Log.i("Video_record_tag","Video recording cancelled");
//                Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Log.i("Video_record_tag","Video recording failed");
//                Toast.makeText(this, "Video recording failed", Toast.LENGTH_SHORT).show();
//            }
//        }

    //text-to-speech and flask
    public void connectServer(View view) {
        String postUrl = "http://192.168.1.4:5000/upload";
//        String postBodyText = "Hello";
//        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
//        RequestBody postBody = RequestBody.create(mediaType, postBodyText);

        // Check if an image is captured
        if (imageUri != null) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody postBodyImage = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "androidFlask1.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                        .build();

                TextView responseText = findViewById(R.id.responseText);
                responseText.setText("Please wait ...");

                // Execute the network operation in an AsyncTask
                new NetworkTask().execute(postUrl, postBodyImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Capture an image first.", Toast.LENGTH_SHORT).show();
        }

//        //image
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        // Read BitMap by file path
//        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        RequestBody postBodyImage = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image", "androidFlask1.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                .build();
//
//        TextView responseText = findViewById(R.id.responseText);
//        responseText.setText("Please wait ...");
//
//        // Execute the network operation in an AsyncTask
//        new NetworkTask().execute(postUrl, postBodyImage);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("ur"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported.", Toast.LENGTH_SHORT).show();
            } else {
                mButtonSpeak.setEnabled(true);
            }
        } else {
            Toast.makeText(this, "TextToSpeech initialization failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NetworkTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String postUrl = (String) params[0];
            RequestBody postBody = (RequestBody) params[1];

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //        @SuppressLint("SetTextI18n")
//        @Override
//        protected void onPostExecute(String result) {
//            if (result != null) {
//                TextView responseText = findViewById(R.id.responseText);
//                responseText.setText(result);
//            } else {
//                TextView responseText = findViewById(R.id.responseText);
//                responseText.setText("Failed to Connect to Server");
//            }
//        }
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject responseJson = new JSONObject(result);
                    String message = responseJson.optString("message");

                    if (message.equals("Image processed successfully!")) {
                        //String uploadedImageBase64 = responseJson.optString("uploaded_image");
                        //String processedImageBase64 = responseJson.optString("processed_image");
                        String predictedLetter = responseJson.optString("prediction");

                        // Convert base64 strings to bitmaps
                        //Bitmap uploadedImageBitmap = decodeBase64Image(uploadedImageBase64);
                        //Bitmap processedImageBitmap = decodeBase64Image(processedImageBase64);

                        // Set the images in image views
                        //ImageView uploadedImageView = findViewById(R.id.uploadedImageView);
                        //ImageView processedImageView = findViewById(R.id.processedImageView);
                        //uploadedImageView.setImageBitmap(uploadedImageBitmap);
                        //processedImageView.setImageBitmap(processedImageBitmap);

                        // Set the predicted letter in a text view
                        TextView predictedLetterTextView = findViewById(R.id.predictedLetterTextView);
                        predictedLetterTextView.setText(predictedLetter);

//                        // Speak the converted text
//                        textToSpeech.speak(predictedLetter, TextToSpeech.QUEUE_FLUSH, null);

                    } else {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Image processing failed.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    TextView responseText = findViewById(R.id.responseText);
                    responseText.setText("Failed to parse server response.");
                }
            } else {
                TextView responseText = findViewById(R.id.responseText);
                responseText.setText("Failed to connect to the server.");
            }
        }

        private Bitmap decodeBase64Image(String base64Image) {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }
    }

    public void selectImage(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadpicture.setImageBitmap(imageBitmap);

            // Create a file to store the captured image
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Save the image to the file
            if (imageFile != null) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    selectedImagePath = imageFile.getAbsolutePath();
                    TextView imgPath = findViewById(R.id.imgPath);
                    imgPath.setText(selectedImagePath);

                    imageUri = Uri.fromFile(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    // Stop TextToSpeech and release resources
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void playAudio(View view) {
        String predictedLetter = ((TextView) findViewById(R.id.predictedLetterTextView)).getText().toString();
//        textToSpeech.speak(predictedLetter, TextToSpeech.QUEUE_FLUSH, null);
        textToSpeech.speak(predictedLetter, TextToSpeech.QUEUE_FLUSH, null);
    }


//    public void selectImage(View v) {
//        Intent intent = new Intent();
//        intent.setType("*/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, 0);
//    }
//
//    @Override
//    protected void onActivityResult(int reqCode, int resCode, Intent data) {
//        super.onActivityResult(reqCode, resCode, data);
//        if (resCode == RESULT_OK && data != null) {
//            Uri uri = data.getData();
//
//            selectedImagePath = getPath(getApplicationContext(), uri);
//            EditText imgPath = findViewById(R.id.imgPath);
//            imgPath.setText(selectedImagePath);
//            Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();
//        }
//    }
//

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}