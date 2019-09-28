package com.example.jobber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter{

    private Context context;
    private ArrayList jobs;
    private File filedir;

    ImageAdapter(Context c, File filedir, ArrayList<Job> jobs)
    {
        context = c;
        this.jobs = jobs;
        this.filedir = filedir;
    }

    @Override
    public int getCount() {
        return jobs.size();
    }

    @Override
    public Object getItem(int position) {
        Job j = (Job) jobs.get(position);
        return j;
    }

    @Override
    public long getItemId(int position) {

        Job j = (Job) jobs.get(position);
        return j.getJobId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if( convertView == null )
        {
            imageView = new ImageView(context);

            //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            //imageView.setAdjustViewBounds(true);

            //imageView.setLayoutParams(new GridView.LayoutParams(GridLayout.LayoutParams.WRAP_CONTENT, GridLayout.LayoutParams.WRAP_CONTENT));
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        Job j = (Job) jobs.get(position);

        Glide.with(context).load(j.getPhotoFile(filedir, 0)).placeholder(R.drawable.missing_image).into(imageView);

        //imageView.setImageBitmap(j.getPhoto(filedir, 0));


        return imageView;
    }
}