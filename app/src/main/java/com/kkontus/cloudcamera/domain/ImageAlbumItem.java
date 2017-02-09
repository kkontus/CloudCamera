package com.kkontus.cloudcamera.domain;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageAlbumItem implements Parcelable {

    public static final Creator<ImageAlbumItem> CREATOR = new Creator<ImageAlbumItem>() {
        public ImageAlbumItem createFromParcel(Parcel in) {
            return new ImageAlbumItem(in);
        }

        public ImageAlbumItem[] newArray(int size) {
            return new ImageAlbumItem[size];
        }
    };
    private Uri imageUri;
    private Bitmap image;
    private String imageName;
    private boolean isSelected;
    private boolean isMoved;
    private String albumName;
    private String serviceName;

    public ImageAlbumItem(Uri imageUri, Bitmap image, String imageName,
                          boolean isSelected, boolean isMoved, String albumName, String serviceName) {
        super();
        this.imageUri = imageUri;
        this.image = image;
        this.imageName = imageName;
        this.isSelected = isSelected;
        this.isMoved = isMoved;
        this.albumName = albumName;
        this.serviceName = serviceName;
    }

    private ImageAlbumItem(Parcel in) {
        image = in.readParcelable(ImageAlbumItem.class.getClassLoader());
        imageName = in.readString();
        isSelected = (in.readInt() == 0) ? false : true;
        isMoved = (in.readInt() == 0) ? false : true;
        imageUri = Uri.parse(in.readString());
        albumName = in.readString();
        serviceName = in.readString();
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean getIsMoved() {
        return isMoved;
    }

    public void setIsMoved(boolean isMoved) {
        this.isMoved = isMoved;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ImageAlbumItem cloneWithoutBitmap() {
        ImageAlbumItem imageAlbumItem = new ImageAlbumItem(imageUri, image, imageName, isSelected, isMoved, albumName, serviceName);

        imageAlbumItem.imageUri = imageUri;
        imageAlbumItem.image = null;
        imageAlbumItem.imageName = imageName;
        imageAlbumItem.isSelected = isSelected;
        imageAlbumItem.isMoved = isMoved;
        imageAlbumItem.albumName = albumName;
        imageAlbumItem.serviceName = serviceName;

        return imageAlbumItem;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeParcelable(image, flags);
        dest.writeString(imageName);
        dest.writeInt(isSelected ? 1 : 0);
        dest.writeInt(isMoved ? 1 : 0);
        dest.writeString(imageUri.toString());
        dest.writeString(albumName);
        dest.writeString(serviceName);
    }

}
