package com.pax.tradepaypw;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.pax.app.IConvert;
import com.pax.app.TradeApplication;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.jemv.demo.R;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.emv.api.EMVCallback;
import com.pax.tradepaypw.device.Device;
import com.pax.tradepaypw.pay.Constants;
import com.pax.tradepaypw.utils.EnterAmountTextWatcher;
import com.pax.tradepaypw.utils.KeyBoardUtils;
import com.pax.tradepaypw.view.CustomEditText;
import com.pax.tradepaypw.view.MenuPage;
import com.pax.tradepaypw.view.SoftKeyboardPosStyle;
import com.pax.tradepaypw.view.dialog.AdsDialog;
import com.pax.tradepaypw.view.dialog.CustomAlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final int CHECK_OPER_LOGIN = 0;
    private static final int KEY_BOARD_CANCEL = 1;
    private static final int KEY_BOARD_OK = 2;
    private static final String AID_FILE = "aid.ini";
    private static final String CAPK_FILE = "capk.ini";

    private CustomEditText edtAmount; // 金额输入框
    private SoftKeyboardPosStyle softKeyboard; // 软键盘
    private FrameLayout flkeyBoardContainer;
    private LinearLayout llMenu;
    private VideoView videoView;
    private EMVCallback emvCallback = EMVCallback.getInstance();

    private AdsDialog dialog;
    private static ConditionVariable cv;
    private Map<ETermInfoKey,String> infos;

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case KEY_BOARD_OK:
                    String amount = edtAmount.getText().toString().trim();
                    if (amount != null && !amount.equals("0.00") && !amount.equals("")){
                        KeyBoardUtils.hide(MainActivity.this, flkeyBoardContainer);
                        Intent intent = new Intent(MainActivity.this, SwingCardActivity.class);
                        intent.putExtra("amount", amount);
                        startActivity(intent);
                    }
                    break;
                case KEY_BOARD_CANCEL:
                    edtAmount.setText("");
                    KeyBoardUtils.hide(MainActivity.this, flkeyBoardContainer);
                    llMenu.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new AdsDialog(this, R.style.Transparent);
        dialog.show();


        //emvCallback.setCallbackListener(emvCallbackListener);
      //  editText = (EditText)findViewById(R.id.edit_text); //Gillian 20170504

   //     btn = (Button)findViewById(R.id.btn);
   //     btn.setOnClickListener(this);

       // dal = FinancialApplication.getDal();
        //infos = dal.getSys().getTermInfo();
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());

        Device.writeTMK(TradeApplication.getConvert().strToBcd("1234567890123456", IConvert.EPaddingPosition.PADDING_LEFT));
        Device.writeTPK(TradeApplication.getConvert().strToBcd("1234567890123456",  IConvert.EPaddingPosition.PADDING_LEFT), null);
        Log.i("writeKey", " load default KEY into PED");
        initView();
        setListeners();
    }


    private void initView() {
        edtAmount = (CustomEditText) findViewById(R.id.amount_edtext);
        // 金额输入框处理
        //edtAmount.setHint(getString(R.string.amount_default));
        edtAmount.setInputType(InputType.TYPE_NULL);
        edtAmount.setIMEEnabled(false, true);
        flkeyBoardContainer = (FrameLayout) findViewById(R.id.fl_trans_softkeyboard);

        softKeyboard = (SoftKeyboardPosStyle) findViewById(R.id.soft_keyboard_view);

        llMenu = (LinearLayout) findViewById(R.id.ll_menu);
        videoView = (VideoView) findViewById(R.id.video);

    }

    private void setListeners() {
        softKeyboard.setOnItemClickListener(new SoftKeyboardPosStyle.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int index) {
                if (index == KeyEvent.KEYCODE_ENTER) {
                    handler.sendEmptyMessage(KEY_BOARD_OK);
                } else if (index == Constants.KEY_EVENT_CANCEL) {
                    handler.sendEmptyMessage(KEY_BOARD_CANCEL);
                }
            }
        });

        edtAmount.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtAmount.setFocusable(true);
                KeyBoardUtils.show(MainActivity.this, flkeyBoardContainer);
                llMenu.setVisibility(View.GONE);
                return false;
            }
        });

        edtAmount.addTextChangedListener(new EnterAmountTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final Uri uri = Uri.parse(MainActivity.this.getFilesDir().getPath() + "/A920.mp4");
                videoView.setVideoURI(uri);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        videoView.setVideoURI(uri);
                        videoView.start();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void resetUI() {
        edtAmount.setText("");
        edtAmount.setFocusable(false);
        edtAmount.setFocusableInTouchMode(true);
        edtAmount.requestFocus();
        KeyBoardUtils.hide(MainActivity.this, flkeyBoardContainer);
        llMenu.setVisibility(View.VISIBLE);
    }

    /*
 * 创建菜单
 */
    private MenuPage createMenu() {
//        MenuPage.Builder builder = new MenuPage.Builder(MainActivity.this, 9, 3)
//                // 余额查询
//                .addTransItem(getString(R.string.trans_balance), R.drawable.app_balance,
//                        new BalanceTrans(MainActivity.this, handler, listener))
//                // 查看版本
//                .addActionItem(getString(R.string.version), R.drawable.app_version, createDispActionForVersion());
//
//        return builder.create();
        return null;
    }


    public void aidClick(View view){
        readData(AID_FILE, "aid");
    }
    public void capkClick(View view){
        readData(CAPK_FILE, "capk");
    }
    public void versionClick(View view){
        //showVersionDialog(TradeApplication.APP_VERSION);
        Intent intent = new Intent(this, VersionActivity.class);
        startActivity(intent);
    }

    private void showVersionDialog(String version) {
        final CustomAlertDialog dialog = new CustomAlertDialog(MainActivity.this);
        //dialog.showContentText(false);
        dialog.setTitleText(getResources().getString(R.string.app_version));
        dialog.setContentText(version);
        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.showConfirmButton(true);
        //dialog.showCancelButton(true);

        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });

    }

    void readData(String fileName, String key){

        byte[] bytes = new byte[1024];
        int len = -1;
        StringBuffer buffer = new StringBuffer();

        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(fileName);
            while ((len = inputStream.read(bytes)) != -1){
                buffer.append(new String(bytes, 0, len));
            }
            inputStream.close();

            String datas = String.valueOf(buffer);

            Intent intent = new Intent(this, ViewParamActivity.class);
            intent.putExtra(key, datas);
            startActivity(intent);
        } catch (IOException e) {
            Log.e("readData",e.getMessage());
            //e.printStackTrace();
        }

    }

}
