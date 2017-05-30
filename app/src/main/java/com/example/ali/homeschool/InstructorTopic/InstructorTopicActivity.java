package com.example.ali.homeschool.InstructorTopic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.homeschool.InstructorLessons.LessonModel;
import com.example.ali.homeschool.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class InstructorTopicActivity extends AppCompatActivity implements ImageClicked {
    int id = 0;
    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int PICK_SOUND_REQUEST = 235;

    private Uri filePath;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ArrayList<String> midLayouts;
    TextView submitTV;
    String m_Text = "";
    String audioLink = "";
    TextView image;
    String courseId;
    String lessonModel;
    TextView sound;
    LinearLayout act_main;
    LinearLayout mainView;
    String start = "<LinearLayout " +
            "android:orientation=\"vertical\" " +
            "android:layout_weight=\"0\" " +
            "android:id=\"2000\" " +
            "android:layout_width=\"match_parent\" " +
            "android:layout_height=\"match_parent\">";
    String mid = "";
    final String soundXML =
    "<Button android:layout_weight=\"0\" android:id=\"PUTIDHERE\" android:text=\"PUTSOUNDTEXTHERE\" android:layout_width=\"match_parent\" " +
            "android:layout_height=\"wrap_content\"" +
            "homeSchool:audioLink=\"PUTLINKHERE\" />";
    String soundText = "PlaceHolder";
    String end = "</LinearLayout>";
    String layout = "<LinearLayout " +
            "android:orientation=\"vertical\" " +
            "android:layout_weight=\"0\" " +
            "android:id=\"2000\" " +
            "android:layout_width=\"match_parent\" " +
            "android:layout_height=\"match_parent\">" +

            "<ImageView android:layout_weight=\"5\" android:id=\"1\" android:layout_width=\"match_parent\"" +
            " android:layout_height=\"wrap_content\" />" +

            "<LinearLayout android:orientation=\"horizontal\" android:layout_weight=\"1\" android:id=\"6\" " +
            "android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\">" +

            "<Button android:layout_weight=\"1\" android:id=\"12\" android:text=\"Meow\" android:layout_width=\"0\" " +
            "android:layout_height=\"match_parent\" />" +

            "<Button android:layout_weight=\"1\" android:id=\"1\" android:text=\"Cat\" android:layout_width=\"0\" " +
            "android:layout_height=\"match_parent\" />" +

            "</LinearLayout>" +
            "</LinearLayout>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_topic);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        submitTV = (TextView) findViewById(R.id.submit);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("lesson")) {
            lessonModel = intent.getStringExtra("lesson");
        }
        if (intent != null && intent.hasExtra("courseID")) {
            courseId = intent.getStringExtra("courseID");
        }
        submitTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = databaseReference.child("courses").child(courseId)
                        .child("lessons").child(lessonModel).child("topics");
                String key = databaseReference.push().getKey();
                TopicModel t = new TopicModel();
                t.setId(key);
                t.setName("Name");
                t.setLayout(start + midLayouts.toString() + end);
                databaseReference.child(key).updateChildren(t.toMap());
                finish();
            }
        });
        midLayouts = new ArrayList<>();
        act_main = (LinearLayout) findViewById(R.id.activiy_main);
        mainView = (LinearLayout) findViewById(R.id.mainLayout);
        image = (TextView) findViewById(R.id.image);
        sound = (TextView) findViewById(R.id.sound);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        InstructorTopicActivity.this);
                builder.setTitle("Select image");
                LayoutInflater li = LayoutInflater.from(InstructorTopicActivity.this);
                LinearLayout someLayout = (LinearLayout) li.inflate(R.layout.image_dialog, null);
                builder.setView(someLayout);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                TextView gallery = (TextView) someLayout.findViewById(R.id.choosefromGallery);
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        openImageActivity();
                    }
                });

                TextView urlTV = (TextView) someLayout.findViewById(R.id.imageUrl);
                urlTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        openImageURLDialog();
                    }
                });

            }
        });
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        InstructorTopicActivity.this);
                builder.setTitle("Select sound file");
                LayoutInflater li = LayoutInflater.from(InstructorTopicActivity.this);
                LinearLayout someLayout = (LinearLayout) li.inflate(R.layout.sound_dialog, null);
                final EditText soundET = (EditText) someLayout.findViewById(R.id.soundtext);
                builder.setView(someLayout);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                TextView gallery = (TextView) someLayout.findViewById(R.id.choosefromGallery);
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        soundText = soundET.getText().toString();
                        openSoundActivity();
                    }
                });

                TextView urlTV = (TextView) someLayout.findViewById(R.id.imageUrl);
                urlTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        soundText = soundET.getText().toString();
                        openSoundURLDialog();
                    }
                });


            }
        });
    }

    public LinearLayout parse(String layout) {
        InputStream stream = null;
        stream = new ByteArrayInputStream(layout.getBytes(Charset.forName("UTF-8")));
        ParseXML parseXML = new ParseXML();
        LinearLayout mainLayout = null;

        try {
            mainLayout = (LinearLayout) parseXML.parse(stream, getApplicationContext(), this);
            Log.v("ITA", "pass");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.v("ITA", "XML ERROR :" + e);
        } catch (IOException e) {
            Log.v("ITA", "XML IO ERROR :" + e);
        }
        return mainLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Log.v("Instructor",
                        "Req : " + requestCode + " Res :" + resultCode + " Intent : " + data
                                .getData().toString());
                filePath = data.getData();
                uploadFile();
            }
            if(requestCode == PICK_SOUND_REQUEST){
                Log.v("Instructor",
                        "Req : " + requestCode + " Res :" + resultCode + " Intent : " + data
                                .getData().toString());
                filePath = data.getData();
                uploadFile("sounds",filePath,soundXML);
            }
        }

    }
    private void openSoundURLDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(InstructorTopicActivity.this);
        builder.setTitle("Sound Button");
        LayoutInflater li = LayoutInflater.from(InstructorTopicActivity.this);
        LinearLayout someLayout;
        someLayout = (LinearLayout) li.inflate(R.layout.dialog_button, null);

        // Set up the input
