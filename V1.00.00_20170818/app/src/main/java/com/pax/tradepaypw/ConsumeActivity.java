package com.pax.tradepaypw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.pax.dal.entity.EReaderType;
import com.pax.dal.exceptions.PedDevException;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.demo.R;
import com.pax.tradepaypw.abl.core.utils.PanUtils;
import com.pax.tradepaypw.view.dialog.CustomAlertDialog;
import com.pax.tradepaypw.view.dialog.InputPwdDialog;

public class
ConsumeActivity extends AppCompatActivity {

    private TextView tv_amount;
    private String amount;
    private int result;
    private String pan;
    private int isOnlinePin;
    private int offlinePinLeftTimes;

//    private SoftKeyboardPasswordStyle soft_keyboard_view;
    private Handler handler = new Handler();
    private InputPwdDialog dialog = null;

    private CustomAlertDialog promptDialog;
    //private boolean isFirstStart = true;// 判断界面是否第一次加载


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consume);
//        soft_keyboard_view = (SoftKeyboardPasswordStyle) findViewById(R.id.soft_keyboard_view);

        tv_amount = (TextView) findViewById(R.id.tv_amount);
        Intent intent = getIntent();
        amount = intent.getStringExtra("amount");
        result = intent.getIntExtra("result", 0);

        isOnlinePin = intent.getIntExtra("isOnlinePin", 1);
        //Log.i("ConsumeActivity", "isOnlinePin = " + isOnlinePin );
        offlinePinLeftTimes = intent.getIntExtra("offlinePinLeftTimes", 0);
        //Log.i("ConsumeActivity", "offlinePinLeftTimes = " + offlinePinLeftTimes );
        pan = intent.getStringExtra("pan");

        tv_amount.setText(amount);
//        soft_keyboard_view.setOnItemClickListener(new SoftKeyboardPasswordStyle.OnItemClickListener() {
//            @Override
//            public void onItemClick(View v, int index) {
//                //工作在子线程
//                if (index == KeyEvent.KEYCODE_ENTER) {
//
//                    ShowSuccessDialog();
//                } else if (index == Constants.KEY_EVENT_CANCEL) {
//                    Intent intent1 = new Intent(ConsumeActivity.this, MainActivity.class);
//                    startActivity(intent1);
//                }
//            }
//
//
//        });
       if (isOnlinePin == 1)
            initInputPwdDialog();
        else
            initInputOfflPwdDialog();
    }
    // 当页面加载完成之后再执行弹出键盘的动作
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            if (isFirstStart) {
//                initInputPwdDialog();
//                isFirstStart = false;
//            }
//        }
//    }

    private void initInputPwdDialog()  {
        Log.i("InputPwdDialog", "creat  initInputPwdDialog");
        dialog = new InputPwdDialog(this, handler, getString(R.string.entry_card_password),
                getString(R.string.without_password));
        //dialog.setTimeout();
        dialog.setcvmType(isOnlinePin);
        dialog.setCancelable(true);
        dialog.setPwdListener(new InputPwdDialog.OnPwdListener() {
            @Override
            public void onSucc(String data) {
                Log.i("InputPwdDialog", "dialog OnSucc");
                if (data == null)
                    GetPinEmv.getInstance().setPinResult(RetCode.EMV_NO_PASSWORD);
                else {
                    GetPinEmv.getInstance().setPinResult(RetCode.EMV_OK);
                    GetPinEmv.getInstance().setPinData(data);
                }
                if (SwingCardActivity.getReadType() == EReaderType.ICC) {
                    dialog.dismiss();

                    ImplEmv.pinEnterReady();
                    //ShowSuccessDialog();
                    finish();
                    //Intent intent1 = new Intent(ConsumeActivity.this, SwingCardActivity.class);
                    //startActivity(intent1);
                }
                else {
                    ShowSuccessDialog();
                    dialog.dismiss();
                }

            }

            @Override
            public void onErr() {
                Log.i("InputPwdDialog", "dialog onErr");
                dialog.dismiss();
                ImplEmv.pinEnterReady();
                if (SwingCardActivity.getReadType() == EReaderType.ICC) {
                    finish();
                }else {
                    Intent intent1 = new Intent(ConsumeActivity.this, MainActivity.class);
                    startActivity(intent1);
                }

            }
        });
        dialog.show();
        dialog.inputOnlinePin(PanUtils.getPanBlock(getIntent().getStringExtra("pan"), PanUtils.EPanMode.X9_8_WITH_PAN));
    }

    private void initInputOfflPwdDialog() {
        Log.i("initInputOfflPwdDialog", "creat  initInputOfflPwdDialog");
        dialog = new InputPwdDialog(this, handler, getString(R.string.entry_card_password),
                getString(R.string.without_password));
        Log.i("InputPwdDialog", "new InputPwdDialog");
        dialog.setcvmType(isOnlinePin);
        Log.i("InputPwdDialog", " setcvmType");
        //dialog.setTimeout();
        dialog.setCancelable(true);
        Log.i("InputPwdDialog", " setCancelable");
        dialog.setPwdListener(new InputPwdDialog.OnPwdListener() {
            @Override
            public void onSucc(String data) {
                Log.i("initInputOfflPwdDialog", "dialog OnSucc");
                if (data == null)
                    GetPinEmv.getInstance().setPinResult(RetCode.EMV_OK);
                else {
                    GetPinEmv.getInstance().setPinResult(RetCode.EMV_OK);
                    //GetPinEmv.getInstance().setPinData(data);
                }
                dialog.dismiss();
                finish();
            }

            @Override
            public void onErr() {
                Log.i("initInputOfflPwdDialog", "dialog onErr");
                dialog.dismiss();
                //MainActivity.pinEnterReady();
                //Intent intent1 = new Intent(ConsumeActivity.this, MainActivity.class);
                //startActivity(intent1);
                finish();
            }
        });
        dialog.show();
        Log.i("InputPwdDialog", " show");
        try {
            dialog.inputOfflinePlainPin();
        } catch (PedDevException e) {
            Log.i("inputOfflinePlainPin", "e :" + e.getErrCode());
            e.printStackTrace();
        }
    }

    private void ShowSuccessDialog() {
        Log.i("InputPwdDialog", "ShowSuccessDialog");
        toTradeResultActivity();
    }

    private void toTradeResultActivity() {
        while (true) {
            if (promptDialog == null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("InputPwdDialog", "toTradeResultActivity online Processing");
                        promptDialog = new CustomAlertDialog(ConsumeActivity.this, CustomAlertDialog.PROGRESS_TYPE);

                        promptDialog.show();
                        promptDialog.setCancelable(false);
                        promptDialog.setTitleText(getString(R.string.prompt_online));

                    }
                });
            }else {
                Log.i("InputPwdDialog", "promptDialog dismiss");
                promptDialog.dismiss();
                break;
            }
            SystemClock.sleep(3000);
        }

        ImplEmv.pinEnterReady();
        Intent intent = new Intent(this, TradeResultActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("pan", getIntent().getStringExtra("pan"));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }



}
