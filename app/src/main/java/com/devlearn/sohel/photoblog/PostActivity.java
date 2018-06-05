package com.devlearn.sohel.photoblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

public class PostActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private AppCompatEditText txtPostTitle, txtPostDesc;
    private Button submitPost;

    private Uri mImageUri =null;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private static final int GALLERY_REQUEST = 1;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        imageButton = (ImageButton)findViewById(R.id.post_image);
        txtPostTitle = findViewById(R.id.post_title);
        txtPostDesc = findViewById(R.id.post_des);
        submitPost = findViewById(R.id.submit_post);
        mProgressBar = findViewById(R.id.progressBar);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }

    private void startPosting() {

        final String title_val = txtPostTitle.getText().toString().trim();
        final String desc_val = txtPostDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null)
        {
            mProgressBar.setVisibility(View.VISIBLE);

            final StorageReference filepath = mStorage.child("Blog_Images").child(random()+".jpg");

            /*from google doc*/
            Log.d("image name", String.valueOf(mImageUri));
            UploadTask uploadTask = filepath.putFile(mImageUri);
            mProgressBar.setVisibility(View.VISIBLE);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        DatabaseReference newPost = mDatabase.push();

                        newPost.child("title").setValue(title_val);
                        newPost.child("desc").setValue(desc_val);
                        newPost.child("image").setValue(downloadUri.toString());

                        Toast.makeText(PostActivity.this, "Success!!", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                        finish();
                    } else {
                        // Handle failures
                        String error = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            /* from tutorila*/
//            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUri = taskSnapshot.getUploadSessionUri();
//                    mProgressBar.setVisibility(View.INVISIBLE);
//                }
//            });
//            filepath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                    if(task.isSuccessful())
//                    {
//                        UploadTask.TaskSnapshot downloadUri = task.getResult();
//                        Toast.makeText(PostActivity.this, "Success!!", Toast.LENGTH_SHORT).show();
//
//                    }
//                    else
//                    {
//                        String error = task.getException().getMessage();
//                        Toast.makeText(PostActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
        }
        else
        {
            Toast.makeText(this, "Insert image and text", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            mImageUri = data.getData();
            imageButton.setImageURI(mImageUri);
        }
    }
    public static String random() {
        int MAX_LENGTH = 9;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
