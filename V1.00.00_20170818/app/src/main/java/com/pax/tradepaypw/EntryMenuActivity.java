package com.pax.tradepaypw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.pax.jemv.demo.R;

public class EntryMenuActivity extends AppCompatActivity {

    private ProgressBar progreso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_menu);
        progreso=(ProgressBar) findViewById(R.id.progress);
        progreso.setWillNotDraw(true);

    }
    public void cargarPuntoVenta(View view) {

        progreso.setWillNotDraw(false);
        Intent ListSong = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(ListSong);
        finish();
    }
    public void cargarRecarga(View view) {
        progreso.setWillNotDraw(false);
        Intent ListSong = new Intent(getApplicationContext(), RecargaActivity.class);
        startActivity(ListSong);
        finish();
    }
    public void salir(View view){
        progreso.setWillNotDraw(false);
        finish();
    }
}
