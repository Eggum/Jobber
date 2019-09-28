package com.example.jobber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements socketCallback{
    ViewFlipperWithGesture viewSwitcher;
    ListView listView;
    private ArrayList<Job> jobs;
    static int REQUEST_CODE = 1234;
    static final int VIEW_JOB = 1;
    GridView gridview;
    ImageAdapter imageAdapter;
    ListViewAdapter listViewAdapter;
    SocketCommunicator socketCommunicator;
    String serverIP = "00.000.0.000";   // add server IP here
    int portNr = 11111;                 // and port number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewSwitcher = findViewById(R.id.viewSwitcher);
        listView = findViewById(R.id.listView);
        gridview = findViewById(R.id.gridView);

        jobs = new ArrayList<>();

        socketCommunicator = new SocketCommunicator(this, serverIP, portNr);

        refreshJobs();

        downloadJobs();

        createFolder();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Job job = (Job) imageAdapter.getItem(position);
                viewJob(job);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Job job = (Job) listViewAdapter.getItem(position);
                viewJob(job);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission
        }
    }

    public void downloadJobs(){
        socketCommunicator.new_session();
        socketCommunicator.add_task(serverCommands.JOB_LIST, new byte[0],null);
        socketCommunicator.add_session();
    }

    public void uploadJobs(){
        downloadJobs();
        refreshJobs();
        for(Job j : jobs) {
            if(j.getLastEdited() != j.getVersionFromServer() || j.getUploadedToServer() == 0) {
                uploadJob(j);
            }
        }
    }

    public void archiveJob(long jobID){
        socketCommunicator.new_session();
        socketCommunicator.add_task(serverCommands.ARCHIVE_JOB, ByteBuffer.allocate(8).putLong(jobID).array(), null);
        socketCommunicator.add_session();
    }

    public void uploadJob(Job j){
        socketCommunicator.new_session();
        socketCommunicator.add_task(serverCommands.UPLOAD_JOB, j.getBytes(), null);
        File jobDir = new File(getExternalFilesDir(null), "jobs/");
        for (int i = 0; i < j.getNumberOfImages(); i++){
            File file = j.getPhotoFile(jobDir, i);
            if(file.length() != 0) {
                socketCommunicator.add_task(serverCommands.UPLOAD_FILE, j.getJobIdAsBytes(), file);
            }
        }
        socketCommunicator.add_session();
    }

    public void refreshJobs(){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        jobs = db.getAllJobs();
        db.close();

        File jobDir = new File(getExternalFilesDir(null), "jobs/");


        listViewAdapter = new ListViewAdapter(this, jobDir, jobs);
        listView.setAdapter(listViewAdapter);
        listView.deferNotifyDataSetChanged();


        imageAdapter = new ImageAdapter(this, jobDir, jobs);
        gridview.setAdapter(imageAdapter);
    }

    public void createFolder(){

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //File write logic here


            //String path = Environment.getExternalStorageDirectory() + File.separator + "jobs";
            String path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.example.jobber/files/jobs";
            //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/jobs/";
            // Create the parent path
            File dir = new File(path);
            boolean res = false;
            if (!dir.exists()) {
                res = dir.mkdirs();
            }
            Log.d("MainActivity", ">> Let's debug why this directory isn't being created: ");
            Log.d("MainActivity", "Is it working?: " + dir.mkdirs());
            Log.d("MainActivity", "Does it exist?: " + dir.exists());
            Log.d("MainActivity", "What is the full URI?: " + dir.toURI());
            Log.d("MainActivity", "--");
            Log.d("MainActivity", "Can we write to this file?: " + dir.canWrite());
            if (!dir.canWrite()) {
                Log.d("MainActivity", ">> We can't write! Do we have WRITE_EXTERNAL_STORAGE permission?");
                if (getBaseContext().checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                    Log.d("MainActivity", ">> We don't have permission to write - please add it.");
                } else {
                    Log.d("MainActivity", "We do have permission - the problem lies elsewhere.");
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        }
    }

    public void viewJob(Job job){
        Intent intent = new Intent(this, ViewJob.class);
        intent.putExtra("job", job.getJobId());
        startActivityForResult(intent,1);
    }

    public void viewNewJob(Job job){
        Intent intent = new Intent(this, ViewJob.class);
        intent.putExtra("job", job.getJobId());
        intent.putExtra("new", true);
        startActivityForResult(intent,1);
    }

    public void downloadJobs(View view) {
        downloadJobs();
    }

    public void uploadJobs(View view){
        uploadJobs();
    }

    public void newJob(View view){

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        Job job = new Job(System.currentTimeMillis());
        jobs.add(job);
        db.addJob(job);

        db.close();

        viewNewJob(job);
    }

    public void onActivityResult( int requestCode, int resultCode, Intent data ){

        if (requestCode == VIEW_JOB){
            if(resultCode == 456677){
                long jobID = data.getLongExtra("jobId", 0);
                archiveJob(jobID);
            }

            /*
            if(resultCode == 55555){
                long jobID = data.getLongExtra("updated", 0);
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                Job job = db.getJob(jobID);
                db.close();
                downloadJobs();
                uploadJob(job);
            }
            */
            refreshJobs();
            uploadJobs();
        }
    }

    @Override
    public void sessionFinished(int i) {
        SocketCommunicator.SocketSession finishedSession = socketCommunicator.sessions.get(i);
        SessionHandler sessionHandler = new SessionHandler();

        if(!finishedSession.getError()){
            List<SocketCommunicator.SocketTask> tasks = finishedSession.getTasks();

            switch (tasks.get(0).getCmd()) {
                case JOB_LIST:
                    ArrayList <Job> newJobs = sessionHandler.Job_List(tasks, jobs, this);

                    int counter = downloadFiles(newJobs);

                    // Update GUI if no new jobs needed to be downloaded. Some jobs might have been removed.
                    if (counter == 0) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                refreshJobs();
                            }
                        });
                    }

                    break;
                case UPLOAD_JOB:
                    sessionHandler.Upload_Job(tasks, this);
                    break;
                case UPLOAD_FILE:
                    break;
                case DELETE_JOB:
                    break;
                case DOWNLOAD_JOB:

                    break;
                case ARCHIVE_JOB:
                    sessionHandler.Archive_Job(tasks, this);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            refreshJobs();
                        }
                    });
                    break;
                case DOWNLOAD_FILE:

                    byte[] outdata = tasks.get(0).getOutData();

                    ByteBuffer buffer = ByteBuffer.wrap(outdata);

                    Long jobId = buffer.order(ByteOrder.BIG_ENDIAN).getLong();

                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    Job job = db.getJob(jobId);
                    job.setNumberOfImages(job.getNumberOfImages() + 1);
                    db.updateJob(job);
                    db.close();


                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            refreshJobs();
                        }
                    });

                    break;
                default:
                    break;
            }
        } else {
            Log.e("sessionFinished", "ERROR" + finishedSession.getTasks().get(0).toString());
        }
    }

    @Override
    public void threadFinished() {

    }

    @Override
    public void onException(Exception e) {
        Log.e("EXCEPTION", "server: "+ e.getStackTrace());
    }

    public int downloadFiles(ArrayList<Job> newJobs){
        int counter = 0;
        for(Job j: newJobs){

            for(int i = 0; i < j.getNumberOfImagesOnServer(); i++) {
                File img = createImageFile(j, i);
                if (img != null) {

                    byte[] jobIdAndPhotoNumber = ByteBuffer.allocate(8 + 2)
                            .putLong(j.getJobId())
                            .order(ByteOrder.LITTLE_ENDIAN).putShort((short) i)
                            .array();

                    counter++;
                    socketCommunicator.new_session();
                    socketCommunicator.add_task(serverCommands.DOWNLOAD_FILE, jobIdAndPhotoNumber, img);
                    socketCommunicator.add_session();
                }
            }
        }
        return counter;
    }

    private File createImageFile(Job job, int fileNumber) {
        File jobDir = new File(getExternalFilesDir(null), "jobs/");
        Log.d("ViewJob", "few" + jobDir.getAbsolutePath());
        File file = new File(jobDir, job.stringID() + "_" + (fileNumber));


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
}
