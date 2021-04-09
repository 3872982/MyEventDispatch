package com.learning.libmyevent.Listener;

import com.learning.libmyevent.MotionEvent;
import com.learning.libmyevent.View;

public interface OnTouchListener {
    boolean onTouch(View v, MotionEvent event);
}
