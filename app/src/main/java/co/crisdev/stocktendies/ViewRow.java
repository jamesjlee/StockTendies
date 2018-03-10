package co.crisdev.stocktendies;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

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

        TextView vrTendieName = (TextView) findViewById(R.id.vrTendieName);
        TextView vrMarketValue = (TextView) findViewById(R.id.vrMarketValue);
        TextView vrPandL = (TextView) findViewById(R.id.vrPAndL);
        TextView vrNetCost = (TextView) findViewById(R.id.vrNetCost);
        TextView vrHoldings = (TextView) findViewById(R.id.vrHoldings);

        String ticker = "";
        String price = "";

        BigDecimal holding = new BigDecimal(0);
        BigDecimal tradePrice = new BigDecimal(0);
        String note = "";
        String count = "";
        String buyOrSell = "";

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
        BigDecimal totalHoldings = new BigDecimal(0);
        BigDecimal netCost = new BigDecimal(0);
        BigDecimal totalMarketValue = new BigDecimal(0);

        count = sharedPreferences.getString(ticker + "_count_", count);

        int countNum = Integer.parseInt(count);
        for(int i=1; i<=countNum; i++) {
            String holdingStr = "";
            holdingStr = sharedPreferences.getString(ticker + "_holding_count_" + Integer.toString(i), holdingStr);
            holding = new BigDecimal(holdingStr);

            totalHoldings = totalHoldings.add(holding);


            String tradePriceStr = "";
            tradePriceStr = sharedPreferences.getString(ticker + "_trade_price_count_" + Integer.toString(i), tradePriceStr);
            tradePrice = new BigDecimal(tradePriceStr);

            BigDecimal marketVal = holding.multiply(tradePrice);
            BigDecimal currentMarketVal = currPrice.multiply(holding);
            netCost = netCost.add(marketVal);
            totalMarketValue = totalMarketValue.add(currentMarketVal);

            note = sharedPreferences.getString(ticker + "_note_count_" + Integer.toString(i), note);

            BigDecimal percentChange = new BigDecimal(0.00);
            if((!(marketVal.compareTo(BigDecimal.ZERO) == 0) && (!(currentMarketVal.compareTo(BigDecimal.ZERO) == 0)))) {
                percentChange = MainActivity.percentChange(marketVal, currentMarketVal);
            }

            buyOrSell = sharedPreferences.getString(ticker + "_buy_or_sell_count_" + Integer.toString(i), buyOrSell);

            viewRowTendiesList.add(new ViewRowTender(ticker, holding.abs().toString(), currPrice.toString(), note, percentChange.toString(), buyOrSell));
        }

        BigDecimal pAndL = totalMarketValue.subtract(netCost);


        if(totalHoldings.compareTo(BigDecimal.ZERO) < 0) {
            totalHoldings = BigDecimal.ZERO;
        }

        if(totalMarketValue.compareTo(BigDecimal.ZERO) < 0) {
            totalMarketValue = BigDecimal.ZERO;
        }

        if(netCost.compareTo(BigDecimal.ZERO) < 0) {
            netCost = BigDecimal.ZERO;
        }

        vrTendieName.setText(ticker);
        vrHoldings.setText(totalHoldings.toString());
        vrNetCost.setText(getApplicationContext().getApplicationContext().getResources().getString(R.string.dollarSign)+netCost.abs().toString());
        vrPandL.setText(getApplicationContext().getApplicationContext().getResources().getString(R.string.dollarSign)+pAndL.toString());
        vrMarketValue.setText(getApplicationContext().getApplicationContext().getResources().getString(R.string.dollarSign)+totalMarketValue.toString());

        MainActivity.updateChangeTextColor(pAndL, vrPandL);

        ViewRowListAdapter listAdapter = new ViewRowListAdapter(this, viewRowTendiesList);
        setListAdapter(listAdapter);
    }
}
