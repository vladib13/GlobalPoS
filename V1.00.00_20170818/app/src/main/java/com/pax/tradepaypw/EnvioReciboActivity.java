package com.pax.tradepaypw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.lang.UCharacter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pax.jemv.demo.R;

import okhttp3.OkHttpClient;

public class EnvioReciboActivity extends AppCompatActivity {

    private Button bSMS;
    private Button bMail;
    private Button bPrinter;
    private LinearLayout lnr;
    private String seleccionMoneda="";
    private String seleccionMonto="";
    private String amount="";
    private EditText txtInput;
    private Button btnEnviar;
    private Button btnCambiar;
    private OkHttpClient mClient = new OkHttpClient();
    private Context mContext;
    private int txto=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_recibo);

        lnr=(LinearLayout) findViewById(R.id.btns);
        btnEnviar=(Button) findViewById(R.id.btnEnviar);
        btnEnviar=(Button) findViewById(R.id.btnRegresar);
        txtInput=(EditText) findViewById(R.id.txtInput);
        bMail=(Button) findViewById(R.id.btnMail);
        bSMS=(Button) findViewById(R.id.btnSMS);
        bPrinter=(Button) findViewById(R.id.btnPrinter);
        mContext=getApplicationContext();
    }
    public void sms(View view){
        txto=1;
        bMail.setVisibility(View.INVISIBLE);
        bPrinter.setVisibility(View.INVISIBLE);
        bSMS.setTranslationY(-bSMS.getY()+bPrinter.getY());
        txtInput.setVisibility(View.VISIBLE);
        txtInput.setHint("Cod. area + nro. movil. Ejm:(5241256225412)");

        txtInput.setInputType(UCharacter.NumericType.DIGIT);
        lnr.setVisibility(View.VISIBLE);

       /* btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

            post("http://201.242.209.32:4567/sms", new  Callback(){

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
            }
        });*/


    }
   /* Call post(String url, Callback callback) throws IOException{
        RequestBody formBody = new FormBody.Builder()
                .add("To", "+584163036505")
                .add("Monto",seleccionMonto)
                .add("Moneda", seleccionMoneda)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call response = mClient.newCall(request);
        response.enqueue(callback);
        return response;

    }*/
    public void mail(View view){
        txto=0;
        bSMS.setVisibility(View.INVISIBLE);
        bPrinter.setVisibility(View.INVISIBLE);
        bMail.setTranslationY(-bMail.getY()+bPrinter.getY());
        txtInput.setVisibility(View.VISIBLE);
        txtInput.setHint("Correo Electronico. Ejm: abc@dominio.com");
        lnr.setVisibility(View.VISIBLE);
    }
    public void change (View view){
        Intent intent =new Intent (mContext,EnvioReciboActivity.class);
        startActivity(intent);
        finish();
    }
    public void send (View view){
        if (txto==0){

            Toast.makeText(mContext,"Enviando recibo via Correo Electronico", Toast.LENGTH_LONG).show();
        }
        else{
            try{
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage("+"+txtInput.getText().toString(),null,"Estimado cliente, hemos registrado de manera exitosa una compra por "+getIntent().getStringExtra("monto")+", con su tarjeta numero "+getIntent().getStringExtra("tdc")+". Gracias por usar nuestros servicios!",null,null);
                Toast.makeText(mContext,"Recibo enviado via SMS", Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                AlertDialog.Builder noSim = new AlertDialog.Builder(getApplicationContext());
                noSim.setMessage("Por favor inserte tarjeta SIM");
                noSim.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialogo = noSim.create();
                dialogo.show();
            }
        }
        Intent intent =new Intent (mContext,EntryMenuActivity.class);
        startActivity(intent);
        finish();
    }
    public void print(View view){
        Toast.makeText(mContext,"Imprimiendo Recibo", Toast.LENGTH_LONG).show();
        Intent intent =new Intent (mContext,EntryMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
