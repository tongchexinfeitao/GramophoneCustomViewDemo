package com.example.gramophonecustomviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hasee on 2018/2/25.
 */

public class GramophoneView extends View {
    /**
     * 尺寸计算设计说明：
     * 1、唱片有两个主要尺寸：中间图片的半径、黑色圆环的宽度。
     * 黑色圆环的宽度 = 图片半径的一半。
     * 2、唱针分为“手臂”和“头”，手臂分两段，一段长的一段短的，头也是一段长的一段短的。
     * 唱针四个部分的尺寸求和 = 唱片中间图片的半径+黑色圆环的宽度
     * 唱针各部分长度 比例——长的手臂：短的手臂：长的头：短的头 = 8:4:2:1
     * 3、唱片黑色圆环顶部到唱针顶端的距离 = 唱针长的手臂的长。度
     */

    private final float DEFUALT_DISK_ROTATE_SPEED = 1f;
    private final float DEFAULT_PICTURE_RADIU = 200;     // 中间图片默认半径
    private final float DEFUALT_PAUSE_NEEDLE_DEGREE = -45;      // 暂停状态时唱针的旋转角度
    private final float DEFUALT_PLAYING_NEEDLE_DEGREE = -15;    // 播放状态时唱针的旋转角度

    private int pictureRadiu;            // 中间图片的半径

    //指针
    private int smallCircleRadiu = 20;     // 唱针顶部小圆半径
    private int bigCircleRadiu = 30;       // 唱针顶部大圆半径

    private int shortArmLength;
    private int longArmleLength;         // 唱针手臂，较长那段的长度
    private int shortHeadLength;         // 唱针的头，较短那段的长度
    private int longHeadLength;
    private Paint needlePaint;

    //唱片
    private float halfMeasureWidth;
    private int diskRingWidth;            // 黑色圆环宽度
    private float diskRotateSpeed;        // 唱片旋转速度
    private Bitmap pictureBitmap;
    private Paint diskPaint;

    //状态控制
    private boolean isPlaying;
    private float currentDiskDegree;            // 唱片旋转角度
    private float currentNeddleDegree = DEFUALT_PLAYING_NEEDLE_DEGREE;  // 唱针旋转角度

    public GramophoneView(Context context) {
        this(context, null);
    }

