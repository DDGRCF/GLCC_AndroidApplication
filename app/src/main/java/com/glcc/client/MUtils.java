package com.glcc.client;

import android.view.View;

public class MUtils {
    static public abstract class NoShakeListener implements View.OnClickListener {
        private long mLastClickTime = 0;
        private boolean isFastDoubleClick() {
            long nowTime = System.currentTimeMillis();
            if (Math.abs(nowTime - mLastClickTime) < 500) {
                return true; // 快速点击事件
            } else {
                mLastClickTime = nowTime;
                return false; // 单次点击事件
            }
        }

        @Override
        public void onClick(View v) {
            if (isFastDoubleClick()) {
                onFastClick(v);
            } else {
                onSingleClick(v);
            }
        }
        protected void onFastClick(View v) {
        }
        protected abstract void onSingleClick(View v);
    }

}
