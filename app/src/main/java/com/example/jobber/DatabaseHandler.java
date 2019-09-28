package com.example.jobber;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 11;

    private static final String DATABASE_NAME = "jobs.db";
    private static final String TABLE_NAME = "jobbar";
    private static final String COLUMN_JOB_ID = "jobId";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_AUDIO = "audio";
    private static final String COLUMN_GPS = "gps";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_NAME = "namn";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_WORKER = "worker";
    private static final String COLUMN_CREATOR = "creator";
    private static final String COLUMN_IMPORTANCE = "importance";
    private static final String COLUMN_LAST_EDITED = "lastEdited";
    private static final String COLUMN_VERSION_FROM_SERVER = "versionFromServer";
    private static final String COLUMN_UPLOADED_TO_SERVER = "uploadedToServer";

    private static final String USER_TABLE_NAME = "user";
    private static final String USER_COLUMN_USER_ID = "userID";
    private static final String USER_COLUMN_USER_NAME = "username";
    private static final String USER_COLUMN_NUMBER_OF_FINISHED_JOBS = "numberOfFinishedJobs";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    TABLE_NAME + " (" +
                    COLUMN_JOB_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_IMAGE + " INTEGER," +
                    COLUMN_AUDIO + " INTEGER," +
                    COLUMN_GPS + " INTEGER," +
                    COLUMN_STATUS + " INTEGER," +
                    COLUMN_WORKER + " INTEGER," +
                    COLUMN_CREATOR + " INTEGER," +
                    COLUMN_IMPORTANCE + " INTEGER," +
                    COLUMN_LAST_EDITED + " INTEGER," +
                    COLUMN_TEXT + " TEXT," +
                    COLUMN_VERSION_FROM_SERVER + " INTEGER," +
                    COLUMN_UPLOADED_TO_SERVER + " INTEGER" +
                    ")";

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + USER_TABLE_NAME + " (" +
                    USER_COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                    USER_COLUMN_USER_NAME + " TEXT," +
                    USER_COLUMN_NUMBER_OF_FINISHED_JOBS + " INTEGER)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_DELETE_USER_TABLE =
            "DROP TABLE IF EXISTS " + USER_TABLE_NAME;

    DatabaseHandler( Context context )
    {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db )
    {
        db.execSQL( SQL_CREATE_ENTRIES );
        db.execSQL( SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
    {
        Log.d("f", "fff");
        db.execSQL(SQL_DELETE_TABLE);
        db.execSQL(SQL_DELETE_USER_TABLE);
        onCreate( db );
    }

    public long addJob( Job job )
    {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("FRA ADD JOB", " " + job.getNumberOfImages());

        ContentValues values = new ContentValues();
        values.put( COLUMN_JOB_ID, job.getJobId() );
        values.put( COLUMN_NAME, job.getJobName());
        values.put( COLUMN_IMAGE, job.getNumberOfImages() );
        values.put( COLUMN_AUDIO, job.getAudio() );
        values.put( COLUMN_GPS, job.getGps() );
        values.put(COLUMN_STATUS, job.getStatus());
        values.put( COLUMN_WORKER, job.getWorker());
        values.put( COLUMN_CREATOR, job.getCreator());
        values.put( COLUMN_IMPORTANCE, job.getImportance());
        values.put( COLUMN_LAST_EDITED, job.getLastEdited());
        values.put( COLUMN_TEXT, job.getText() );
        values.put( COLUMN_VERSION_FROM_SERVER, job.getVersionFromServer());
        values.put( COLUMN_UPLOADED_TO_SERVER, job.getUploadedToServer());



        long result = db.insert( TABLE_NAME, null, values );
        db.close();

        return result;
    }

    public Job getJob( long id )
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query( TABLE_NAME,
                new String[] { COLUMN_JOB_ID, COLUMN_NAME, COLUMN_IMAGE, COLUMN_AUDIO, COLUMN_GPS, COLUMN_STATUS, COLUMN_WORKER, COLUMN_CREATOR, COLUMN_IMPORTANCE, COLUMN_LAST_EDITED, COLUMN_TEXT, COLUMN_VERSION_FROM_SERVER, COLUMN_UPLOADED_TO_SERVER},
                COLUMN_JOB_ID + " = ?",
                new String[] { String.valueOf(id) }, null, null, null, null );

        Job job;
        if( cursor.moveToFirst() )
        {
            job = new Job(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getInt(9),
                    cursor.getString(10),
                    -1,
                    cursor.getLong(11),
                    cursor.getInt(12)
                    );
        }
        else
        {
            job = null;
        }

        cursor.close();
        db.close();
        return job;
    }

    public ArrayList<Job> getAllJobs()
    {
        ArrayList<Job> jobList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery( selectQuery, null );

        if( cursor.moveToFirst() )
        {
            do{
                Job job = new Job(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9),
                        cursor.getString(10),
                        -1,
                        cursor.getLong(11),
                        cursor.getInt(12)
                );
                jobList.add( job );
            } while( cursor.moveToNext() );
        }

        cursor.close();
        db.close();
        return jobList;
    }

    public int updateJob( Job job )
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put( COLUMN_NAME, job.getJobName());
        values.put( COLUMN_IMAGE, job.getNumberOfImages() );
        values.put( COLUMN_AUDIO, job.getAudio() );
        values.put( COLUMN_GPS, job.getGps() );
        values.put(COLUMN_STATUS, job.getStatus());
        values.put( COLUMN_WORKER, job.getWorker());
        values.put( COLUMN_CREATOR, job.getCreator());
        values.put( COLUMN_IMPORTANCE, job.getImportance());
        values.put( COLUMN_LAST_EDITED, job.getLastEdited());
        values.put( COLUMN_TEXT, job.getText() );
        values.put( COLUMN_VERSION_FROM_SERVER, job.getVersionFromServer());
        values.put( COLUMN_UPLOADED_TO_SERVER, job.getUploadedToServer());



        int result = db.update( TABLE_NAME, values, COLUMN_JOB_ID + " = ?",
                new String[] {String.valueOf(job.getJobId())} );

        db.close();
        return result;
    }

    public int deleteJob( Job job )
    {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete( TABLE_NAME, COLUMN_JOB_ID + " = ?",
                new String[] {String.valueOf(job.getJobId())} );

        db.close();
        return result;
    }
}