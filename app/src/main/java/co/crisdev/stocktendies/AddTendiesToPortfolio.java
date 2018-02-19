package co.crisdev.stocktendies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static co.crisdev.stocktendies.MainActivity.symbol;


/**
 * Created by lee on 1/28/18.
 */

public class AddTendiesToPortfolio extends Activity {
    private SearchView searchViewForTendies;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tendies_to_portfolio);

        searchViewForTendies = (SearchView) findViewById(R.id.searchForTendies);

        searchViewForTendies.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                boolean isTender = false;
                Set<String> tendiesList = new HashSet<>();
                try {
                    Stock stock = YahooFinance.get(s);
                    SharedPreferences.Editor editor;
                    sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    tendiesList = sharedPreferences.getStringSet("tendiesList", tendiesList);
                    tendiesList.add(s.toUpperCase());
                    editor.putStringSet("tendiesList", tendiesList);
                    editor.commit();
                    isTender = true;

                    if(stock.getQuote().getPrice() != null) {
                        switchView(s, symbol, stock.getQuote().getPrice());
                    } else {
                        Toast.makeText(AddTendiesToPortfolio.this, "Couldn't find your precious tendie. Please enter a valid ticker that's in the NASDAQ.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(AddTendiesToPortfolio.this, "Could not find your precious tendie. Please enter a valid ticker that's in the NASDAQ.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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
}
