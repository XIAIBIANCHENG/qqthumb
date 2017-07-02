package com.example.thumbsup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private FrameLayout                 pearentFrame;
    private List<AnimaImageView>        moveImages=new ArrayList<AnimaImageView>();
    private ImageView                   thumbBtn;
    private int[]                       randomColors={0xFFCAAAFF,0xFFFFF6A6,0xFFFF66AA,0xFF1A6CFF,0xFF7CFF52};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thumbBtn=(ImageView) findViewById(R.id.thumb);
        thumbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] pos=new int[2];

                thumbBtn.getLocationInWindow(pos);
                Bitmap bitmap=generateRandomColorBitmap();
                ImageView img=new ImageView(MainActivity.this);
                pearentFrame=(FrameLayout) findViewById(R.id.activity_main);
                img.setImageBitmap(bitmap);
                pearentFrame.addView(img,40,40);
                AnimaImageView animaImageView=new AnimaImageView(img,pos,pearentFrame);
                moveImages.add(animaImageView);
                animaImageView.startAnimator();

            }
        });
    }

    //生成不同颜色的点赞图片
    private Bitmap generateRandomColorBitmap(){
        int w,h;
        w=h=dip2px(this,20);
        Bitmap dst= Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        Bitmap src=BitmapFactory.decodeResource(getResources(),R.mipmap.pql).copy(Bitmap.Config.ARGB_8888, true);;

        Canvas dstCanvas=new Canvas(dst);
        Canvas srcCanvas=new Canvas(src);

        Paint rectPaint=new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(randomColors[getRandom(0,5)]);
        dstCanvas.drawRect(0,0,w,h,rectPaint);

        Paint p=new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        srcCanvas.drawBitmap(dst,0,0,p);
        dst.recycle();
        return src;
    }

    public  int getRandom(int min, int max){
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }

    public  int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    //生成显示的图片
    public class AnimaImageView{
        private ViewGroup       parentView;
        private ImageView       img;
        private int[]           startPos;
        private AnimatorSet     set=new AnimatorSet();
        private PointF          p0;
        private PointF          p1;
        private PointF          p2;
        private PointF          p3;
        public AnimaImageView(ImageView imageView,int[] pos,ViewGroup parent){
            parentView=parent;
            img=imageView;
            img.setTranslationX(pos[0]);
            img.setTranslationY(pos[1]-20);
            img.setScaleType(ImageView.ScaleType.CENTER);

            //计算随机三次贝塞尔曲线
            int t=getRandom(0,10);
            int offset=getRandom(20,100);
            if(t<5)
                offset=-offset;
            startPos=pos;
            p3=new PointF(startPos[0],startPos[1]-400);
            p1=new PointF(startPos[0]-offset,startPos[1]-200);
            p2=new PointF(startPos[0]+offset,startPos[1]-200);
            p0=new PointF(startPos[0],startPos[1]-20);
        }

        public void setScale(float v){
            img.setScaleY(v);
            img.setScaleX(v);
        }

        public void setPosition(float v){

            //计算位置的变化
            PointF p=CalculateBezierPointForCubic(v,p0,p1,p2,p3);
            img.setX(p.x);
            img.setY(p.y);
        }

        public void setAlpha(float v){
            img.setAlpha(v);
        }

        public void startAnimator(){
            set.playTogether(ObjectAnimator.ofFloat(this,"scale",0.4f,1.3f,1.0f));
            set.playTogether(ObjectAnimator.ofFloat(this,"position",0.0f,1.0f));
            set.playTogether(ObjectAnimator.ofFloat(this,"alpha",0.0f,1.0f,0.0f));
            set.setInterpolator(new LinearInterpolator());
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    parentView.removeView(img);
                    moveImages.remove(AnimaImageView.this);
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            set.setDuration(2000);
            set.start();
        }

        /**
         * 三次贝塞尔曲线计算公式
         * B(t) = P0 * (1-t)^3 + 3 * P1 * t * (1-t)^2 + 3 * P2 * t^2 * (1-t) + P3 * t^3, t ∈ [0,1]
         * @param t  曲线长度比例
         * @param p0 起始点
         * @param p1 控制点1
         * @param p2 控制点2
         * @param p3 终止点
         * @return t对应的点
         */
        private  PointF CalculateBezierPointForCubic(float t, PointF p0, PointF p1, PointF p2, PointF p3) {
            PointF point = new PointF();
            float temp = 1 - t;
            point.x = p0.x * temp * temp * temp + 3 * p1.x * t * temp * temp + 3 * p2.x * t * t * temp + p3.x * t * t * t;
            point.y = p0.y * temp * temp * temp + 3 * p1.y * t * temp * temp + 3 * p2.y * t * t * temp + p3.y * t * t * t;
            return point;
        }
    }
}
