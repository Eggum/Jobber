package com.example.jobber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Calendar;

class Job{
    private long jobId;
    private String jobName;
    private int numberOfImages;
    private int audio;
    private int gps;
    private String text;
    private int status;
    private int worker;
    private int creator;
    private int importance;
    private long lastEdited;
    private int numberOfImagesOnServer;

    private long versionFromServer;
    private int uploadedToServer;

    Job(long jobId, String jobName, int number_of_images, int audio, int gps, int status, int worker, int creator, int importance, long lastEdited, String text, int numberOfImagesOnServer, long versionFromServer, int uploadedToServer){
        this.jobId = jobId;
        this.jobName = jobName;
        this.numberOfImages = number_of_images;
        this.audio = audio;
        this.gps = gps;
        this.status = status;
        this.worker = worker;
        this.creator = creator;
        this.importance = importance;
        this.lastEdited = lastEdited;
        this.text = text;
        this.numberOfImagesOnServer = numberOfImagesOnServer;
        this.versionFromServer = versionFromServer;
        this.uploadedToServer = uploadedToServer;
    }

    Job(long jobId){
        this.jobId = jobId;
        jobName = "...";
        numberOfImages = 0;
        audio = -1;
        gps = -1;
        text = "...";
        status = 1;
        worker = -1;
        creator = -1;
        importance = 1;
        lastEdited = jobId;
        versionFromServer = -1;
        uploadedToServer = 0;
    }

    public long getJobId() {
        return jobId;
    }

    public int getNumberOfImagesOnServer(){
        return numberOfImagesOnServer;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        if(jobName != null && !jobName.equals("")) {
            this.jobName = jobName;
        }
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public int getAudio() {
        return audio;
    }

    public void setAudio(int audio) {
        this.audio = audio;
    }

    public int getGps() {
        return gps;
    }

    public void setGps(int gps) {
        this.gps = gps;
    }

    public String getText() {
        return text;
    }

    public long getVersionFromServer(){
        return versionFromServer;
    }

    public void setVersionFromServer(long versionFromServer){
        this.versionFromServer = versionFromServer;
    }

    public int getUploadedToServer(){
        return uploadedToServer;
    }

    public void setUploadedToServer(int uploadedToServer){
        this.uploadedToServer = uploadedToServer;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getWorker() {
        return worker;
    }

    public void setWorker(int worker) {
        this.worker = worker;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public String getCreationDateString(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(jobId);

        return c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
    }

    public long getLastEdited(){
        return lastEdited;
    }

    public void setLastEdited(long lastEdited){
        this.lastEdited = lastEdited;
    }

    public byte[] getBytes(){
        byte[] name = jobName.getBytes();
        byte[] jobText = text.getBytes();

        byte[] byteArray = ByteBuffer.allocate(8 + 7 + 8 + 2 + name.length + jobText.length)
                .putLong(jobId)
                .put((byte) name.length)
                .put(name)
                .put((byte) numberOfImages)
                .put((byte) audio)
                .put((byte) gps)
                .put((byte) status)
                .put((byte) worker)
                .put((byte) creator)
                .put((byte) importance)
                .putLong(lastEdited)
                .put((byte) jobText.length)
                .put(jobText)
                .array();

        return byteArray;
    }

    public byte[] getJobIdAsBytes(){

        return ByteBuffer.allocate(8)
                .putLong(jobId)
                .array();
    }

    public Bitmap getPhoto(File fileDir, int number){
        File file = new File(fileDir, jobId + "_" + number);
        Bitmap image_bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        return image_bmp;
    }

    public File getPhotoFile(File filedir, int number) {
        File file = new File(filedir, jobId + "_" + number);
        return file;
    }

    @Override
    public String toString()
    {
        return getCreationDateString() + ": " + jobName;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof Job){
            Job j = (Job) obj;
            if(jobId == j.getJobId()){
                return true;
            }
        }
        return false;
    }

    public String stringID()
    {
        return String.valueOf(jobId);
    }
}
