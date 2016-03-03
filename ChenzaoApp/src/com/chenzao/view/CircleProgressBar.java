package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgressBar extends View {
    private int maxProgress = 100;//最大进度
    private float progress = 0;//当前进度
    private int progressStrokeWidth = 12;//线宽
    // 画圆所在的矩形区域
    RectF oval;
    Paint paint;

    public CircleProgressBar(Context context) {
        super(context);
        // TODO 自动生成的构造函数存根
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO 自动生成的构造函数存根
        oval = new RectF();
        paint = new Paint();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO 自动生成的构造函数存根
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int mWidth = this.getWidth();
        int mHeight = this.getHeight();

        /**
         * 画最外层的大圆环
         */
        int center = mWidth/2; //获取圆心的x坐标
        int radius = (int) (center - progressStrokeWidth/2-34); //圆环的半径
        paint.setColor(Color.WHITE);//(roundColor); //设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); //设置空心
        paint.setStrokeWidth(3); //设置圆环的宽度
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawCircle(center, center, radius, paint); //画出圆环

        /**
         * 画圆弧 ，画圆环的进度
         */

        //设置进度是实心还是空心
        paint.setStrokeWidth(progressStrokeWidth); //设置圆环的宽度
        paint.setColor(Color.WHITE);  //设置进度的颜色
        RectF oval = new RectF(center - radius, center - radius, center
                + radius, center + radius);  //用于定义的圆弧的形状和大小的界限

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, -90, 360 * progress / maxProgress, false, paint);  //根据进度画圆弧  绘制白色圆圈，即进度条背景
    
        //画进度条末端小圆图
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.chart_dot);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float angle = (float) (Math.PI * 2 * progress / maxProgress);
        float left = (float) (center + radius * Math.sin(angle)-width/2);
        float top = (float) (center - radius*Math.cos(angle)-height/2);
        canvas.drawBitmap(bitmap, left, top, paint);
        
        //画中间的圆盘
        int radio1 = mWidth*3/8;
        paint.setColor(getResources().getColor(R.color.color_chart)); //设置圆饼的颜色
        paint.setStyle(Paint.Style.FILL); //设置实心
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawCircle(center, center, radio1, paint); //画出圆饼
        
        //画中间的线
        int y = mHeight/2;
        int startX = mWidth/4;
        int endX = mWidth*3/4;
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        canvas.drawLine(startX, y, endX, y, paint);
    }

    public int getMaxProgress(){
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
    }

    public void setProgress(float progress){
        this.progress = progress;
        this.invalidate();
    }

    public void setProgressNotInUiThread(int progress){
        this.progress = progress;
        this.postInvalidate();
    }

}
