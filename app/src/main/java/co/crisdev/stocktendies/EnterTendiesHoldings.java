package co.crisdev.stocktendies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedHashSet;
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
    private ProgressBar exceptionSpinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_tendies_holdings);


        tickerTv = (TextView) findViewById(R.id.ticker);
        currPriceTv = (TextView) findViewById(R.id.currentPrice);
        tradePriceEt = (EditText) findViewById(R.id.tradePrice);
        holdingsTv = (TextView) findViewById(R.id.holdings);
        notes = (TextView) findViewById(R.id.notesInput);
        exceptionSpinner = (ProgressBar) findViewById(R.id.exceptionSpinner);

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
                try {
                    exceptionSpinner.setVisibility(View.VISIBLE);
                    Set<String> tendiesList = new LinkedHashSet<>();
                    String count = "";
                    String countStr = "";
                    int nCount = 0;
                    SharedPreferences.Editor editor;
                    sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();

                    tendiesList = sharedPreferences.getStringSet("tendiesList", tendiesList);

                    if(tendiesList.contains(tickerTv.getText().toString())) {
                        count = sharedPreferences.getString(tickerTv.getText().toString() + "_count_", count);
                        nCount = Integer.parseInt(count) + 1;
                        editor.putString(tickerTv.getText().toString() + "_count_", Integer.toString(nCount)).apply();
                    } else {
                        editor.putString(tickerTv.getText().toString() + "_count_", "1").apply();
                    }

                    tendiesList.add(tickerTv.getText().toString());
                    editor.putStringSet("tendiesList", tendiesList).apply();

                    countStr = sharedPreferences.getString(tickerTv.getText().toString() + "_count_", countStr);

                    editor.putString(tickerTv.getText().toString() + "_holding_count_" + countStr, holdingsTv.getText().toString()).apply();
                    editor.putString(tickerTv.getText().toString() + "_trade_price_count_" + countStr, tradePriceEt.getText().toString()).apply();
                    editor.putString(tickerTv.getText().toString() + "_note_count_" + countStr, notes.getText().toString()).apply();
                    editor.putString(tickerTv.getText().toString() + "_date_count_" + countStr, new Date().toString()).apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    break;
                } catch (Exception ex) {
                    exceptionSpinner.setVisibility(View.GONE);
                    Toast.makeText(EnterTendiesHoldings.this, "Could not find your precious tendie. Please enter a valid ticker that's in the NASDAQ, or try again.", Toast.LENGTH_LONG).show();
                }

            default:
                break;
        }

        return true;
    }
}