//                final EditText input = new EditText(MainActivity.this);
//                final EditText audioIn = new EditText(MainActivity.this);
        final EditText input = (EditText) someLayout.findViewById(R.id.text);
        final EditText audioIn = (EditText) someLayout.findViewById(R.id.audio);

//                input.setHint("Text");
//                audioIn.setHint("Audio Link");
//                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(
//                        InputType.TYPE_CLASS_TEXT );
//                audioIn.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(someLayout);


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                m_Text = input.getText().toString();
                audioLink = audioIn.getText().toString();
                mid = "<Button android:layout_weight=\"0\" android:id=\"" + id
                        + "\" android:text=\"" + soundText + "\" android:layout_width=\"match_parent\" " +
                        "android:layout_height=\"wrap_content\"" +
                        "homeSchool:audioLink=\"" + audioLink + "\" />";
                midLayouts.add(id, mid);

                id++;
                LinearLayout linearLayout = parse(start + midLayouts.toString() + end);


                mainView.removeAllViews();
                mainView.addView(linearLayout);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void openSoundActivity(){
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Sound"),
                PICK_SOUND_REQUEST);
    }
    private void openImageURLDialog(){
        final EditText input = new EditText(InstructorTopicActivity.this);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT);
        final AlertDialog.Builder urlBuilder = new AlertDialog.Builder(InstructorTopicActivity.this);
        urlBuilder.setTitle("Title");
        urlBuilder.setView(input);
        urlBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                mid = "<ImageView android:layout_weight=\"1\" android:id=\"" + id
                        + "\" android:layout_width=\"match_parent\"" +
                        " android:layout_height=\"wrap_content\" homeSchool:src=\"" + m_Text + "\" />";
                midLayouts.add(id, mid);
                id++;
                LinearLayout linearLayout = parse(
                        start + midLayouts.toString() + end);
                mainView.removeAllViews();
                mainView.addView(linearLayout);
            }
        });
        urlBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        urlBuilder.show();

    }
    private void openImageActivity(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST);
    }
    @Override
    public void onClick(View v) {
        Log.v("Test", "COUNT BF " + mainView.getChildCount());
        Log.v("Test", "id " + v.getId());
//        v.setVisibility(View.INVISIBLE);
        mainView.removeView(v);
        int mainId = v.getId();
        Log.v("Test", "Main ID :" + v.getId());
        LinearLayout v0 = (LinearLayout) mainView.getChildAt(0);
        Log.v("Test", "V0 id " + v0.getId());
        v0.removeView(v);
//        LinearLayout view =(LinearLayout) mainView.findViewById(2000);
//        view.removeView(v);
//        mainView.removeView((View) view.getParent());
//        mainView.removeViewInLayout(view);

        act_main.removeView(v);
//        mainView.removeViewAt(v.getInd);
        mid = "";
        mainView.invalidate();
        Log.v("Test", "COUNT AF " + mainView.getChildCount());
//        midLayouts.get(v.getId());
        midLayouts.remove(v.getId());
        --id;
        Log.v("Test", "mid Layouts " + midLayouts);
    }
    private void addLayout(String link,String layout){
        link = link.replaceAll("&","&amp;");
//      link = link.replaceAll("&#63;", "?");
        layout =layout.replaceAll("PUTLINKHERE",link);
        layout =layout.replaceAll("PUTIDHERE", String.valueOf(id));
        layout = layout.replaceAll("PUTSOUNDTEXTHERE",soundText);
        midLayouts.add(id, layout);
        id++;
        LinearLayout linearLayout = parse(start +midLayouts.toString() + end);
        mainView.removeAllViews();
        mainView.addView(linearLayout);
    }
    private void uploadFile(String path, Uri filePath, final String layout){
        //if there is a file to upload
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = storageReference.
                    child(path +"/" + courseId + "/" + UUID.randomUUID() + getFileName(
                            getApplicationContext(), filePath));
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            @SuppressWarnings("VisibleForTests") String link = taskSnapshot
                                    .getDownloadUrl().toString();
                            addLayout(link,layout);
                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded",
                                    Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            @SuppressWarnings("VisibleForTests") double progress = (100.0 * taskSnapshot
                                    .getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }
    private void uploadFile() {
        //if there is a file to upload
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            Log.v("ITA", "Type: " + getMimeType(getApplicationContext(), filePath));
            Log.v("ITA", "FileName: " + getFileName(getApplicationContext(), filePath));

            StorageReference riversRef = storageReference.
                    child("images/" + courseId + "/" + UUID.randomUUID() + getFileName(
                            getApplicationContext(), filePath));
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            @SuppressWarnings("VisibleForTests") String link = taskSnapshot
                                    .getDownloadUrl().toString();
                            link = link.replaceAll("&","&amp;");
//                            link = link.replaceAll("&#63;", "?");
                            mid = "<ImageView android:layout_weight=\"1\" android:id=\"" +id
                                    + "\" android:layout_width=\"match_parent\"" +
                                    " android:layout_height=\"wrap_content\" homeSchool:src=\"" + link + "\" />";
                            midLayouts.add(id, mid);
                            id++;
                            LinearLayout linearLayout = parse(start +midLayouts.toString() + end);
                            mainView.removeAllViews();
                            mainView.addView(linearLayout);
                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded",
                                    Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            @SuppressWarnings("VisibleForTests") double progress = (100.0 * taskSnapshot
                                    .getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap
                    .getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}