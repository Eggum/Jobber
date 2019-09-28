package com.example.jobber;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SessionHandler {

    public void Upload_Job(List<SocketCommunicator.SocketTask> tasks, Context context){

        byte[] outdata = tasks.get(0).getOutData();

        ByteBuffer buffer = ByteBuffer.wrap(outdata);

        Long jobId = buffer.order(ByteOrder.BIG_ENDIAN).getLong();

        DatabaseHandler db = new DatabaseHandler(context);
        Job job = db.getJob(jobId);
        job.setUploadedToServer(1);
        job.setVersionFromServer(job.getLastEdited());
        db.updateJob(job);
        db.close();
    }

    public void Archive_Job(List<SocketCommunicator.SocketTask> tasks, Context context){

        byte[] outdata = tasks.get(0).getOutData();

        ByteBuffer buffer = ByteBuffer.wrap(outdata);

        Long jobId = buffer.order(ByteOrder.BIG_ENDIAN).getLong();

        DatabaseHandler db = new DatabaseHandler(context);
        Job job = db.getJob(jobId);
        db.deleteJob(job);
        db.close();
    }

    public ArrayList<Job> Job_List(List<SocketCommunicator.SocketTask> tasks, ArrayList<Job> jobs, Context context){
        ArrayList<Job> jobsFromServer = new ArrayList<>();
        try {

            if (tasks.size() == 1) {
                byte[] dataRecieved = tasks.get(0).getInData();
                ByteBuffer buffer = ByteBuffer.wrap(dataRecieved);

                while (buffer.remaining() != 0) {

                    short sh = buffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
                    long jobId = buffer.order(ByteOrder.BIG_ENDIAN).getLong();
                    int res = buffer.get();
                    byte[] nameByte = new byte[res];
                    for (int i = 0; i < res; i++) {
                        nameByte[i] = buffer.get();
                    }
                    int numberOfImages = buffer.get();
                    int audio = buffer.get();
                    int gps = buffer.get();
                    int status = buffer.get();
                    int worker = buffer.get();
                    int creator = buffer.get();
                    int importance = buffer.get();
                    long lastEdited = buffer.order(ByteOrder.BIG_ENDIAN).getLong();
                    int textLength = buffer.get();

                    byte[] textByte = new byte[textLength];
                    buffer.get(textByte);
                    short numberOfImagesOnServer = buffer.order(ByteOrder.LITTLE_ENDIAN).getShort();


                    long versionFromServer = lastEdited;        // when downloaded from server these two will be the same.
                    int uploadedToServer = 1;       // if the job comes from the server, it will always have been uploaded.


                    String text = new String(textByte);
                    String jobName = new String(nameByte);


                    Log.d("job_list", jobId + " " + jobName + " " + text);

                    Job j = new Job(jobId, jobName, numberOfImages, audio, gps, status, worker, creator, importance, lastEdited, text, numberOfImagesOnServer, versionFromServer, uploadedToServer);

                    jobsFromServer.add(j);
                    /*
                    if (jobs.indexOf(j) == -1) {
                        DatabaseHandler db = new DatabaseHandler(context);
                        j.setNumberOfImages(0);
                        db.addJob(j);
                        db.close();

                        newJobs.add(j);
                    }
                    */
                }
            } else {
                Log.e("sessionHandleJob_List", "Error: fleire enn ein task i tasks lista");
            }
        } catch (Exception e){
            Log.e("Error in decoding files", " " + e);
        }


        // om eg ikkje har jobbene fra før av:
        ArrayList<Job> newJobs = new ArrayList<>();
        for(Job j: jobsFromServer){
            if (jobs.indexOf(j) == -1) {
                DatabaseHandler db = new DatabaseHandler(context);
                j.setNumberOfImages(0);
                db.addJob(j);
                db.close();

                newJobs.add(j);
            }
        }

        // checking for new updates
        for(Job j: jobsFromServer){
            int index = jobs.indexOf(j);
            if(index != -1){
                Job oldJob = jobs.get(index);
                if(oldJob.getLastEdited() < j.getLastEdited()){
                    // overskriver gamle jobben med den nye
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.updateJob(j);
                    db.close();
                }
            }
        }

        // delete jobs that aren't on the server anymore and has been uploaded
        for(Job j: jobs){
            if(jobsFromServer.indexOf(j) == -1){
                if(j.getUploadedToServer() == 1) {
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.deleteJob(j);
                    db.close();
                }
            }
        }


        return newJobs;
    }
}
