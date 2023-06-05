package com.example.videoplayer;

import android.graphics.Bitmap;//---

public class VideoRVModal {
    private String videoName;
    private String videoPAth;
    private Bitmap thumbNail;

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoPAth() {
        return videoPAth;
    }

    public void setVideoPAth(String videoPAth) {
        this.videoPAth = videoPAth;
    }

    public Bitmap getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(Bitmap thumbNail) {
        this.thumbNail = thumbNail;
    }

    public VideoRVModal(String videoName, String videoPAth, Bitmap thumbNail) {
        this.videoName = videoName;
        this.videoPAth = videoPAth;
        this.thumbNail = thumbNail;
    }
}
