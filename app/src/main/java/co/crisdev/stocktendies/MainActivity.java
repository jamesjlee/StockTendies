package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends ListActivity {
    private SharedPreferences sharedPreferences;
    public static SwipeRefreshLayout tendiesRefreshLayout;
    public static TextView totalTendiesChange;
    public static TextView changePercentSymbol;
    public static TextView totalTendiesValue;
    public static BigDecimal cumulativeMarketValAtTradePrices;
    public static BigDecimal cumulativeMarketValAtCurrPrices;
    public static  BigDecimal totalDayPercentChange;
    public static BigDecimal totalTendiesChangeInDollars;
    public static final String symbol = "$";
    public static String todayDate;
    public static String yesterdayDate;
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100.00);
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        tendiesRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshTendies);
        totalTendiesChange = (TextView) findViewById(R.id.tendiesPercentDayChangeVal);
        totalTendiesValue = (TextView) findViewById(R.id.mainTendiesValue);
        MainActivity.context = getApplicationContext();
        totalDayPercentChange = BigDecimal.ZERO;
        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);

        new loadingTask().doInBackground();

        tendiesRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new loadingTask().doInBackground();
                Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout on refresh listener");
            }
        });
    }

    private class loadingTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            loadSwipeRefreshLayout();
            return null;
        }
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new loadingTask().doInBackground();
        Log.i("onResume", "onRefresh called from SwipeRefreshLayout on onResume");
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
                                sharedPreferences.edit().clear().apply();
                                totalTendiesChange.setText("0.00");
                                totalTendiesValue.setText("0.00");
                                updateChangeTextColorNoSymbol(BigDecimal.ZERO, totalTendiesValue);
                                updateChangeTextColorNoSymbol(BigDecimal.ZERO, totalTendiesChange);
                                new loadingTask().doInBackground();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutPage.class);
                startActivity(aboutIntent);
                break;
            default:
                break;
        }

        return true;
    }

    public void loadSwipeRefreshLayout() {
        tendiesRefreshLayout.setRefreshing(true);
        Set<String> tendiesSet = new LinkedHashSet<>();
        String count = "";

        cumulativeMarketValAtTradePrices = BigDecimal.ZERO;
        cumulativeMarketValAtCurrPrices = BigDecimal.ZERO;
        totalTendiesChangeInDollars = BigDecimal.ZERO;
        totalDayPercentChange = BigDecimal.ZERO;

        ArrayList<Tender> tendiesList = new ArrayList<Tender>();
        tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);

        for(String tendie : tendiesSet) {
            try {
                Stock stock = YahooFinance.get(tendie);
                BigDecimal price = stock.getQuote().getPrice();
                BigDecimal change = stock.getQuote().getChangeInPercent();
                BigDecimal changeInDollars = stock.getQuote().getChange();
                BigDecimal holdings = BigDecimal.ZERO;
                BigDecimal marketVal = BigDecimal.ZERO;
                count = sharedPreferences.getString(tendie + "_count_", count);

                int countNum = Integer.parseInt(count);
                for(int i=1; i<=countNum; i++) {
                    String holding = "";
                    String tradePrice = "";
                    String buyOrSell = "";
                    BigDecimal holdingNum = BigDecimal.ZERO;

                    holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(i), holding);
                    tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(i), tradePrice);
                    buyOrSell = sharedPreferences.getString(tendie + "_buy_or_sell_count_" + Integer.toString(i), buyOrSell);

                    holdingNum = new BigDecimal(holding);

                    BigDecimal tradePriceBigDecimal = new BigDecimal(tradePrice);
                    BigDecimal holdingBigDecimal = new BigDecimal(holding);

                    holdings = holdings.add(holdingNum);
                    cumulativeMarketValAtTradePrices = cumulativeMarketValAtTradePrices.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                    cumulativeMarketValAtCurrPrices = cumulativeMarketValAtCurrPrices.add(price.multiply(holdingBigDecimal));
                    marketVal = marketVal.add(price.multiply(holdingBigDecimal));
                }

                Tender tender = new Tender(getResources().getDrawable(R.mipmap.ic_launcher, null), tendie.toUpperCase(), holdings, price.setScale(2, RoundingMode.HALF_UP), change, marketVal.setScale(2, RoundingMode.HALF_UP), symbol, changeInDollars);
                tendiesList.add(tender);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if((cumulativeMarketValAtTradePrices.compareTo(BigDecimal.ZERO) > 0) || (cumulativeMarketValAtTradePrices.compareTo(BigDecimal.ZERO) < 0)) {
            totalDayPercentChange = percentChange(cumulativeMarketValAtCurrPrices, cumulativeMarketValAtTradePrices);
            totalTendiesChange.setText(totalDayPercentChange.setScale(2, RoundingMode.HALF_UP).toString()+"%");
            updateChangeTextColorNoSymbol(totalDayPercentChange, totalTendiesChange);
            totalTendiesValue.setText(String.format("$%,.2f", cumulativeMarketValAtCurrPrices.subtract(cumulativeMarketValAtTradePrices).add(cumulativeMarketValAtCurrPrices).setScale(2, RoundingMode.HALF_UP)));
        } else if(cumulativeMarketValAtTradePrices.compareTo(BigDecimal.ZERO) == 0) {
            totalTendiesChange.setText("0.00%");
            totalTendiesValue.setText("$0.00");
            updateChangeTextColorNoSymbol(BigDecimal.ZERO, totalTendiesChange);
        }

        Collections.sort(tendiesList, new CustomComparator());
        MyListAdapter listAdapter = new MyListAdapter(this, tendiesList);
        setListAdapter(listAdapter);
        tendiesRefreshLayout.setRefreshing(false);
    }

    public static BigDecimal percentChange(BigDecimal first, BigDecimal second) {
        double firstDouble = first.doubleValue();
        double secondDouble = second.doubleValue();
        double oneHundred = 100.00;

        double res = ((firstDouble-secondDouble)/ secondDouble) * oneHundred;
        BigDecimal resBigDecimal = new BigDecimal(res, MathContext.DECIMAL64);
        resBigDecimal = resBigDecimal.setScale(2, RoundingMode.HALF_UP);

        return resBigDecimal;
    }

    public static void updateChangeTextColor(BigDecimal percentChange, TextView tv, TextView tv2) {
        if(percentChange.compareTo(BigDecimal.ZERO) > 0) {
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.positive));
            tv2.setTextColor(ContextCompat.getColor(getAppContext(), R.color.positive));
        } else if (percentChange.compareTo(BigDecimal.ZERO) < 0) {
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.negative));
            tv2.setTextColor(ContextCompat.getColor(getAppContext(), R.color.negative));
        } else if (percentChange.compareTo(BigDecimal.ZERO) == 0){
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.neutral));
            tv2.setTextColor(ContextCompat.getColor(getAppContext(), R.color.neutral));
        }
    }

    public static void updateChangeTextColorNoSymbol(BigDecimal percentChange, TextView tv) {
        if(percentChange.compareTo(BigDecimal.ZERO) > 0) {
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.positive));
        } else if (percentChange.compareTo(BigDecimal.ZERO) < 0) {
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.negative));
        } else if (percentChange.compareTo(BigDecimal.ZERO) == 0){
            tv.setTextColor(ContextCompat.getColor(getAppContext(), R.color.neutral));
        }
    }

    public class CustomComparator implements Comparator<Tender> {
        @Override
        public int compare(Tender t1, Tender t2) {
            return t1.getName().toString().compareTo(t2.getName().toString());
        }
    }
}
