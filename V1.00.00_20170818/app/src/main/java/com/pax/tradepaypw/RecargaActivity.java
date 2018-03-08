package com.pax.tradepaypw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.pax.jemv.demo.R;

import okhttp3.OkHttpClient;

public class RecargaActivity extends AppCompatActivity {

    private EditText mTo;
    private RadioButton mMonto1;
    private RadioButton mMonto2;
    private RadioButton mMonto3;
    private RadioButton mMonto4;
    private RadioButton mMonto5;
    private RadioGroup grupo;
    private String seleccionMoneda="Pesos";
    private String seleccionMonto="";
    private String amount="";
    private Button mSend;
    private ProgressBar progreso;
    private OkHttpClient mClient = new OkHttpClient();
    private Context mContext;
    private Spinner mMoneda;
    private TextView areaCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga);
        grupo=(RadioGroup) findViewById(R.id.montos);
        mTo = (EditText) findViewById(R.id.txtNumber);
        areaCode=(TextView) findViewById(R.id.areaCode);
        mMonto1 = (RadioButton) findViewById(R.id.valor1);
        mMonto1.setChecked(true);
        mMonto2 = (RadioButton) findViewById(R.id.valor2);
        mMonto3 = (RadioButton) findViewById(R.id.valor3);
        mMonto4 = (RadioButton) findViewById(R.id.valor4);
        mMonto5 = (RadioButton) findViewById(R.id.valor5);
        mSend = (Button) findViewById(R.id.btnSend);
        mMoneda = (Spinner) findViewById(R.id.moneda);
        progreso=(ProgressBar) findViewById(R.id.progress);
        String[] letra = {"Peso(MX)","Bolivar","Peso(CO)","Peso(CHL)","Dolar(USD)"};
        mMoneda.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, letra));
        mContext = getApplicationContext();
        progreso.setWillNotDraw(true);

        mMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getSelectedItemPosition()){
                    case 1: seleccionMoneda="Bs.";
                        mMonto1.setText("500");
                        mMonto2.setText("1.000");
                        mMonto3.setText("5.000");
                        mMonto4.setText("10.000");
                        mMonto5.setText("20.000");
                        areaCode.setText("+58");
                        break;
                    case 0: seleccionMoneda="Pesos";
                        mMonto1.setText("100");
                        mMonto2.setText("500");
                        mMonto3.setText("1.000");
                        mMonto4.setText("2.000");
                        mMonto5.setText("5.000");
                        areaCode.setText("+52");
                        break;
                    case 2: seleccionMoneda="Pesos";
                        mMonto1.setText("5.000");
                        mMonto2.setText("10.000");
                        mMonto3.setText("50.000");
                        mMonto4.setText("100.000");
                        mMonto5.setText("200.000");
                        areaCode.setText("+57");
                        break;
                    case 3: seleccionMoneda="Pesos";
                        mMonto1.setText("2.000");
                        mMonto2.setText("5.000");
                        mMonto3.setText("10.000");
                        mMonto4.setText("20.000");
                        mMonto5.setText("40.000");
                        areaCode.setText("+56");
                        break;
                    case 4: seleccionMoneda="USD";
                        mMonto1.setText("10");
                        mMonto2.setText("20");
                        mMonto3.setText("50");
                        mMonto4.setText("100");
                        mMonto5.setText("500");
                        areaCode.setText("+1");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progreso.setWillNotDraw(false);
                try {

                    if (mMonto1.isChecked()){
                        seleccionMonto=mMonto1.getText().toString();
                    }
                    else if (mMonto2.isChecked()){
                        seleccionMonto=mMonto2.getText().toString();
                    }
                    else if (mMonto3.isChecked()){
                        seleccionMonto=mMonto3.getText().toString();
                    }
                    else if (mMonto4.isChecked()){
                        seleccionMonto=mMonto4.getText().toString();
                    }
                    else if (mMonto5.isChecked()){
                        seleccionMonto=mMonto5.getText().toString();
                    }
                    try {
                        SmsManager smsman = SmsManager.getDefault();
                        smsman.sendTextMessage(areaCode.getText().toString() + mTo.getText().toString(), null, "Estimado cliente, su recarga por " + seleccionMonto + " " + seleccionMoneda + ", esta siendo procesada, gracias por preferirnos.", null, null);
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
                    amount=seleccionMonto+".00";
                    Intent intent = new Intent(getApplicationContext(), SwingCardActivity.class);
                    intent.putExtra("amount", amount);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }




    public void rbtn1(View rbtn){
        mMonto2.setChecked(false);
        mMonto3.setChecked(false);
        mMonto4.setChecked(false);
        mMonto5.setChecked(false);
    }
    public void rbtn2(View rbtn){
        mMonto1.setChecked(false);
        mMonto3.setChecked(false);
        mMonto4.setChecked(false);
        mMonto5.setChecked(false);
    }
    public void rbtn3(View rbtn){
        mMonto1.setChecked(false);
        mMonto2.setChecked(false);
        mMonto4.setChecked(false);
        mMonto5.setChecked(false);
    }
    public void rbtn4(View rbtn){
        mMonto1.setChecked(false);
        mMonto2.setChecked(false);
        mMonto3.setChecked(false);
        mMonto5.setChecked(false);
    }
    public void rbtn5(View rbtn){
        mMonto1.setChecked(false);
        mMonto2.setChecked(false);
        mMonto3.setChecked(false);
        mMonto4.setChecked(false);
    }
    public void cargarAtras(View view){
        progreso.setWillNotDraw(false);
        mTo.setText("");
        mMonto1.setChecked(true);
        mMoneda.setSelection(0);
        areaCode.setText("+52");
        Intent ListSong = new Intent(getApplicationContext(), EntryMenuActivity.class);
        startActivity(ListSong);
        finish();
    }
    public void salir(View view){
        progreso.setWillNotDraw(false);
        finish();
    }
}