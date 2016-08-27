package com.google.android.gms.samples.vision.face.googlyeyes;

public class EyeUpdateEvent {
    public final Eye eye;
    public final float x;
    public boolean eyeClosed;

    public EyeUpdateEvent(Eye eye, float x, boolean eyeClosed) {
        this.eye = eye;
        this.x = x;
        this.eyeClosed = eyeClosed;
    }
}
