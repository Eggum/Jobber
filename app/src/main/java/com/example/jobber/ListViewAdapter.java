package com.example.jobber;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList jobs;
    private File filedir;

    ListViewAdapter(Context c, File filedir, ArrayList<Job> jobs)
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

        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
//            convertView = inflater.inflate(R.layout.single_list_item, parent, false);
            convertView = inflater.inflate(R.layout.testing, parent, false);

            viewHolder.listJobName = (TextView) convertView.findViewById(R.id.listJobName);
            viewHolder.listImportance = (TextView) convertView.findViewById(R.id.listImportance);
            viewHolder.listCreationDate = convertView.findViewById(R.id.listCreationDate);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.jobImage);
            viewHolder.checkMark = convertView.findViewById(R.id.checkmark);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Job j = (Job) jobs.get(position);


        viewHolder.listJobName.setText(j.getJobName());
        if(j.getUploadedToServer() == 0){
            viewHolder.listJobName.setTextColor(Color.RED);
        }
        int imprtanceValue = j.getImportance();
        if(imprtanceValue == 1){

            viewHolder.listImportance.setText("Uviktig");
            viewHolder.listImportance.setTextColor(Color.BLACK);

        } else if(imprtanceValue == 2){
            viewHolder.listImportance.setText("Viktig");
            viewHolder.listImportance.setTextColor(0xFFFAA500);

        } else if(imprtanceValue == 3){
            viewHolder.listImportance.setText("Haster");
            viewHolder.listImportance.setTextColor(Color.RED);
        }


        viewHolder.listCreationDate.setText(j.getCreationDateString());


        Glide.with(context).load(j.getPhotoFile(filedir, 0)).placeholder(R.drawable.missing_image).into(viewHolder.icon);

        if(j.getStatus() == 2){
            Glide.with(context).load(R.drawable.green_checkmark_).into(viewHolder.checkMark);
        }
        return convertView;
    }

    private static class ViewHolder {

        TextView listJobName;
        TextView listImportance;
        TextView listCreationDate;
        ImageView icon;
        ImageView checkMark;
    }
}
