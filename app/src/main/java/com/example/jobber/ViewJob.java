package com.example.jobber;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;


public class ViewJob extends AppCompatActivity {

    //private ImageView image;
    private LinearLayout smallImages;
    private EditText name;
    private EditText description;
    private File fileDir;

    private Button importance;
    private Button status;
    private Button deleteJob;
    private TextView creationDate;

    private boolean newChangesToJob = false;

    private ViewSwitcher viewSwitcher;
    private ImageView image1;
    private ImageView image2;

    private boolean newJob = false;
    private ImageView gifImageView = null;
    static final int CAPTURE_IMAGE = 1;
    Job job;
    static private boolean canYouTakeImage = true;


    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job);

        Intent intent = getIntent();
        long jobId = intent.getLongExtra("job", 0);

        fileDir = new File(getExternalFilesDir(null), "jobs/");
        refreshJobObject(jobId);

       // image = findViewById(R.id.imageView);


        smallImages = findViewById(R.id.smallImages);
        name = findViewById(R.id.EditTextName);
        description = findViewById(R.id.EditTextDescription);

        importance = findViewById(R.id.importance);
        status = findViewById(R.id.status);
        deleteJob = findViewById(R.id.archiveJob);
        creationDate = findViewById(R.id.creationDate);

        viewSwitcher = findViewById(R.id.viewSwitcher);

        name.setHint(job.getJobName());


        if(job.getText().equals("...")){
            description.setHint(job.getText());
        } else {
            description.setText(job.getText());
        }

        placeMainImage(0);
        viewSwitcher.showNext();
        placeMainImage(0);


        placeAllSmallImages();
        setImportance();
        setStatus();

        creationDate.setText(job.getCreationDateString());


        // if its a new job, take a photo immediately
        if(intent.hasExtra("new") && canYouTakeImage){
            newJob = intent.getBooleanExtra("new", false);
            if(newJob && job.getNumberOfImages() == 0) {
                canYouTakeImage = false;
                takePhoto();
            }
        }
    }


    private void placeMainImage(int i){


        ImageView nextImage = (ImageView) viewSwitcher.getNextView();
        Glide
                .with(this)
                .load(job.getPhotoFile(fileDir, i))
                .placeholder(R.drawable.missing_image)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewSwitcher.showNext();

                        return false;
                    }
                })
                .into(nextImage);



        // Glide.with(this).load(job.getPhotoFile(fileDir, i)).placeholder(R.drawable.missing_image).into(image);
    }

    private void placeAllSmallImages(){
        for(int i = 0; i < job.getNumberOfImages(); i++){
            addSmallImagePhoto(i);
        }
    }

    private void addSmallImagePhoto(int photoNumber){
        ImageView imageSmall = new ImageView(getApplicationContext());
        imageSmall.setId(photoNumber);



        Glide.with(this).load(job.getPhotoFile(fileDir, photoNumber)).placeholder(R.drawable.missing_image).into(imageSmall);


        smallImages.addView(imageSmall);


        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) imageSmall.getLayoutParams();
        marginParams.setMargins(4, 4, 4, 4);



        imageSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeMainImage(v.getId());
            }
        });
    }

    public void archiveJob(View view){
        new AlertDialog.Builder(this)
                .setTitle("Arkivere jobb")
                .setMessage("Vil du arkivere jobben?")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("jobId", job.getJobId());
                        setResult(456677, returnIntent);
                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void deleteJob(View view){

        new AlertDialog.Builder(this)
                .setTitle("Slette jobb")
                .setMessage("Vil du slette jobben? Jobben blir kun slettet lokalt, ikkje på serveren")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        int result = db.deleteJob(job);
                        db.close();

                        if(result > 0)
                        {
                            File jobDir = new File(getExternalFilesDir(null), "jobs/");
                            for(int i = 0; i < job.getNumberOfImages(); ++i)
                            {
                                File file = new File( jobDir,job.stringID() + "_" + String.valueOf(i) );
                                file.delete();
                            }
                        }

                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void deleteJob(){

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        int result = db.deleteJob(job);
        db.close();

        if(result > 0)
        {
            File jobDir = new File(getExternalFilesDir(null), "jobs/");
            for(int i = 0; i < job.getNumberOfImages(); ++i)
            {
                File file = new File( jobDir,job.stringID() + "_" + String.valueOf(i) );
                file.delete();
            }
        }
        finish();
    }

    public void refreshJobObject(long jobId){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        job = db.getJob(jobId);
        db.close();
    }





    public void returnButton(View view) {
        finish();
    }

    public void newPhotoButton(View view) {
        takePhoto();
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File img = createImageFile();
            if (img != null) {
                Uri uri = FileProvider.getUriForFile(this, "eggum.jobber.fileprovider", img);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        }
    }

    public void setImportance(){
        int i = job.getImportance();
        if(i == 1){
            importance.setText("Uviktig");
            importance.setBackgroundColor(Color.LTGRAY);
        } else if(i == 2){
            importance.setText("Viktig");
            importance.setBackgroundColor(0xFFFAA500);
        } else if(i == 3){
            importance.setText("Haster");
            importance.setBackgroundColor(Color.RED);
        }
    }

    public void setStatus(){
        int i = job.getStatus();
        if(i == 1){
            status.setText("Uferdig");
            status.setBackgroundColor(Color.YELLOW);
        } else if (i == 2){
            status.setText("Ferdig!");
            status.setBackgroundColor(Color.GREEN);
            gif();
        }
    }

    public void gif(){

        if(gifImageView == null){
            gifImageView = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    /*width*/ ViewGroup.LayoutParams.MATCH_PARENT,
                    /*height*/ ViewGroup.LayoutParams.MATCH_PARENT
            );

            ConstraintLayout parent = findViewById(R.id.parent);
            parent.addView(gifImageView, param);

        }
        Glide
                .with(this)
                .asGif()
                .load(R.drawable.giphy)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            resource.setLoopCount(2);
                        }
                        return false;
                    }
                })
                .into(gifImageView);
    }

    public void changeStatus(View view){
        int i = job.getStatus();
        int newI = i + 1;
        if(newI == 3){
            newI = 1;
        }
        job.setStatus(newI);
        newChangesToJob = true;
        setStatus();
    }

    public void changeImportance(View view){
        int i = job.getImportance();
        int newI = i + 1;
        if(newI == 4){
            newI = 1;
        }
        job.setImportance(newI);
        newChangesToJob = true;
        setImportance();
    }

    private File createImageFile() {
        File jobDir = new File(getExternalFilesDir(null), "jobs/");
        Log.d("ViewJob", "few" + jobDir.getAbsolutePath());
        File file = new File(jobDir, job.stringID() + "_" + (job.getNumberOfImages()));

        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ){
        if(requestCode == CAPTURE_IMAGE){

            File jobDir = new File(getExternalFilesDir(null), "jobs/");
            File file = new File(jobDir, job.stringID() + "_" + job.getNumberOfImages());

            canYouTakeImage = true;


            if(resultCode == RESULT_OK){
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                job.setNumberOfImages(job.getNumberOfImages() + 1);
                db.updateJob(job);
                db.close();
                newChangesToJob = true;


                int index = job.getNumberOfImages() -1;
                addSmallImagePhoto(index);
            }
            if( resultCode == RESULT_CANCELED )
            {
                if( file.exists() )
                {
                    file.delete();
                }
                if(newJob){
                    deleteJob();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        changes();
    }

    private void changes(){
        if(newChangesToJob || !name.getText().toString().equals("") || !description.getText().toString().equals(job.getText())){

            job.setJobName(name.getText().toString());
            job.setText(description.getText().toString());
            job.setLastEdited(System.currentTimeMillis());
            DatabaseHandler db = new DatabaseHandler(this);
            db.updateJob(job);
            db.close();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("updated", job.getJobId());
            setResult(55555, returnIntent);
        }
    }
}
