package com.example.jy.testapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.jy.testapplication.base.BaseActivity;
import com.example.jy.testapplication.utils.LogUtil;
import com.example.jy.testapplication.utils.UploadUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    public static final String TAG = "MainActivity";
    private Button mUploadBtn;
    private Button mSPBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initViewHeadStatus();
    }

    private void initView() {
        mUploadBtn = (Button) findViewById(R.id.upload_button);
        mSPBtn = (Button) findViewById(R.id.sp_button);
    }

    private void initViewHeadStatus() {
        initHeadView();
        setHeadVisable(true);
        initLeftTitleView("返回");
        initTitleView("标题");
        setRithtTitleViewVisable(true);
        initRithtTitleView("设置");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upload_button:
                LogUtil.d(TAG, "onClick upload_button");
                /*UploadUtil uploadUtil = UploadUtil.getInstance();
                uploadUtil.setOnUploadProcessListener(new OnUploadProcessListenerClient());
                uploadUtil.uploadFile("", "", UploadUtil.IPADRESS, null);*/
                break;
            case R.id.sp_button:
                LogUtil.d(TAG, "onClick sp_button");
                break;
            default:
                break;
        }
    }

    class OnUploadProcessListenerClient implements UploadUtil.OnUploadProcessListener {
        @Override
        public void onUploadDone(int responseCode, String message) {
            LogUtil.d(TAG, "responseCode:" + responseCode + ",message:" + message);
        }

        @Override
        public void onUploadProcess(int uploadSize) {
        }

        @Override
        public void initUpload(int fileSize) {
        }
    }
}