    public GramophoneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        diskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GramophoneView);

        //拿到xml中的图片和图片半径和，旋转的度数
        pictureRadiu = (int) typedArray.getDimension(R.styleable.GramophoneView_picture_radiu, DEFAULT_PICTURE_RADIU);
        diskRotateSpeed = typedArray.getFloat(R.styleable.GramophoneView_disk_rotate_speed, DEFUALT_DISK_ROTATE_SPEED);
        Drawable drawable = typedArray.getDrawable(R.styleable.GramophoneView_src);
        if (drawable == null) {
            pictureBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        } else {
            pictureBitmap = ((BitmapDrawable) drawable).getBitmap();
        }

        //初始化唱片的变量
        diskRingWidth = pictureRadiu >> 1;

        shortHeadLength = (pictureRadiu + diskRingWidth) / 15;    //图片半径和黑色圆环的和 等于 指针的总长度
        longHeadLength = shortHeadLength << 1;    //左移相当于乘以2
        shortArmLength = longHeadLength << 1;
        longArmleLength = shortArmLength << 1;


    }

    /**
     * 理想的宽高是，取决于picture的 半径的
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //我们想要的理想宽高
        int width = (pictureRadiu + diskRingWidth) * 2;
        int hight = (pictureRadiu + diskRingWidth) * 2 + longArmleLength;

        //根据我们理想的宽和高 和xml中设置的宽高，按resolveSize规则做最后的取舍
        //resolveSize规则 1、精确模式，按
        int measureWidth = resolveSize(width, widthMeasureSpec);
        int measureHeight = resolveSize(hight, heightMeasureSpec);

        setMeasuredDimension(measureWidth, measureHeight);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        halfMeasureWidth = getMeasuredWidth() >> 1;
        drawDisk(canvas);
        drawNeedle(canvas);
        if (currentNeddleDegree > DEFUALT_PAUSE_NEEDLE_DEGREE) {
            invalidate();
        }
    }

    private void drawDisk(Canvas canvas) {
        currentDiskDegree = currentDiskDegree % 360 + diskRotateSpeed;

        canvas.save();
        canvas.translate(halfMeasureWidth, longArmleLength + diskRingWidth + pictureRadiu);
        canvas.rotate(currentDiskDegree);
        diskPaint.setColor(Color.BLACK);
        diskPaint.setStyle(Paint.Style.STROKE);
        diskPaint.setStrokeWidth(pictureRadiu / 2);
        canvas.drawCircle(0, 0, pictureRadiu + diskRingWidth / 2, diskPaint);


        Path path = new Path();       // 裁剪的path路径 （为了裁剪成圆形图片，其实是将画布剪裁成了圆形）
        path.addCircle(0, 0, pictureRadiu, Path.Direction.CW);
        canvas.clipPath(path);

        Rect src = new Rect();                  //将要画bitmap的那个范围
        src.set(0, 0, pictureBitmap.getWidth(), pictureBitmap.getHeight());
        Rect dst = new Rect();
        dst.set(-pictureRadiu, -pictureRadiu, pictureRadiu, pictureRadiu);      //将要将bitmap画要坐标系的那个位置
        canvas.drawBitmap(pictureBitmap, src, dst, null);
        canvas.restore();

    }

    private void drawNeedle(Canvas canvas) {
        canvas.save();

        //移动坐标原点，画指针第一段
        canvas.translate(halfMeasureWidth, 0);
        canvas.rotate(currentNeddleDegree);
        needlePaint.setColor(Color.parseColor("#C0C0C0"));
        needlePaint.setStrokeWidth(20);
        canvas.drawLine(0, 0, 0, longArmleLength, needlePaint);

        //画指针第二段
        canvas.translate(0, longArmleLength);
        canvas.rotate(-30);
        canvas.drawLine(0, 0, 0, shortArmLength, needlePaint);


        //画指针第三段
        canvas.translate(0, shortArmLength);
        needlePaint.setStrokeWidth(30);
        canvas.drawLine(0, 0, 0, longHeadLength, needlePaint);

        //画指针的第四段
        canvas.translate(0, longHeadLength);
        needlePaint.setStrokeWidth(45);
        canvas.drawLine(0, 0, 0, shortHeadLength, needlePaint);
        canvas.restore();


        //画指针的支点
        canvas.save();
        canvas.translate(halfMeasureWidth, 0);
        needlePaint.setColor(Color.parseColor("#8A8A8A"));
        needlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, bigCircleRadiu, needlePaint);

        needlePaint.setColor(Color.parseColor("#C0C0C0"));
        canvas.drawCircle(0, 0, smallCircleRadiu, needlePaint);
        canvas.restore();

        //当前如果是播放的话，就移动到播放的位置 ，因为逆时针旋转度数是负的所以，-  + 需要注意
        if (isPlaying) {
            if (currentNeddleDegree < DEFUALT_PLAYING_NEEDLE_DEGREE) {  //不是暂停状态，就是播放状态，或者是切换中状态
                currentNeddleDegree += 3;  //切换中状态指针是要有动画效果的，所有要改变指针的度数
            }
        } else {
            if (currentNeddleDegree > DEFUALT_PAUSE_NEEDLE_DEGREE) {
                currentNeddleDegree -= 3;
            }
        }
    }

    public void pauseOrstart() {
        isPlaying = !isPlaying;
        invalidate();
    }


    /**
     * 设置图片半径
     *
     * @param pictureRadius 图片半径
     */
    public void setPictureRadius(int pictureRadius) {
        this.pictureRadiu = pictureRadius;
    }


    /**
     * 设置唱片旋转速度
     *
     * @param diskRotateSpeed 旋转速度
     */
    public void setDiskRotateSpeed(float diskRotateSpeed) {
        this.diskRotateSpeed = diskRotateSpeed;
    }

    /**
     * 设置图片资源id
     *
     * @param resId 图片资源id
     */
    public void setPictureRes(int resId) {
        pictureBitmap = BitmapFactory.decodeResource(getContext().getResources(), resId);
        invalidate();
    }
}
