package qiyi.scan;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import qiyi.businessmodule.StringUtils;

/**
 * Created by jiangjingbo on 2017/6/17.
 */

public class ScanMainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_main);
        LinearLayout lt = (LinearLayout)findViewById(R.id.lt);
        ScanLineView lineView = new ScanLineView(this);
        lineView.draw(new Canvas());
        lt.addView(lineView);

        StringUtils utils = new StringUtils();
        int x = utils.getSum(3,6);
        if(x > 5)
        Log.e("TAG","x = "+x);


    }
}
