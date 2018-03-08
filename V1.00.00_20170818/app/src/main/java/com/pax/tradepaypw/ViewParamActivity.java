package com.pax.tradepaypw;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.pax.jemv.demo.R;

public class ViewParamActivity extends AppCompatActivity {
    private TextView tv_param;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_param);

        initView();

    }

    private void initView() {
        tv_param = (TextView) findViewById(R.id.tv_param);
        Intent intent = getIntent();
        String aid = intent.getStringExtra("aid");
        String capk = intent.getStringExtra("capk");
        if (aid != null ){
            tv_param.setText(aid);
        }else if (capk != null ){
            tv_param.setText(capk);
        }

    }

    public void backClick(View view){
        finish();
    }
}
