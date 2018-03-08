package com.pax.tradepaypw;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import com.pax.jemv.demo.R;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private Banner banner;
    private List<String> bannerTitle;
    private List<Integer> imageUrl;
    private TextView tv_jump;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initData();
        initView();

        tv_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                toMainActivity();
            }
        });

        //10秒后自动进入mainactivity
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toMainActivity();
            }
        }, 5000);
    }

    private void initView() {
        tv_jump = (TextView) findViewById(R.id.tv_jump);
        banner = (Banner) findViewById(R.id.banner_splash);
        if (imageUrl.size() <= 1){
            //一张图片，不显示指示器和标题
            banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        }else {
            //显示数字指示器和标题
            banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        }
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(imageUrl);
        //banner.setBannerTitles(bannerTitle);
        //banner.setDelayTime(2000);
        //banner.start();

    }

    private void initData() {
        //本地图片地址
        imageUrl = new ArrayList<>();
//        imageUrl.add(R.drawable.splash1);
//        imageUrl.add(R.drawable.splash2);
//        imageUrl.add(R.drawable.splash3);
        imageUrl.add(R.drawable.pax_logo);

        //Title名称
        bannerTitle = new ArrayList<>();
        bannerTitle.add("一");
        bannerTitle.add("二");
        bannerTitle.add("三");
    }



    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //在页面可见时开始轮播
        banner.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //在页面不可见时停止轮播
        banner.isAutoPlay(false);
    }

    @Override
    protected void onDestroy() {
        //清空消息队列所有Message，防止内存泄露
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
