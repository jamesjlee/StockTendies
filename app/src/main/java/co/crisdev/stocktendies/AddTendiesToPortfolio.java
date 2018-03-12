package co.crisdev.stocktendies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static co.crisdev.stocktendies.MainActivity.symbol;


/**
 * Created by lee on 1/28/18.
 */

public class AddTendiesToPortfolio extends Activity {
    private SearchView searchViewForTendies;
    private ProgressBar spinner;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tendies_to_portfolio);

        searchViewForTendies = (SearchView) findViewById(R.id.searchForTendies);
        spinner = (ProgressBar) findViewById(R.id.addToPortfolioSpinner);


        searchViewForTendies.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                boolean isTender = false;
                spinner.setVisibility(View.VISIBLE);
                Stock stock = new loadingTask().doInBackground(s);
                if(stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                    isTender = true;
                    spinner.setVisibility(View.GONE);
                    switchView(s, symbol, stock.getQuote().getPrice());
                } else {
                    Toast.makeText(AddTendiesToPortfolio.this, "Couldn't find your precious tendie. Please enter a valid ticker that's in the NASDAQ, or try again.", Toast.LENGTH_SHORT).show();
                    spinner.setVisibility(View.GONE);
                }
                return isTender;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    public void switchView(String s, String symbol, BigDecimal price) {
        Intent intentBundle = new Intent(AddTendiesToPortfolio.this, EnterTendiesHoldings.class);
        Bundle bundle = new Bundle();
        bundle.putString("ticker", s);
        bundle.putString("price", symbol+price.toString());
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

    private class loadingTask extends AsyncTask<String, String, Stock> {
        @Override
        protected Stock doInBackground(String... strings) {
            Stock stock = queryStock(strings[0]);
            return stock;
        }
    }

    public Stock queryStock(String s) {
        Stock stock = null;
        try {
            stock = YahooFinance.get(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stock;
    }
}
