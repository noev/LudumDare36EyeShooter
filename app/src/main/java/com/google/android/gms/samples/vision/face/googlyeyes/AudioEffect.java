package com.google.android.gms.samples.vision.face.googlyeyes;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class AudioEffect {
    private SoundPool sp;
    private Map<Integer, Integer> sounds;

    AudioEffect(Context context){
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        sp = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attrs)
                .build();

        sounds = new HashMap<>(2);
        sounds.put(R.raw.explosion, sp.load(context, R.raw.explosion, 1));
        sounds.put(R.raw.laser_shoot, sp.load(context, R.raw.laser_shoot, 1));
    }

    public void playSound(Integer sound){
        sp.play(sounds.get(sound), 1, 1, 1, 0, 1.0f);
    }
}
