package co.crisdev.stocktendies;

import android.app.Activity;
import android.content.Context;
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
                    tendiesList.add(s);
                    SharedPreferences.Editor editor;
                    sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putStringSet("tendiesList", tendiesList);
                    editor.commit();
                    isTender = true;
                } catch (IOException e) {
                    Toast.makeText(AddTendiesToPortfolio.this, "Could not find your precious tendie. Please find a valid ticker.", Toast.LENGTH_LONG).show();
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
}
