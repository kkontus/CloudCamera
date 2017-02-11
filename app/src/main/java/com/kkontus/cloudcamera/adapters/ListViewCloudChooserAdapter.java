package com.kkontus.cloudcamera.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkontus.cloudcamera.R;
import com.kkontus.cloudcamera.helpers.AppSettings;

public class ListViewCloudChooserAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResource;
    private String[] values;

    public ListViewCloudChooserAdapter(Context context, int resource, String[] values) {
        super(context, resource, values);
        this.mContext = context;
        this.mResource = resource;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);

            viewHolder = new ViewHolder();

			viewHolder.imageName = (TextView) convertView.findViewById(R.id.textviewCloud);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.imageviewCloud);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageName.setText(values[position]);


        // Change icon based on name
        String s = values[position];

        if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_LOCAL_FOLDER)) {
            viewHolder.image.setImageResource(R.mipmap.localfolder);
        } else if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_BOX)) {
            viewHolder.image.setImageResource(R.mipmap.box);
        } else if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_DROPBOX)) {
            viewHolder.image.setImageResource(R.mipmap.dropbox);
        } else if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_GOOGLE_DRIVE)) {
            viewHolder.image.setImageResource(R.mipmap.googledrive);
        } else if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_ONE_DRIVE)) {
            viewHolder.image.setImageResource(R.mipmap.onedrive);
        } else if (s.equalsIgnoreCase(AppSettings.SENDER_SERVICE_FACEBOOK)) {
            viewHolder.image.setImageResource(R.mipmap.facebook);
        } else {
            viewHolder.image.setImageResource(R.mipmap.android_logo);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView image;
        TextView imageName;
    }

}
