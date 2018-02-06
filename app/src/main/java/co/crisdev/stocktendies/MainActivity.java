package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends ListActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        loadSwipeRefreshLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSwipeRefreshLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(this, AddTendiesToPortfolio.class);
                startActivity(intent);
                break;
            case R.id.reset:
                new AlertDialog.Builder(this)
                        .setTitle("Reset Portfolio")
                        .setMessage("Do you really want to drop all your tendies from your portfolio?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(MainActivity.this, "Dropped all your tendies.", Toast.LENGTH_SHORT).show();
                                sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                                sharedPreferences.edit().remove("tendiesList").commit();
                                loadSwipeRefreshLayout();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.about:

                break;
            default:
                break;
        }

        return true;
    }

    public void loadSwipeRefreshLayout() {
        Set<String> tendiesSet = new HashSet<>();
        ArrayList<Tender> tendiesList = new ArrayList<Tender>();

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
        tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);

        System.out.println(Integer.toString(tendiesSet.size()));

        if(tendiesSet != null) {
            for(String tendie : tendiesSet) {
                try {
                    Stock stock = YahooFinance.get(tendie);
                    BigDecimal price = stock.getQuote().getPrice();
                    BigDecimal change = stock.getQuote().getChangeInPercent();
                    BigDecimal marketValue = new BigDecimal(1);
                    Tender tender = new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), tendie.toUpperCase(), 0, price, change, marketValue);
                    tendiesList.add(tender);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MyListAdapter listAdapter = new MyListAdapter(this, tendiesList);
            setListAdapter(listAdapter);
        }
    }
}
