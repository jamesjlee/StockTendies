package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends ListActivity {
    private SharedPreferences sharedPreferences;
    public static SwipeRefreshLayout tendiesRefreshLayout;
    private TextView totalDayChange;
    private TextView totalTendiesValue;
    private BigDecimal mainTendiesVal;
    public static final String symbol = "$";
    private String todayDate;
    private String yesterdayDate;
    private BigDecimal totalDayPercentChange;
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100.00);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        tendiesRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshTendies);
        totalDayChange = (TextView) findViewById(R.id.tendiesPercentDayChangeVal);
        totalTendiesValue = (TextView) findViewById(R.id.mainTendiesValue);
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        todayDate = df.format(today);
        cal.add(Calendar.DATE, -1);
        yesterdayDate = df.format(cal.getTime());

        loadSwipeRefreshLayout();

        tendiesRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSwipeRefreshLayout();
                Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout");
            }
        });
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
                                sharedPreferences.edit().clear().commit();
                                updatePercentChangeColor(totalDayPercentChange);
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
        tendiesRefreshLayout.setRefreshing(true);
        Set<String> tendiesSet = new LinkedHashSet<>();
        String count = "";
        String yesterdaysMarketVal = "";
        BigDecimal cumulativeMarketValAtTradePrices = new BigDecimal(0);
        mainTendiesVal = new BigDecimal(0);
        int holdings = 0;
        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);


        ArrayList<Tender> tendiesList = new ArrayList<Tender>();
        tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);
        yesterdaysMarketVal = sharedPreferences.getString(yesterdayDate + "market-val", yesterdaysMarketVal);

        for(String tendie : tendiesSet) {
            try {
                Stock stock = YahooFinance.get(tendie);
                BigDecimal price = stock.getQuote().getPrice();
                BigDecimal change = stock.getQuote().getChangeInPercent();
                count = sharedPreferences.getString(tendie + "_count_", count);

                int countNum = Integer.parseInt(count);
                for(int i=1; i<=countNum; i++) {
                    String holding = "";
                    String tradePrice = "";

                    holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(i), holding);
                    tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(i), tradePrice);

                    int holdingNum = Integer.parseInt(holding);
                    holdings += holdingNum;

                    BigDecimal tradePriceBigDecimal = new BigDecimal(tradePrice);
                    BigDecimal holdingBigDecimal = new BigDecimal(holding);

                    cumulativeMarketValAtTradePrices = cumulativeMarketValAtTradePrices.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                }

                BigDecimal marketValue = price.multiply(new BigDecimal(holdings));
                mainTendiesVal = mainTendiesVal.add(marketValue);

                Tender tender = new Tender(getResources().getDrawable(R.mipmap.ic_launcher, null), tendie.toUpperCase(), holdings, price, change, cumulativeMarketValAtTradePrices, symbol);
                tendiesList.add(tender);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!mainTendiesVal.equals(BigDecimal.ZERO)) {
            if(yesterdaysMarketVal != null && !yesterdaysMarketVal.isEmpty()) {
                totalDayPercentChange = percentChange(mainTendiesVal, new BigDecimal(yesterdaysMarketVal));
            } else {
                totalDayPercentChange = percentChange(mainTendiesVal, cumulativeMarketValAtTradePrices);
            }

            updatePercentChangeColor(totalDayPercentChange);

            sharedPreferences.edit().putString(todayDate + "market-val", totalDayPercentChange.toString());
            sharedPreferences.edit().commit();
            totalDayChange.setText(totalDayPercentChange.toString());
        } else {
            totalDayChange.setText("0.00");
        }

        totalTendiesValue.setText(cumulativeMarketValAtTradePrices.toString());

        Collections.sort(tendiesList, new CustomComparator());
        MyListAdapter listAdapter = new MyListAdapter(this, tendiesList);
        setListAdapter(listAdapter);
        tendiesRefreshLayout.setRefreshing(false);
    }

    public static BigDecimal percentChange(BigDecimal first, BigDecimal second) {
        double firstDouble = first.doubleValue();
        double secondDouble = second.doubleValue();
        double oneHundred = 100.00;

        double res = ((firstDouble-secondDouble) / secondDouble) * oneHundred;
        BigDecimal resBigDecimal = new BigDecimal(res, MathContext.DECIMAL64);
        resBigDecimal = resBigDecimal.setScale(2, RoundingMode.HALF_UP);

        return resBigDecimal;
    }

    public void updatePercentChangeColor(BigDecimal totalDayPercentChange) {
        if(totalDayPercentChange.compareTo(BigDecimal.ZERO) > 0) {
            totalDayChange.setTextColor(ContextCompat.getColor(this, R.color.positive));
        } else if (totalDayPercentChange.compareTo(BigDecimal.ZERO) < 0) {
            totalDayChange.setTextColor(ContextCompat.getColor(this, R.color.negative));
        } else {
            totalDayChange.setTextColor(ContextCompat.getColor(this, R.color.neutral));
        }
    }

    public class CustomComparator implements Comparator<Tender> {
        @Override
        public int compare(Tender tender, Tender t1) {
            return tender.getName().toString().compareTo(t1.getName().toString());
        }
    }
}
