package com.pax.tradepaypw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pax.jemv.demo.R;
import com.pax.mposapi.PrinterManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {
    LayoutParams p;
    PaintView mView;
    private Context mContext;
    private Bitmap mSignBitmap;
    static int bitmapWidth = PrinterManager.PRN_DOTS_PER_LINE;
    static int bitmapHeight = PrinterManager.PRN_DOTS_PER_LINE / 2;
    private Button btnOk;
    static final int BACKGROUND_COLOR = Color.WHITE;
    static final int BRUSH_COLOR = Color.BLACK;
    //sign save to this path if you choose to save it.
    private String signFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        setContentView(R.layout.activity_signature);

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
        linearLayout.addView(mView);
        mView.requestFocus();
        Button btnClear = (Button) findViewById(R.id.tablet_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override//�������ť
            public void onClick(View v) {
                mView.clear();
            }
        });

        btnOk = (Button) findViewById(R.id.tablet_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
////				mSignBitmap = mView.getcacheBitmap();
//				Bitmap origBMP = mView.getcacheBitmap();
//				Matrix matrix = new Matrix();
//                matrix.postScale(0.3f,0.3f);
//                mSignBitmap = Bitmap.createBitmap(origBMP,0,0,origBMP.getWidth(),origBMP.getHeight(),matrix,true);
//
////				signFilePath = createFile();
//
////				BitmapFactory.Options options = new BitmapFactory.Options();
////				options.inSampleSize = 15;
////				options.inTempStorage = new byte[5 * 1024];
////				Bitmap zoombm = BitmapFactory.decodeFile(signFilePath, options);
//
                Intent intent2 = new Intent(SignatureActivity.this,TradeResultActivity.class);
                startActivity(intent2);
//				intent.putExtra("sign", mSignBitmap);
                finish();
            }
        });

        Button btnCancel = (Button)findViewById(R.id.tablet_cancel);
       /* btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override//����ȡ��ť
            public void onClick(View v) {

            }
        });*/
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
            cacheBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
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

    public void cancel (View view){
        Intent intent = new Intent(SignatureActivity.this,SwingCardActivity.class);
        startActivity(intent);
        finish();
    }
}
