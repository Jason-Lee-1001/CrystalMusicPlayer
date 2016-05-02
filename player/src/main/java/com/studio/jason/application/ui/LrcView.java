package com.studio.jason.application.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LrcView extends TextView {
    private float width;        //歌词视图宽度
    private float height;        //歌词视图高度
    private Paint currentPaint;    //当前画笔对象
    private Paint notCurrentPaint;    //非当前画笔对象
    private float textHeight = 60;    //文本高度
    private float textSize = 40;        //文本大小
    private int index = 0;        //list集合下标


    private List<LrcContent> mLrcList = new ArrayList<LrcContent>();

    public LrcView(Context context) {
        super(context);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setmLrcList(List<LrcContent> mLrcList) {
        this.mLrcList = mLrcList;
    }

    private void init() {
        setFocusable(true);        //设置可对焦

        //高亮部分
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setTextAlign(Paint.Align.CENTER);

        //非高亮部分
        notCurrentPaint = new Paint();
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }

        currentPaint.setColor(Color.argb(230, 255, 255, 255));
        notCurrentPaint.setColor(Color.argb(140, 255, 255, 255));

        currentPaint.setTextSize(45);
        currentPaint.setTypeface(Typeface.DEFAULT);

        notCurrentPaint.setTextSize(textSize);
        notCurrentPaint.setTypeface(Typeface.DEFAULT);

        try {
            setText("");
            canvas.drawText(mLrcList.get(index).getLrcStr(), width / 2, height / 2, currentPaint);

            float tempY = height / 2;
            //画出本句之前的句子
            for (int i = index - 1; i >= 0; i--) {
                //向上推移
                tempY = tempY - textHeight;
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
            }
            tempY = height / 2;
            //画出本句之后的句子
            for (int i = index + 1; i < mLrcList.size(); i++) {
                //往下推移
                tempY = tempY + textHeight;
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
            }
        } catch (Exception e) {
            setText("");
        }
    }

    /**
     * 当view大小改变的时候调用的方法
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}

