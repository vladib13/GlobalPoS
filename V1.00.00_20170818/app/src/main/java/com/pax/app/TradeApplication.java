package com.pax.app;

import android.app.Application;
import android.util.Log;

import com.pax.dal.IDAL;
//import com.pax.gl.IGL;
//import com.pax.gl.convert.IConvert;
//import com.pax.gl.impl.GLProxy;
import com.pax.jemv.clcommon.ClssTmAidList;
import com.pax.jemv.clcommon.Clss_MCAidParam;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.neptunelite.api.DALProxyClient;
import com.pax.tradepaypw.utils.FileParse;
import com.pax.tradepaypw.utils.FileUtils;

/**
 * Created by chenld on 2017/3/13.
 */

public class TradeApplication extends Application {
    private static final String TAG = "TradeApplication";
    private static TradeApplication tradeApplication;
    public final static String APP_VERSION = "V1.00.00_20170315";

    // 获取IPPI常用接口
    private static IDAL dal;
    //public static IGL gl;
    private static IConvert convert;

    private String mp4 = "A920.mp4";

    public static IDAL getDal() {
        return dal;
    }

    public static IConvert getConvert() {
        return convert;
    }

    public static TradeApplication getInstance() {
        return tradeApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TradeApplication.tradeApplication = this;

        init();
    }

    private void init() {
        // 获取IPPI常用接口
        DALProxyClient dalProxyClient = DALProxyClient.getInstance();
        try {if (dal == null) {
            dal = dalProxyClient.getDal( tradeApplication.getApplicationContext() );
            Log.i("FinancialApplication:", "dalProxyClient finished.");
        }
        }catch (Exception e){
            //e.printStackTrace();
            Log.e("dalProxyClient",e.getMessage());
        }
        //gl = new GLProxy(tradeApplication).getGL();;
        //convert = gl.getConvert();new ConverterImp();
        convert = new ConverterImp();

        FileParse.parseAidFromAssets(tradeApplication, "aid.ini");

        FileParse.parseCapkFromAssets(tradeApplication, "capk.ini");
        Log.i(TAG, "init: ");

        String file = this.getFilesDir().getPath() + "/" + mp4;
        try {
            FileUtils.copyFileFromAssert(this, mp4, file);
        }catch (Exception e){
            Log.e("File",e.getMessage());
        }
    }

    static {
        System.loadLibrary("F_DEVICE_LIB_Android");
        System.loadLibrary("F_PUBLIC_LIB_Android");
        System.loadLibrary("F_ENTRY_LIB_Android");
        System.loadLibrary("F_MC_LIB_Android");
        System.loadLibrary("F_WAVE_LIB_Android");
        System.loadLibrary("F_AE_LIB_Android");
        System.loadLibrary("JniEMV_V1.00.00_20170721");
        System.loadLibrary("JniEntry_V1.00.00_20170616");
        System.loadLibrary("JniMC_V1.00.00_20170616");
        System.loadLibrary("JniWave_V1.00.00_20170616");
        System.loadLibrary("JniAmex_V1.00.00_20170614");
    }

}
