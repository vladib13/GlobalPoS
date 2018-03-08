package com.pax.tradepaypw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pax.jemv.amex.api.ClssAmexApi;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clssentrypoint.trans.ClssEntryPoint;
import com.pax.jemv.demo.R;
import com.pax.jemv.emv.api.EMVCallback;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paywave.api.ClssWaveApi;
import com.pax.mposapi.PrinterManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TradeResultActivity extends AppCompatActivity {
    private TextView tv_amount;
    private TextView tv_cardNum;
    private TextView tv_date;
    private String xs="";
    private double paso;
    private ScrollView sv;
    private Spinner montoPropina;
    private Handler handler = new Handler();
    private ClssEntryPoint entryPoint = ClssEntryPoint.getInstance();
    private double propina;
    private TextView montoGrande;
    private TextView total;
    private String tdc;
    private String[] letra = {"Sin Propina","10 %","25%","50%","75%","100%"};
    WindowManager.LayoutParams p;
    PaintView mView;
    private Context mContext;
    private Bitmap mSignBitmap;
    static int bitmapWidth = PrinterManager.PRN_DOTS_PER_LINE;
    static int bitmapHeight = PrinterManager.PRN_DOTS_PER_LINE / 2;
    private Button btnOk;
    static final int BACKGROUND_COLOR = Color.WHITE;
    static final int BRUSH_COLOR = Color.BLACK;
    private String signFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initView();
        initData();

        Window window = getWindow();
        Display display = getWindowManager().getDefaultDisplay();
        p = window.getAttributes();
        /*
		p.height = MPosApiDefines.PRN_DOTS_PER_LINE + 8;
		p.width = MPosApiDefines.PRN_DOTS_PER_LINE + 38;
		*/
        p.height = display.getHeight() * 3 / 4;
        p.width = display.getWidth() * 9 / 10;
        window.setAttributes(p);
        bitmapWidth = p.width;
        bitmapHeight = p.height;
        mContext= getApplicationContext();
        mView = new PaintView(this);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tablet_view);
        sv = (ScrollView) findViewById(R.id.parentscr);
        linearLayout.addView(mView);
        mView.requestFocus();

        //singleTask模式
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(TradeResultActivity.this, EnvioReciboActivity.class);
                intent.putExtra("monto",tv_amount.getText().toString());
                startActivity(intent);
            }
        }, 90000);

    }
