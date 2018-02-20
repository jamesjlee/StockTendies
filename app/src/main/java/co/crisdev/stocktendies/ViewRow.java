package co.crisdev.stocktendies;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by lee on 2/18/18.
 */

public class ViewRow extends ListActivity {
    private SharedPreferences sharedPreferences;
    private ArrayList<ViewRowTender> viewRowTendiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_row);
        String ticker = "";
        String price = "";

        BigDecimal holding = new BigDecimal(0);
        BigDecimal tradePrice = new BigDecimal(0);
        String note = "";
        String count = "";

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
        viewRowTendiesList = new ArrayList<ViewRowTender>();


        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();

        if(!bundle.isEmpty()) {
            boolean hasTicker = bundle.containsKey("ticker");
            boolean hasPrice = bundle.containsKey("price");

            if(hasTicker && hasPrice) {
                ticker = bundle.getString("ticker");
                price = bundle.getString("price");
            }
        }


        BigDecimal currPrice = new BigDecimal(price.substring(1, price.length()));

        count = sharedPreferences.getString(ticker + "_count_", count);

        int countNum = Integer.parseInt(count);
        for(int i=1; i<=countNum; i++) {
            String holdingStr = "";
            holdingStr = sharedPreferences.getString(ticker + "_holding_count_" + Integer.toString(i), holdingStr);
            holding = new BigDecimal(holdingStr);

            String tradePriceStr = "";
            tradePriceStr = sharedPreferences.getString(ticker + "_trade_price_count_" + Integer.toString(i), tradePriceStr);
            tradePrice = new BigDecimal(tradePriceStr);

            BigDecimal marketVal = holding.multiply(tradePrice);
            BigDecimal currentMarketVal = currPrice.multiply(holding);

            note = sharedPreferences.getString(ticker + "_note_count_" + Integer.toString(i), note);

            BigDecimal percentChange = MainActivity.percentChange(marketVal, currentMarketVal);

            viewRowTendiesList.add(new ViewRowTender(ticker, holding.toString(), tradePrice.toString(), note, percentChange.toString()));
        }

        ViewRowListAdapter listAdapter = new ViewRowListAdapter(this, viewRowTendiesList);
        setListAdapter(listAdapter);
    }
}
