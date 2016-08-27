package com.google.android.gms.samples.vision.face.googlyeyes;

public class EyeClosedEvent {
    public final String message;
    public final float x;

    public EyeClosedEvent(String message, float x) {
        this.message = message;
        this.x = x;
    }
}
