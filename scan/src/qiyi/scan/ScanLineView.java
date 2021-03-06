package qiyi.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;




/**
 */
public class ScanLineView extends View {

    private Context mContext;
    private Bitmap mScanLine;
    private Rect mRect;
    private TranslateAnimation mAnimation;

    public ScanLineView(Context context) {
        super(context);
        init(context);
    }

    public ScanLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScanLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRect = new Rect();
//        mScanLine = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scan_line);

        mAnimation = new TranslateAnimation(0, 0, 0, 260);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setDuration(2500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setInterpolator(new LinearInterpolator());
        post(new Runnable() {
            @Override
            public void run() {
                startAnimation(mAnimation);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRect.set(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(mScanLine, null, mRect, null);
    }
}
