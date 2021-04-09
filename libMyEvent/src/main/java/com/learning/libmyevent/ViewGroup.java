package com.learning.libmyevent;

import java.util.ArrayList;
import java.util.List;

public class ViewGroup extends View{
    List<View> childList = new ArrayList<>();
    private View[] mChildren=new View[0];
    private TouchTarget mFirstTouchTarget;


    public ViewGroup(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);
    }

    /**
     * 添加子视图，只会添加下一个层级的视图，下下一个层级的视图并不会被添加到这里面，满足迭代的要求
     * @param view
     */
    public void addView(View view) {
        if (view == null) {
            return;
        }
        childList.add(view);
        //数组访问更快更便捷
        mChildren = (View[]) childList.toArray(new View[childList.size()]);
    }

    /**
     * 分发事件伪代码：先问自己要不要拦截事件，不要拦截就发送给可以接受点击事件的子视图问他要不要拦截事件，
     * 一直重复这个过程知道事件被消费位置
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = false; //记录是否被消费
        boolean intercepted = onInterceptTouchEvent(event);
        TouchTarget newTouchTarget = null;

        int action_masked = event.getActionMasked();//事件类型

        if(action_masked != MotionEvent.ACTION_CANCEL && !intercepted){
            //处理action_down事件
            if(action_masked == MotionEvent.ACTION_DOWN){

                //这里用倒序，因为需要最上面的视图先得到响应
                for(int i=mChildren.length-1;i>=0;i--){
                    View child = mChildren[i];

                    //被点击的坐标不在view中，则继续检查下一个view
                    if(!child.isContains(event.getX(),event.getY())){
                        continue;
                    }

                    //能够响应事件的view，则分发给他
                    if(dispatchTransformedTouchEvent(event,child)){
                        handled = true;
                        newTouchTarget = addTouchTarget(child);
                        break;
                    }
                }
            }//==================end of if action_down================

            //走到这，说明没有子视图消费了事件，只剩下ViewGroup了
            if(mFirstTouchTarget == null){
                dispatchTransformedTouchEvent(event,null);
            }
        }


        return handled;
    }

    private boolean dispatchTransformedTouchEvent(MotionEvent event, View child) {
        boolean handled = false;
        if(child != null){
            handled = child.dispatchTouchEvent(event);
        }else{//处理ViewGroup
            handled = super.dispatchTouchEvent(event);
        }

        return handled;
    }

    /**
     * 是否拦截事件 默认不拦截
     * @param event
     * @return
     */
    public boolean onInterceptTouchEvent(MotionEvent event){
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /***
     * 添加一个target到mFirstTouchTarget链表头
     * @param child
     * @return mFirstTouchTarget
     */
    private TouchTarget addTouchTarget(View child) {
        final TouchTarget target = TouchTarget.obtain(child);
        target.next = mFirstTouchTarget;
        mFirstTouchTarget = target;
        return target;
    }


    /**
     * 回收池策略   -- 单链表实现，sRecycleBin指向链表头部 ，插入删除都从头部去进行操作
     * 这个学过数据结构的会更容易理解一点
     */
    public static final class TouchTarget{
        public View child;
        //回收池的链表头部
        private static TouchTarget sRecycleBin;
        //回收池的长度
        private static int sRecycleBinCount;
        private static final Object sRecycleLock = new Object[0];
        private TouchTarget next;

        /**
         * 如果回收池中有，则取出回收池头部，并将回收池链表后移一格
         * 否则创建新的并返回
         */
        public static TouchTarget obtain(View view){
            TouchTarget touchTarget = null;

            synchronized (sRecycleLock){
                if(sRecycleBin == null){
                    touchTarget = new TouchTarget();
                }else{
                    touchTarget = sRecycleBin;
                }
                sRecycleBin = touchTarget.next;
                sRecycleBinCount--;
                touchTarget.child = view;;
                touchTarget.next = null;
            }

            return touchTarget;
        }

        /**
         * 回收，就是讲当前的TouchTarget添加到回收池头部即可
         */
        public void recycle(){
            if(null == child){
                throw new IllegalStateException("已经被回收过了");
            }

            synchronized (sRecycleLock) {
                //android系统对于嵌套超过32层的视图将不再触发消息
                if(sRecycleBinCount < 32) {
                    this.next = sRecycleBin;
                    this.child = null;
                    sRecycleBin = this;
                    sRecycleBinCount++;
                }
            }
        }
    }//----end of class TouchTarget----


    //name 用来辅助辨别所在视图的
    private String name;

    @Override
    public String toString() {
        return ""+name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