//here1
    private void initData() {

        int iRet;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ByteArray byteArray = new ByteArray();
        if (entryPoint.getOutParam().ucKernType == KernType.KERNTYPE_MC) {
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x9F,0x26},(byte) 2,10,byteArray);
            byte[] a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);

            iRet = ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x95},(byte) 1,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);

            Log.i("Clss_TLV_MC iRet 0x95", iRet + "");

            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x4F},(byte) 1,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x50},(byte) 1,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x9F, 0x12},(byte) 2,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x9B},(byte) 1,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x9F, 0x26},(byte) 2,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte)0x9F, 0x36},(byte) 2,10,byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);

        }
        else if(entryPoint.getOutParam().ucKernType == KernType.KERNTYPE_VIS) {
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x9F26, byteArray);
            byte[] a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x95, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x4F, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short)0x50, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x9F12, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x9B, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x9F26, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssWaveApi.Clss_GetTLVData_Wave((short) 0x9F36, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
        }
        else if(entryPoint.getOutParam().ucKernType == KernType.KERNTYPE_AE) {
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x9F26, byteArray);
            byte[] a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x95, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x4F, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short)0x50, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x9F12, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x9B, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x9F26, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            ClssAmexApi.Clss_GetTLVData_AE((short) 0x9F36, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
        }
        else{ // contact
            EMVCallback.EMVGetTLVData((short) 0x9F26, byteArray);
            byte[] a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x95, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x4F, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short)0x50, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x9F12, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x9B, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x9F26, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
            EMVCallback.EMVGetTLVData((short) 0x9F36, byteArray);
            a = new byte[byteArray.length];
            System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
        }
        tv_date.setText(dateFormat.format(new Date()));
        tdc = getIntent().getStringExtra("pan");
        tv_cardNum.setText(tdc);
        tv_amount.setText("$ "+getIntent().getStringExtra("amount"));
        montoPropina.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, letra));
        montoPropina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getSelectedItemPosition()){
                    case 0: propina=0;
                        break;
                    case 1: propina=0.1;
                        break;
                    case 2: propina=0.25;
                        break;
                    case 3: propina=0.5;
                        break;
                    case 4: propina=0.75;
                        break;
                    case 5: propina=1;
                        break;
                }

                java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
                xs =xs+df.format((paso+(paso*propina)));
                total.setText("$ "+xs.toString());
                montoGrande.setText("$ "+xs.toString());
                xs="";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        paso = Double.parseDouble(getIntent().getStringExtra("amount"));


    }

    private void initView() {
        tv_amount = (TextView) findViewById(R.id.ecash_amount);
       // image=(SignatureActivity.PaintView) findViewById(R.id.tablet_view);
        tv_cardNum = (TextView) findViewById(R.id.ecash_bankcardNo);
        tv_date = (TextView) findViewById(R.id.ecash_time);
        montoGrande = (TextView) findViewById(R.id.total);
        total=(TextView) findViewById(R.id.total_monto);

        montoPropina = (Spinner) findViewById(R.id.propina_monto);
    }

    public void clear (View view){
        mView.clear();
    }
    public void enterClick(View view){
        Intent intent = new Intent(this, EnvioReciboActivity.class);
        intent.putExtra("monto",tv_amount.getText().toString());
        intent.putExtra("tdc",tdc);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(this, EnvioReciboActivity.class);
            intent.putExtra("monto",tv_amount.getText().toString());
            startActivity(intent);
        }

        return super.onKeyDown(keyCode, event);
    }

    private String createFile() {
        ByteArrayOutputStream baos = null;
        String path = null;
        try {
            String sign_dir = Environment.getExternalStorageDirectory() + File.separator;
            path = sign_dir + System.currentTimeMillis() + ".png";
            baos = new ByteArrayOutputStream();
            mSignBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] photoBytes = baos.toByteArray();
            if (photoBytes != null) {
                new FileOutputStream(new File(path)).write(photoBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    /**
     * This view implements the drawing canvas.
     *
     * It handles all of the input events and drawing functions.
     */
    class PaintView extends View {
        private Paint paint;
        private Canvas cacheCanvas;
        private Bitmap cacheBitmap;
        private Path path;

        public Bitmap getcacheBitmap() {
            return cacheBitmap;
        }

        public PaintView(Context context) {
            super(context);
            init();
        }

        private void init(){
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(BRUSH_COLOR);
            path = new Path();
            cacheBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            cacheCanvas = new Canvas(cacheBitmap);
            cacheCanvas.drawColor(BACKGROUND_COLOR);
        }
        public void clear() {
            if (cacheCanvas != null) {

                paint.setColor(BACKGROUND_COLOR);
                cacheCanvas.drawPaint(paint);
                paint.setColor(BRUSH_COLOR);
                cacheCanvas.drawColor(BACKGROUND_COLOR);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // canvas.drawColor(BRUSH_COLOR);

            canvas.drawBitmap(cacheBitmap, 0, 0, null);
            canvas.drawPath(path, paint);
        }

		/*
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {

			int curW = cacheBitmap != null ? cacheBitmap.getWidth() : 0;
			int curH = cacheBitmap != null ? cacheBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (cacheBitmap != null) {
				newCanvas.drawBitmap(cacheBitmap, 0, 0, null);
			}
			cacheBitmap = newBitmap;
			cacheCanvas = newCanvas;
		}
		*/

        private float cur_x, cur_y;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();
            sv.requestDisallowInterceptTouchEvent(true);

           // sv.setVisibility(View.INVISIBLE);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    cur_x = x;
                    cur_y = y;
                    path.moveTo(cur_x, cur_y);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    path.quadTo(cur_x, cur_y, x, y);
                    cur_x = x;
                    cur_y = y;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    cacheCanvas.drawPoint(cur_x, cur_y, paint);
                    cacheCanvas.drawPath(path, paint);
                    path.reset();
                    break;
                }
            }

            invalidate();

            return true;
        }
    }

}
