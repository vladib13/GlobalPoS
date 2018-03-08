package com.pax.tradepaypw;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.app.TradeApplication;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.entity.PollingResult.EOperationType;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.jemv.amex.api.ClssAmexApi;
import com.pax.jemv.amex.model.CLSS_AEAIDPARAM;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.ClssTmAidList;
import com.pax.jemv.clcommon.Clss_MCAidParam;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_TransParam;
import com.pax.jemv.clcommon.Clss_VisaAidParam;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.OnlineResult;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clssentrypoint.model.TransResult;
import com.pax.jemv.clssentrypoint.trans.ClssEntryPoint;
import com.pax.jemv.clssexpresspay.trans.ClssExpressPay;
import com.pax.jemv.clsspaypass.trans.ClssPayPass;
import com.pax.jemv.clsspaywave.trans.ClssPayWave;
import com.pax.jemv.demo.R;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paywave.api.ClssWaveApi;
import com.pax.tradepaypw.abl.core.utils.TrackUtils;
import com.pax.tradepaypw.device.Device;
import com.pax.tradepaypw.pay.constant.EUIParamKeys;
import com.pax.tradepaypw.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.tradepaypw.pay.trans.callback.TradeCallback;
import com.pax.tradepaypw.utils.FileParse;
import com.pax.tradepaypw.utils.PromptMsg;
import com.pax.tradepaypw.utils.ToastUtil;
import com.pax.tradepaypw.utils.Utils;
import com.pax.tradepaypw.view.CustomEditText;
import com.pax.tradepaypw.view.dialog.CustomAlertDialog;

import java.util.Arrays;

import static com.pax.tradepaypw.utils.Utils.bcd2Str;
import static com.pax.tradepaypw.utils.Utils.str2Bcd;

//import com.pax.example.emvdemo.*;

//import com.pax.ipp.service.aidl.Exceptions;

