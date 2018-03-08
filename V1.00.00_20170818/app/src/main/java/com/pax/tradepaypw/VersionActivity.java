package com.pax.tradepaypw;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.pax.jemv.demo.R;

public class VersionActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
    }

    public void backClick(View view){
        finish();
    }
    public void versionClick(View view){
        Intent intent = new Intent(this, ReleaseNotesActivity.class);
        startActivity(intent);
    }

 //   @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.rl_item1:
//                Intent intent = new Intent(this, ReleaseNotesActivity.class);
//                startActivity(intent);
//                break;
//            default:
//                break;
//        }
//    }
}
