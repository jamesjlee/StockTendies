package co.crisdev.stocktendies;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by lee on 2/18/18.
 */

public class ViewRow extends ListActivity {
    private SharedPreferences sharedPreferences;
    private ArrayList<ViewRowTender> viewRowTendiesList;
    public static TextView vrMarketValue;
    public static TextView vrPandL;
    public static TextView vrNetCost;
    public static TextView vrHoldings;
    public static TextView vrPrice;
    public String ticker = "";
    public String price = "";
    public static ProgressBar vrSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_row);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView vrTendieName = (TextView) findViewById(R.id.vrTendieName);
        vrMarketValue = (TextView) findViewById(R.id.vrMarketValue);
        vrPandL = (TextView) findViewById(R.id.vrPAndL);
        vrNetCost = (TextView) findViewById(R.id.vrNetCost);
        vrHoldings = (TextView) findViewById(R.id.vrHoldings);
        vrPrice = (TextView) findViewById(R.id.vrCurrPrice);
        vrSpinner = (ProgressBar) findViewById(R.id.vrSpinner);


        BigDecimal holding = BigDecimal.ZERO;
        BigDecimal tradePrice = BigDecimal.ZERO;
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


        BigDecimal currPrice = new BigDecimal(price.substring(1, price.length())).setScale(2, BigDecimal.ROUND_CEILING);
        BigDecimal totalHoldings = BigDecimal.ZERO;
        BigDecimal netCost = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;

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

            BigDecimal marketVal = tradePrice.multiply(holding);
            BigDecimal currentMarketVal = currPrice.multiply(holding);
            netCost = netCost.add(marketVal);
            totalMarketValue = totalMarketValue.add(currentMarketVal);

            note = sharedPreferences.getString(ticker + "_note_count_" + Integer.toString(i), note);

            double percentChange = 0.00;
            percentChange = MainActivity.percentChange(marketVal, currentMarketVal);

            buyOrSell = sharedPreferences.getString(ticker + "_buy_or_sell_count_" + Integer.toString(i), buyOrSell);

            viewRowTendiesList.add(new ViewRowTender(ticker, holding.abs().toString(), currPrice.toString(), note, Double.toString(percentChange), buyOrSell));
        }

        BigDecimal pAndL = totalMarketValue.subtract(netCost);


        if(totalHoldings.compareTo(BigDecimal.ZERO) < 0) {
            totalHoldings = BigDecimal.ZERO;
        }

        if(totalMarketValue.compareTo(BigDecimal.ZERO) < 0) {
            totalMarketValue = BigDecimal.ZERO;
        }

        vrTendieName.setText(ticker);
        vrPrice.setText("$"+currPrice.setScale(2, RoundingMode.DOWN).toString());
        vrHoldings.setText(String.format("%,.0f", totalHoldings));
        vrNetCost.setText(String.format("$%,.2f", netCost.setScale(2, RoundingMode.DOWN)));
        vrPandL.setText(String.format("$%,.2f", pAndL.setScale(2, RoundingMode.DOWN)));
        vrMarketValue.setText(String.format("$%,.2f", totalMarketValue.setScale(2, RoundingMode.DOWN)));

        MainActivity.updateChangeTextColorNoSymbol(pAndL, vrPandL);

        ViewRowListAdapter listAdapter = new ViewRowListAdapter(this, viewRowTendiesList);
        setListAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_on_view_row, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.detailsOnViewRow:
                vrSpinner.setVisibility(View.VISIBLE);
                Intent intentBundle2 = new Intent(ViewRow.this, Details.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("ticker", ticker);
                bundle2.putString("price", price.toString());
                bundle2.putString("from", "viewRow");
                intentBundle2.putExtras(bundle2);
                startActivity(intentBundle2);
                break;
            case R.id.addOnViewRow:
                Intent intentBundle = new Intent(ViewRow.this, EnterTendiesHoldings.class);
                Bundle bundle = new Bundle();
                bundle.putString("ticker", ticker);
                bundle.putString("price", price.toString());
                bundle.putString("from", "viewRow");
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
                break;
            default:
                break;
        }
        return true;
    }
}
