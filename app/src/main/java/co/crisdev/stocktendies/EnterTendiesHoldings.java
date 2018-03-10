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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup radioGroup;


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
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

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
                radioGroup.check(R.id.buyBtn);
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
                    if(tradePriceEt.getText().toString().isEmpty()) {
                        Toast.makeText(EnterTendiesHoldings.this, "Please enter a price for trade price.", Toast.LENGTH_LONG).show();
                        exceptionSpinner.setVisibility(View.GONE);
                        break;
                    } else if(holdingsTv.getText().toString().isEmpty()) {
                        Toast.makeText(EnterTendiesHoldings.this, "Please enter an amount for holdings.", Toast.LENGTH_LONG).show();
                        exceptionSpinner.setVisibility(View.GONE);
                        break;
                    } else if(!holdingsTv.getText().toString().isEmpty() && !tradePriceEt.getText().toString().isEmpty()) {
                        Date now = new Date();
                        exceptionSpinner.setVisibility(View.VISIBLE);
                        Set<String> tendiesList = new LinkedHashSet<>();
                        String count = "";
                        String countStr = "";
                        int nCount = 0;
                        int holdings = 0;
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

                        String buy_or_sell = "buy";
                        holdings = Integer.parseInt(holdingsTv.getText().toString());
                        if(radioGroup.getCheckedRadioButtonId() == R.id.sellBtn) {
                            buy_or_sell = "sell";
                            holdings = Integer.parseInt(holdingsTv.getText().toString()) * -1;
                        }

                        editor.putString(tickerTv.getText().toString() + "_holding_count_" + countStr, Integer.toString(holdings)).apply();
                        editor.putString(tickerTv.getText().toString() + "_trade_price_count_" + countStr, tradePriceEt.getText().toString()).apply();
                        editor.putString(tickerTv.getText().toString() + "_note_count_" + countStr, notes.getText().toString()).apply();
                        editor.putString(tickerTv.getText().toString() + "_date_count_" + countStr, now.toString()).apply();
                        editor.putString(tickerTv.getText().toString() + "_buy_or_sell_count_" + countStr, buy_or_sell).apply();

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        exceptionSpinner.setVisibility(View.GONE);
                        break;
                    }
                } catch (Exception ex) {
                    exceptionSpinner.setVisibility(View.GONE);
                    Toast.makeText(EnterTendiesHoldings.this, "Could not save your precious tendie. Please try again.", Toast.LENGTH_LONG).show();
                }

            default:
                break;
        }

        return true;
    }
}
