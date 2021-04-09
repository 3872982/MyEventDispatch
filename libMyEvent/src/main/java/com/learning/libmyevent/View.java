package com.learning.libmyevent;

import com.learning.libmyevent.Listener.OnClickListener;
import com.learning.libmyevent.Listener.OnTouchListener;

public class View {

    private int left;
    private int top;
    private int right;
    private int bottom;

    private OnTouchListener mOnTouchListener;
    private OnClickListener mOnClickListener;

    public View() {
    }

    public View(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void setOnTouchListener(OnTouchListener mOnTouchListener) {
        this.mOnTouchListener = mOnTouchListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    /**
     * 判断坐标（x,y）是否在View中
     * @param x
     * @param y
     * @return
     */
    public boolean isContains(int x,int y){
        if(x >= left && x < right && y>=top && y < bottom){
            return true;
        }
        return false;
    }

    /**
     * 分发事件 由于View没有onInterceptEvent事件，所以直接先执行onTouchListener，再执行onTouchEvent即可
     * @param event
     * @return
     */
    public boolean dispatchTouchEvent(MotionEvent event){
        boolean result = false;

        //onTouchListener优先级高于onTouchEvent
        if(mOnTouchListener != null && mOnTouchListener.onTouch(this,event)){
            result = true;
        }

        if(!result && onTouchEvent(event)){
            result = true;
        }

        return result;
    }

    /**
     * 处理触摸事件  优先处理点击事件，点击事件会消费事件
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event){
        if(mOnClickListener!=null){
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

}
