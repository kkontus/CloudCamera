package com.kkontus.cloudcamera.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;

import java.util.List;

public class ListViewImageAlbumAdapter extends ArrayAdapter<ImageAlbumItem> {

    private Context mContext;
    private int mResource;
    private List<ImageAlbumItem> items = null;

    public ListViewImageAlbumAdapter(Context context, int resource, List<ImageAlbumItem> items) {
        super(context, resource, items);
        this.mContext = context;
        this.mResource = resource;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);

            viewHolder = new ViewHolder();

			/*
            viewHolder.imageName = (TextView) convertView.findViewById(R.id.textviewImageName);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.imageviewImageTaken);
			viewHolder.imageToUpload = (CheckBox) convertView.findViewById(R.id.checkboxItemToUpload);
			*/

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.imageToUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                items.get(position).setIsSelected(isChecked);
                notifyDataSetChanged();
            }
        });

        viewHolder.imageName.setText(items.get(position).getImageName());
        viewHolder.image.setImageBitmap(items.get(position).getImage());
        viewHolder.imageToUpload.setChecked(items.get(position).getIsSelected());

        return convertView;
    }

    static class ViewHolder {
        //ImageView imageUri;
        ImageView image;
        TextView imageName;
        CheckBox imageToUpload;
    }

}