public class SwingCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SwingCardActivity";



    private TextView tv_amount;
    private String amount;
    private ImageView header_back;
    private Button ok_btn;
    private CustomEditText edtCardNo;

    private final static int READ_CARD_OK = 1; // 读卡成功
    private final static int READ_CARD_CANCEL = 2; // 取消读卡
    private final static int READ_CARD_ERR = 3; // 读卡失败
    private final static int READ_CARD_PAUSE = 4; // 读卡暂停
    private EReaderType readerType = null; // 读卡类型

    private boolean supportManual = false; // 是否支持手输

    private int ret = RetCode.EMV_OK;
    private CustomAlertDialog promptDialog ;
    private PollingResult pollingResult;
    private static EReaderType readerMode;
    private ClssEntryPoint entryPoint = ClssEntryPoint.getInstance();



    String pan;

    /**
     * 支持的寻卡类型
     */
    private byte mode; // 寻卡模式

    public static void setReadType(EReaderType type){
        readerMode = type;
    }

    public static EReaderType getReadType(){
        return readerMode;
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //PollingResult pollingResult = null;
            switch (msg.what) {
                case READ_CARD_OK:// 读卡成功
                    Log.i(TAG, " msg.what = READ_CARD_OK");
                    if (pollingResult.getReaderType() == EReaderType.MAG) {
                        setReadType(EReaderType.MAG);
                        Log.i(TAG, " EReaderType.MAG");
                    } else if (pollingResult.getReaderType() == EReaderType.ICC) {
                        setReadType(EReaderType.ICC);
                        Log.i(TAG," EReaderType.ICC");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                Looper.prepare();
                                startEmvTrans();
                               // Looper.prepare();
                               // Toast.makeText(SwingCardActivity.this, "result " + ret, Toast.LENGTH_LONG).show();
                               // Looper.loop();
                            }
                        }).start();
                    } else if (pollingResult.getReaderType() == EReaderType.PICC) {
                        setReadType(EReaderType.PICC);
                        Log.i(TAG, " EReaderType.PICC");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                Looper.prepare();
                                starPiccTrans();
                            }
                        }).start();
                    }
                    break;
                case READ_CARD_CANCEL:
                    Log.i("TAG", "SEARCH CARD CANCEL");
                    try {
                        //TradeApplication.dal.getCardReaderHelper().setIsPause(true);
                        TradeApplication.getDal().getCardReaderHelper().stopPolling();
                        TradeApplication.getDal().getPicc(EPiccType.INTERNAL).close();
                    } catch (PiccDevException e1) {
                        e1.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private static ConditionVariable cv;
    private  void startEmvTrans(){
        ImplEmv emv = new ImplEmv(SwingCardActivity.this);
        emv.ulAmntAuth = Long.parseLong(amount.replace(".", ""));
        Log.i(TAG, "transParam.ulAmntAuth:" + emv.ulAmntAuth);
        emv.ulAmntOther = 0;
        emv.ulTransNo = 1;
        emv.ucTransType = 0x00;

        int ret = emv.startContactEmvTrans();
        Log.i(TAG, "startContactEmvTrans ret= " + ret);

        if (ret == TransResult.EMV_ARQC) {
           toOnlineProc();
            ret = emv.CompleteContactEmvTrans();
        }
        if (ret == TransResult.EMV_ONLINE_APPROVED || ret == TransResult.EMV_OFFLINE_APPROVED || ret == TransResult.EMV_ONLINE_CARD_DENIED) {
            byte[] track2 = ImplEmv.getTlv(0x57);
            String strTrack2 = TradeApplication.getConvert().bcdToStr(track2);
            strTrack2 = strTrack2.split("F")[0];
            pan = strTrack2.split("D")[0];
            //pan = ImplEmv.getTlv(0x57);
            Log.i(TAG, "Start TradeResultActivity");
            Intent intent = new Intent(this, TradeResultActivity.class);
            intent.putExtra("amount", amount);
            intent.putExtra("pan", pan);
            startActivity(intent);
        }
        else
        {
            showErr(ret);
        }
        return;
    }

    private void toOnlineProc() {
        while (true) {
            if (promptDialog == null){
                Log.i(TAG, "toOnlineProc promptDialog == null");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        promptDialog = new CustomAlertDialog(SwingCardActivity.this, CustomAlertDialog.PROGRESS_TYPE);

                        promptDialog.show();
                        promptDialog.setCancelable(false);
                        promptDialog.setTitleText(getString(R.string.prompt_online));

                    }
                });
            }else {
                Log.i(TAG, "promptDialog dismiss");
                promptDialog.dismiss();
                break;
            }
            SystemClock.sleep(3000);
        }
    }


    private void starPiccTrans() {
        //DeviceManager.getInstance().setIDevice(new DeviceImpl());
        Clss_TransParam transParam = new Clss_TransParam();
        transParam.ulAmntAuth = Long.parseLong(amount.replace(".", ""));
        Log.i(TAG, "transParam.ulAmntAuth:" + transParam.ulAmntAuth);
        transParam.ulAmntOther = 0;
        transParam.ulTransNo = 1;
        transParam.ucTransType = 0x00;
      //  DeviceManager.getInstance().getTime(time);
      //  System.arraycopy(time, 0, transParam.aucTransDate, 0, 3);
      //  System.arraycopy(time, 3, transParam.aucTransTime, 0, 3);
        String Transdate = TradeApplication.getDal().getSys().getDate();
        System.arraycopy(str2Bcd(Transdate.substring(2, 8)), 0, transParam.aucTransDate, 0, 3);
        String Transtime = TradeApplication.getDal().getSys().getDate();
        System.arraycopy(str2Bcd(Transtime.substring(8)), 0, transParam.aucTransTime, 0, 3);

        ClssTmAidList[] tmAidList = FileParse.getTmAidLists();
        Clss_PreProcInfo[] preProcInfo = FileParse.getPreProcInfos();
        entryPoint.coreInit();
        ClssPayWave.getInstance().coreInit();
        ClssPayPass.getInstance().coreInit((byte) 1);
        ClssExpressPay.getInstance().coreInit();

        while(true) {
            ret =entryPoint.setConfigParam((byte) /*0x37*/0x36, false, tmAidList, preProcInfo);
            if (ret != RetCode.EMV_OK) {
                showErr(ret);
                Log.e(TAG, "setConfigParam ret = " + ret);
                return;
            }

            ret =entryPoint.entryProcess(transParam);
            if (ret != RetCode.EMV_OK) {
                showErr(ret);
                Log.e(TAG, "entryProcess ret = " + ret);
                return;
            }

            switch (ClssEntryPoint.getInstance().getOutParam().ucKernType) {
                case KernType.KERNTYPE_MC:
                    ret = startMC();
                    break;
                case KernType.KERNTYPE_VIS:
                    ret = startVIS();
                    break;
                case KernType.KERNTYPE_AE:
                    ret = startAE();
                    break;
                default:
                    Log.e(TAG, "KernType error, type = " +entryPoint.getOutParam().ucKernType);
                    showErr(PromptMsg.ONLY_PAYPASS_PAYWAVE);
                    break;
            }
            if (ret == RetCode.CLSS_TRY_AGAIN || ret == RetCode.CLSS_REFER_CONSUMER_DEVICE){
                continue;
            }
            else if (ret != 0) {
                showErr(ret);
            }
            break;
        }

    }

    //Gillian end 20170522
    private void showErr(final int ret) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = PromptMsg.getErrorMsg(ret);
                final CustomAlertDialog dialog = new CustomAlertDialog(SwingCardActivity.this, CustomAlertDialog.ERROR_TYPE);
                dialog.setTitleText(msg);
                dialog.show();
                Device.beepErr();
                dialog.showConfirmButton(true);
                //dialog.showCancelButton(true);
                dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        finish();
                    }
                });
            }
        });
    }

    private int startVIS() {
        Clss_PreProcInfo procInfo = null;
        TransResult transResult = new TransResult();
        ClssPayWave.getInstance().setCallback(new TradeCallback(this));
        //ClssPayWave.getInstance().coreInit();


        byte[] aucCvmReq = new byte[2];
        aucCvmReq[0] = CvmType.RD_CVM_REQ_SIG;
        aucCvmReq[1] = CvmType.RD_CVM_REQ_ONLINE_PIN;
        Clss_VisaAidParam visaAidParam = new Clss_VisaAidParam(100000, (byte) 0, (byte) 2, aucCvmReq, (byte) 0);
        for (int i = 0; i < FileParse.getPreProcInfos().length; i++) {
            if (Arrays.equals(ClssEntryPoint.getInstance().getOutParam().sAID,
                    FileParse.getPreProcInfos()[i].aucAID)) {
                procInfo = FileParse.getPreProcInfos()[i];
                break;
            }
        }
        ret = ClssPayWave.getInstance().setConfigParam(visaAidParam, procInfo);

        ret = ClssPayWave.getInstance().waveProcess(transResult);
        Log.i(TAG, "waveProcess ret = " + ret);
        Log.i(TAG, "transResult = " + transResult.result);
        if (ret == 0) {
            successProcess(ClssPayWave.getInstance().getCVMType(), transResult.result);
            Log.i(TAG, "cvm = " + ClssPayWave.getInstance().getCVMType());
        }
        return ret;
    }


    private int startMC() {
        Clss_PreProcInfo procInfo = null;
        Clss_MCAidParam aidParam = null;
        TransResult transResult = new TransResult();
        ClssPayPass.getInstance().setCallback(new TradeCallback(this));
        //ClssPayPass.getInstance().setCallback(TradeCallback.getInstance(this));

        //ClssPayPass.getInstance().coreInit((byte) 1);

        for (int i = 0; i < FileParse.getPreProcInfos().length; i++) {
            if (Arrays.equals(ClssEntryPoint.getInstance().getOutParam().sAID,
                    FileParse.getPreProcInfos()[i].aucAID)) {
                procInfo = FileParse.getPreProcInfos()[i];
                aidParam = FileParse.getMcAidParams()[i];
                break;
            }
        }
        ClssPayPass.getInstance().setConfigParam(aidParam, procInfo);
        ret = ClssPayPass.getInstance().passProcess(transResult);
        Log.i(TAG, "passProcess ret = " + ret);
        Log.i(TAG, "transResult = " + transResult.result);
        if (ret == 0) {
            successProcess(ClssPayPass.getInstance().getCVMType(), transResult.result);
            Log.i(TAG, "cvm = " + ClssPayPass.getInstance().getCVMType());
        }
        return ret;
    }

    private int startAE(){
        String ssAID;
        String listAID;


        Clss_PreProcInfo procInfo = null;
        CLSS_AEAIDPARAM aidParam = null;
        TransResult transResult = new TransResult();
        ClssExpressPay.getInstance().setCallback(new TradeCallback(this));
        //ClssExpressPay.getInstance().coreInit();
       // Clss_ReaderParam readerParam = new Clss_ReaderParam();
        //System.arraycopy();
        ssAID = bcd2Str(ClssEntryPoint.getInstance().getOutParam().sAID, ClssEntryPoint.getInstance().getOutParam().iAIDLen);
        //Log.i(TAG, "sAID  = " + ssAID);
        for (int i = 0; i < FileParse.getPreProcInfos().length; i++) {
            listAID = bcd2Str(FileParse.getPreProcInfos()[i].aucAID, FileParse.getPreProcInfos()[i].ucAidLen);
            if (ssAID.indexOf(listAID) != -1) {
                //Log.i(TAG, "ssAID.indexOf(listAID) OK");
                procInfo = FileParse.getPreProcInfos()[i];
                aidParam = FileParse.getAeAidParams()[i];
                break;
            }
        }

        //Log.i(TAG, "aidParam.ucAETermCap  = " + Integer.toHexString(aidParam.ucAETermCap) );

        ret = ClssExpressPay.getInstance().setConfigParam(aidParam, procInfo);

        ret = ClssExpressPay.getInstance().expressProcess(transResult);
        Log.i(TAG, "expressProcess ret = " + ret);
        Log.i(TAG, "transResult = " + transResult.result);
        if (ret == 0) {
            successProcess(ClssExpressPay.getInstance().getCVMType(), transResult.result);
            Log.i(TAG, "cvm = " + ClssExpressPay.getInstance().getCVMType());
        }
        return ret;
    }

    private void successProcess(int cvmType, int result) {
        ByteArray tk2 = new ByteArray();
        if (ClssEntryPoint.getInstance().getOutParam().ucKernType == KernType.KERNTYPE_MC){
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{0x57}, (byte) 1, 60, tk2);
        }else if (ClssEntryPoint.getInstance().getOutParam().ucKernType == KernType.KERNTYPE_VIS){
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x57, tk2);
        }
        else if (ClssEntryPoint.getInstance().getOutParam().ucKernType == KernType.KERNTYPE_AE){
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x57, tk2);
        }

        pan = TrackUtils.getPan(bcd2Str(tk2.data));

        if (cvmType == CvmType.RD_CVM_ONLINE_PIN) {
            toConsumeActitivy(result, cvmType);
        }else if (cvmType == CvmType.RD_CVM_NO) {
            if (result == TransResult.EMV_ARQC) {
                toTradeResultActivity();
            }
            else if (result == TransResult.EMV_OFFLINE_APPROVED) {
                toTradeResultActivity_tc();
            }
        }else{
            if (result == TransResult.EMV_ARQC) {
                toTradeResultActivity();
            }
            else if (result == TransResult.EMV_OFFLINE_APPROVED) {
                toTradeResultActivity_tc();
            }
        }

    }

    private void toTradeResultActivity() {
        while (true) {
            if (promptDialog == null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        promptDialog = new CustomAlertDialog(SwingCardActivity.this, CustomAlertDialog.PROGRESS_TYPE);

                        promptDialog.show();
                        promptDialog.setCancelable(false);
                        promptDialog.setTitleText(getString(R.string.prompt_online));

                    }
                });
            }else {
                Log.i(TAG, "promptDialog dismiss");
                promptDialog.dismiss();
                break;
           }
           SystemClock.sleep(3000);
        }
        if (ClssEntryPoint.getInstance().getOutParam().ucKernType == KernType.KERNTYPE_AE){
            int result = OnlineResult.ONLINE_APPROVE;
            byte[] aucRspCode = "00".getBytes();
            byte[] aucAuthCode ="123456".getBytes();;
            int sgAuthDataLen = 5;
            byte[] sAuthData = Utils.str2Bcd("1234567890");
            byte[] sIssuerScript = Utils.str2Bcd("9F1804AABBCCDD86098424000004AABBCCDD");
            int sgScriptLen = 18;
            ClssExpressPay.getInstance().amexFlowComplete(result,aucRspCode, aucAuthCode,sAuthData,sgAuthDataLen,sIssuerScript, sgScriptLen);
        }
        Log.i(TAG, "Start TradeResultActivity");
        Intent intent = new Intent(this, TradeResultActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("pan", pan);
        startActivity(intent);
     }

    private void toTradeResultActivity_tc() {
        while (true) {
            if (promptDialog == null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        promptDialog = new CustomAlertDialog(SwingCardActivity.this, CustomAlertDialog.PROGRESS_TYPE);

                        promptDialog.show();
                        promptDialog.setCancelable(false);
                        promptDialog.setTitleText(getString(R.string.prompt_offline));

                    }
                });
            }else {
                Log.i(TAG, "promptDialog dismiss");
                promptDialog.dismiss();
                break;
            }
            SystemClock.sleep(3000);
        }
        Log.i(TAG, "Start TradeResultActivity");
        Intent intent = new Intent(this, TradeResultActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("pan", pan);
        startActivity(intent);
    }

    private void toConsumeActitivy(int result, int cvmtype) {


        Intent intent = new Intent(SwingCardActivity.this, ConsumeActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("pan", pan);
       // intent.putExtra("result", result);
      //  intent.putExtra("cvmtype", cvmtype);
        startActivity(intent);
//            edtCardNo.setFocusable(true);
//            edtCardNo.setText(PanUtils.separateWithSpace(pan));
//            ok_btn.setBackgroundResource(R.drawable.enter);
//            ok_btn.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_swing_card);
        setContentView(R.layout.activity_bankcad_pay);
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());

        tv_amount = (TextView) findViewById(R.id.tv_amount);
        Intent intent = getIntent();
        amount = intent.getStringExtra("amount");
        tv_amount.setText(amount);
        header_back = (ImageView) findViewById(R.id.header_back);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        header_back.setOnClickListener(this);
        ok_btn.setOnClickListener(this);

        edtCardNo = (CustomEditText) findViewById(R.id.bank_card_number);
        edtCardNo.setIMEEnabled(false, true);

        loadParam();
        new SearchCardThread().start();

    }


    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        // 寻卡方式，默认挥卡
        try {
            mode = bundle.getByte(EUIParamKeys.CARD_SEARCH_MODE.toString(), SearchMode.INSERT_TAP);
            if ((mode & SearchMode.KEYIN) == SearchMode.KEYIN) { // 是否支持手输卡号
                supportManual = true;
            } else {
                supportManual = false;
            }

            readerType = toReaderType(mode);
        } catch (Exception e) {
            Log.e("loadParam",e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * 获取ReaderType
     *
     * @param mode
     * @return
     */
    private EReaderType toReaderType(byte mode) {
        mode &= ~SearchMode.KEYIN;
        EReaderType[] types = EReaderType.values();
        for (EReaderType type : types) {
            if (type.getEReaderType() == mode)
                return type;
        }
        return null;
    }

    // 寻卡线程
    class SearchCardThread extends Thread {

        @Override
        public void run() {
            try {
                ICardReaderHelper cardReaderHelper = TradeApplication.getDal().getCardReaderHelper();
                if (readerType == null) {
                    return;
                }
                pollingResult = cardReaderHelper.polling(readerType, 600 * 1000);
                cardReaderHelper.stopPolling();
                if (pollingResult.getOperationType() == EOperationType.CANCEL
                        || pollingResult.getOperationType() == EOperationType.TIMEOUT) {
                    Log.i("TAG", "CANCEL | TIMEOUT");
                    //ToastUtil.showToast(SwingCardActivity.this, "CANCEL | TIMEOUT");
                    handler.sendEmptyMessage(READ_CARD_CANCEL);
//                } else if (pollingResult.getOperationType() == EOperationType.PAUSE) {
//                    Log.i("TAG", "EOperationType.PAUSE");
//                    //ToastUtil.showToast(SwingCardActivity.this, "EOperationType.PAUSE");
//                    handler.sendEmptyMessage(READ_CARD_PAUSE);
                } else {
                    Log.i("TAG", "READ_CARD_OK");
                    //Device.beepPrompt();
                    //ToastUtil.showToast(SwingCardActivity.this, "READ_CARD_OK");
                    handler.sendEmptyMessage(READ_CARD_OK);
                }

            } catch (PiccDevException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(READ_CARD_ERR);
            } catch (IccDevException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(READ_CARD_ERR);
            } catch (MagDevException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(READ_CARD_ERR);
            }

        }

    }

    public void showEnterPin(Context context, String pan, Boolean isOnlinePin, int offlinePinLeftTimes){
        Intent intent = new Intent(context, ConsumeActivity.class);
        Log.i("log", "enterPin intent " );
        //intent.putExtra("header", header);
        if (isOnlinePin == true)
            intent.putExtra("isOnlinePin", 1);
        else
            intent.putExtra("isOnlinePin", 0);
        intent.putExtra("offlinePinLeftTimes", offlinePinLeftTimes);
        intent.putExtra("pan", pan);
        //intent.putExtra("cvmtype", cvmtype);
        Log.i("log", "enterPin intent startActivity" );
        startActivity(intent);
    }

    private void ShowCustomDialog(String carNum) {
        final CustomAlertDialog dialog = new CustomAlertDialog(SwingCardActivity.this);
        //dialog.showContentText(false);
        dialog.setTitleText("卡号确认");
        dialog.setContentText(carNum);
        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.showConfirmButton(true);
        dialog.showCancelButton(true);

        dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                ToastUtil.showToast(SwingCardActivity.this, "取消");
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                Intent intent = new Intent(SwingCardActivity.this, ConsumeActivity.class);
                intent.putExtra("amount", amount);
                intent.putExtra("pan", pan);
                startActivity(intent);
                dialog.dismiss();
            }
        });

    }
    @Override
    protected void onStop() {
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handler.sendEmptyMessage(READ_CARD_CANCEL);
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                handler.sendEmptyMessage(READ_CARD_CANCEL);
                finish();
                break;
            case R.id.ok_btn:
                //TODO
                //test
                Intent intent = new Intent(SwingCardActivity.this, ConsumeActivity.class);
                intent.putExtra("amount", amount);
                intent.putExtra("pan", pan);
                startActivity(intent);
                //ShowCustomDialog("8392894789283993");
                break;
            default:
                break;

        }
    }

}
