package com.example.jy.testapplication;

import android.os.Bundle;

import com.example.jy.testapplication.base.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initHeadView();
        setHeadVisable(true);
        initLeftTitleView("返回");
        initTitleView("标题");
        setRithtTitleViewVisable(true);
        initRithtTitleView("设置");
    }
}
