package com.google.android.gms.samples.vision.face.googlyeyes;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class FaceTracker extends Tracker<Face> {
    private static final float EYE_CLOSED_THRESHOLD = 0.4f;

    private FaceChangeListener faceChangeListener;

    private boolean mPreviousIsLeftOpen = true;
    private boolean mPreviousIsRightOpen = true;

    public interface FaceChangeListener{
        void leftEyeClosed();
        void rightEyeClosed();
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        float leftOpenScore = face.getIsLeftEyeOpenProbability();
        boolean isLeftOpen;
        if(leftOpenScore == Face.UNCOMPUTED_PROBABILITY){
            isLeftOpen = mPreviousIsLeftOpen;
        }else{
            isLeftOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
            if(mPreviousIsLeftOpen&& !isLeftOpen){
                faceChangeListener.leftEyeClosed();
            }
            mPreviousIsLeftOpen = isLeftOpen;
        }

        float rightOpenScore = face.getIsRightEyeOpenProbability();
        boolean isRightOpen;
        if(rightOpenScore == Face.UNCOMPUTED_PROBABILITY){
            isRightOpen = mPreviousIsRightOpen;
        }else{
            isRightOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
            if(mPreviousIsRightOpen && !isRightOpen){
                faceChangeListener.rightEyeClosed();
            }
            mPreviousIsRightOpen = isRightOpen;
        }
    }

    public void setFaceChangeListener(FaceChangeListener listener){
        this.faceChangeListener = listener;
    }
}
