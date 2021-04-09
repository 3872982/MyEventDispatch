###事件分发实现

####事件分发原理
Activity中调用onTouchEvent之前会先调用getWindow().dispathTouchEvent,而`getWindow() => phoneWindow => decorView`
所以直观的将Activity在调用onTouchEvent之前会调用根视图**DecorView的分发事件**。

#####DecorView的分发事件
DecorView先问自己要不要拦截，如果不要，就问问自己的子视图要不要拦截，依次类推，只要事件被消费，或者没有更多的子节点为止

####RecyclerBin的实现原理
>会者不难，难者不会，实际上就是一个单项链表，通过一个静态常量一直指向单向链表的头部，需要用的时候从头部拎出一个来，并将静态指针后移一位
```java
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
```