package co.crisdev.stocktendies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lee on 2/7/18.
 */

public class EnterTendiesHoldings extends Activity {
    private TextView tickerTv;
    private TextView currPriceTv;
    private EditText tradePriceEt;
    private TextView holdingsTv;
    private TextView notes;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_tendies_holdings);


        tickerTv = (TextView) findViewById(R.id.ticker);
        currPriceTv = (TextView) findViewById(R.id.currentPrice);
        tradePriceEt = (EditText) findViewById(R.id.tradePrice);
        holdingsTv = (TextView) findViewById(R.id.holdings);
        notes = (TextView) findViewById(R.id.notesInput);

        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();

        if(!bundle.isEmpty()) {
            boolean hasTicker = bundle.containsKey("ticker");
            boolean hasPrice = bundle.containsKey("price");

            if(hasTicker && hasPrice) {
                String ticker = bundle.getString("ticker");
                String currPrice = bundle.getString("price");

                tickerTv.setText(ticker.toUpperCase());
                currPriceTv.setText(currPrice);
                tradePriceEt.setText(currPrice.substring(1, currPrice.length()));
                tradePriceEt.setText(currPrice.substring(1, currPrice.length()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_holdings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                Set<String> holdingsList = new HashSet<>();
                Set<String> tradePriceList = new HashSet<>();
                Set<String> notesList = new HashSet<>();
                SharedPreferences.Editor editor;
                sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();

                if(!sharedPreferences.getStringSet(tickerTv.getText()+"_holdings_list", holdingsList).isEmpty() && !sharedPreferences.getStringSet(tickerTv.getText()+"_trade_price_list", tradePriceList).isEmpty() && !sharedPreferences.getStringSet(tickerTv.getText()+"_notes_list", notesList).isEmpty()) {
                    holdingsList = sharedPreferences.getStringSet(tickerTv.getText()+"_holdings_list", holdingsList);
                    tradePriceList = sharedPreferences.getStringSet(tickerTv.getText()+"_trade_price_list", tradePriceList);
                    notesList = sharedPreferences.getStringSet(tickerTv.getText()+"_notes_list", notesList);
                }
                holdingsList.add(holdingsTv.getText().toString());
                tradePriceList.add(tradePriceEt.getText().toString());
                notesList.add(notes.getText().toString());
                editor.putStringSet(tickerTv.getText()+"_holdings_list", holdingsList);
                editor.putStringSet(tickerTv.getText()+"_trade_price_list", tradePriceList);
                editor.putStringSet(tickerTv.getText()+"_notes_list", tradePriceList);
                editor.commit();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }
}
