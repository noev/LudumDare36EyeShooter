package com.google.android.gms.samples.vision.face.googlyeyes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class GameView extends SurfaceView {
    private int rightCannonX = 0;
    private int leftCannonX = 0;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private int levelHeight = 0;
    private static final int TARGET_SIZE = 100;
    private static final int TARGET_Y = 10;
    private static final int TEXT_SIZE = 100;
    private Bitmap targetBitmap;
    private Bitmap bulletBitmap;
    private Bitmap gameOverBitmap;
    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;
    private int x = 0;
    private int bulletSpeed = 10;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private long lastClick;
    private Tracker<Face> faceTracker;
    private boolean targetAlive = true;
    private int bulletsLeft = 10;
    private Paint textPaint;
    private ArrayList<GameObject> targets = new ArrayList<>();
    private boolean gameOver = false;
    private AudioEffect audio;
    private int invaderSpeed = 5;
    private int xSpeed = invaderSpeed;
    private final int triangleHeight = 30;
    private final int triangleWidth = 30;
    TextView bulletsLeftTextView;

    public GameView(Context context) {
        super(context);
        initializeGameView();
    }


    public GameView(Context context, AttributeSet attrs) {
        super(context);
        initializeGameView();
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context);
        initializeGameView();
    }

    private void initializeGameView() {
        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.WHITE);

        audio = new AudioEffect(getContext());


        EventBus.getDefault().register(this);
        gameLoopThread = new GameLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }
        });

        targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.invader);
        targetBitmap = Bitmap.createScaledBitmap(targetBitmap, 100, 100, false);
        bulletBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
        gameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        drawTriangle(leftCannonX + bulletBitmap.getWidth() / 2, canvasHeight, triangleWidth, triangleHeight, false, textPaint, canvas);
        drawTriangle(rightCannonX + bulletBitmap.getWidth() / 2, canvasHeight, 30, 30, false, textPaint, canvas);
        if (gameOver) {
            canvas.drawBitmap(gameOverBitmap, (canvasWidth / 2) - gameOverBitmap.getWidth() / 2, (canvasHeight /2 ) - gameOverBitmap.getHeight() /2, null);
//            canvas.drawText("Game Over", canvasWidth / 2, canvasHeight / 2, textPaint);
        }

        //canvas.drawText("Bullets: " + Integer.toString(bulletsLeft), 50, 200, textPaint);
        if(bulletsLeftTextView != null){
            bulletsLeftTextView.setText(Integer.toString(bulletsLeft));
        }


        for (GameObject go : targets) {
            if (go.x == canvasWidth - targetBitmap.getWidth()) {
                xSpeed = invaderSpeed;
                xSpeed *= -1;
                for (GameObject t : targets) {
                    t.y += levelHeight;
                }
            }
            if (go.x == 0) {
                xSpeed = invaderSpeed;
                for (GameObject t : targets) {
                    t.y += levelHeight;
                }
            }
            go.x = go.x + xSpeed;

            canvas.drawBitmap(targetBitmap, go.x, go.y, null);

            if (spaceInvaderReachGround(go)) {
                gameOver = true;
            }
        }

        for (int z = bullets.size() - 1; z >= 0; z--) {
            //destroy bullets that are outside of the View
            if (bullets.get(z).y < 0) {
                bullets.remove(z);
                continue;
            }

            //bullet & space invader collision detection
            for (int i = targets.size() - 1; i >= 0; i--) {
                if (isCollision(bullets.get(z).x, bullets.get(z).y, bulletBitmap.getWidth() + bullets.get(z).x, bulletBitmap.getHeight() + bullets.get(z).y, targets.get(i).x, targets.get(i).y, targetBitmap.getWidth() + targets.get(i).x, targetBitmap.getHeight() + targets.get(i).y)) {
                    audio.playSound(R.raw.explosion);
                    targets.remove(i);
                    bullets.remove(z);
                    bulletsLeft += 1;
                    EventBus.getDefault().post(new BulletUpdateEvent(bulletsLeft));
                    if (targets.size() == 0) {
                        gameOver = true;
                    }
                    break;
                }
            }

            if (bullets.size() == 0) {
                break;
            }
            if (bullets.size() == z || bullets.get(z) == null) {
                continue;
            }

            bullets.get(z).y -= bulletSpeed;
            canvas.drawBitmap(bulletBitmap, bullets.get(z).x, bullets.get(z).y, null);
        }
    }

    private boolean spaceInvaderReachGround(GameObject go) {
        return go.y + targetBitmap.getHeight() >= canvasHeight;
    }

    private void drawTriangle(int x, int y, int width, int height, boolean inverted, Paint paint, Canvas canvas) {

        Point p1 = new Point(x, y);
        int pointX = x + width / 2;
        int pointY = inverted ? y + height : y - height;

        Point p2 = new Point(pointX, pointY);
        Point p3 = new Point(x + width, y);


        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();

        canvas.drawPath(path, paint);
    }


    class GameObject {
        int x;
        int y;

        GameObject(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void restart() {
        gameOver = false;
        initializeTargets();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasWidth = w;
        canvasHeight = h;
        levelHeight = canvasHeight / 10;
        initializeTargets();
    }

    public boolean isCollision(int item1x, int item1y, int item1width, int item1height, int item2x, int item2y, int item2width, int item2height) {
        Rect rItem1 = new Rect(item1x, item1y, item1width, item1height);
        Rect rItem2 = new Rect(item2x, item2y, item2width, item2height);
        return rItem1.intersect(rItem2);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (System.currentTimeMillis() - lastClick > 100) {
//            lastClick = System.currentTimeMillis();
//            synchronized (getHolder()) {
//                shoot();
//            }
//        }
//        return true;
//    }


    public void initializeTargets() {
        GameObject g1 = new GameObject(10, 0);
        GameObject g2 = new GameObject(210, 0);
        GameObject g3 = new GameObject(410, 0);

        GameObject g4 = new GameObject(110, levelHeight);
        GameObject g5 = new GameObject(310, levelHeight);

        GameObject g6 = new GameObject(210, levelHeight * 2);

        targets.add(g1);
        targets.add(g2);
        targets.add(g3);
        targets.add(g4);
        targets.add(g5);
        targets.add(g6);
    }

//    public void setFaceTracker(FaceTracker faceTracker) {
//        faceTracker.setFaceChangeListener(new FaceTracker.FaceChangeListener() {
//            @Override
//            public void leftEyeClosed() {
//                shoot("left", event.x);
//            }
//
//            @Override
//            public void rightEyeClosed() {
//                shoot("right", event.x);
//            }
//        });
//    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEyeUpdateEvent(EyeUpdateEvent event) {
        moveCannon(event);
        if (event.eyeClosed) {
            shoot(event.eye);
        }
    }

    private void moveCannon(EyeUpdateEvent event) {
        switch (event.eye) {
            case LEFT:
                leftCannonX = Math.round(event.x);
                break;
            case RIGHT:
                rightCannonX = Math.round(event.x);
                break;
            default:
                break;
        }
    }

    private void shoot(Eye eye) {
        if (bulletsLeft <= 0)
            return;
        audio.playSound(R.raw.laser_shoot);
        bulletsLeft--;
        EventBus.getDefault().post(new BulletUpdateEvent(bulletsLeft));
        switch (eye) {
            case LEFT:
                bullets.add(new Bullet(leftCannonX, canvasHeight));
                break;
            case RIGHT:
                bullets.add(new Bullet(rightCannonX, canvasHeight));
                break;
            default:
                break;
        }
    }


    class Bullet extends GameObject {
        Bullet(int x, int y) {
            super(x, y);
        }
    }

    public float dpToPx(int dp){
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
