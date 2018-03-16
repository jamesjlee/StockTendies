package co.crisdev.stocktendies;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import static co.crisdev.stocktendies.ViewRow.vrSpinner;

/**
 * Created by lee on 3/16/18.
 */

public class Details extends Activity implements AdapterView.OnItemSelectedListener {
    private String ticker;
    private String price;
    private CandleStickChart candleStickChart;
    private Spinner timeFrame;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    private SharedPreferences sharedPreferences;
    private String savedTimeFrame;
    private TextView detailsTicker;
    private TextView detailsPrice;
    private TextView detailsChange;
    private SharedPreferences.Editor editor;
    private ProgressBar detailsProgressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);


        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        candleStickChart = (CandleStickChart) findViewById(R.id.detailsChart);
        detailsTicker = (TextView) findViewById(R.id.detailsTicker);
        detailsPrice = (TextView) findViewById(R.id.detailsPrice);
        detailsChange = (TextView) findViewById(R.id.detailsChange);
        detailsProgressSpinner = (ProgressBar) findViewById(R.id.detailsProgressSpinner);
        timeFrame = (Spinner) findViewById(R.id.detailsSpinner);

        detailsProgressSpinner.setVisibility(View.VISIBLE);


        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();

        ticker = "";
        price = "";

        if(!bundle.isEmpty()) {
            boolean hasTicker = bundle.containsKey("ticker");
            boolean hasPrice = bundle.containsKey("price");

            if(hasTicker && hasPrice) {
                ticker = bundle.getString("ticker");
                price = bundle.getString("price");

                detailsTicker.setText(ticker);
                detailsPrice.setText(price);
            }
        }

        new init().doInBackground();
    }

    private class loadingCandleChartTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            loadCandleChart((int) objects[0], (int) objects[1], (Interval)objects[2]);
            return null;
        }
    }

    private class init extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println("LEZ GO");

            timeFrame.setOnItemSelectedListener(Details.this);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Details.this.getApplicationContext(),
                    R.array.timeframe_array, R.layout.spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeFrame.setAdapter(adapter);

            savedTimeFrame = "";
            sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            savedTimeFrame = sharedPreferences.getString(ticker + "_timeframe_", savedTimeFrame);

            if(!savedTimeFrame.isEmpty()) {
                int spinnerPosition = adapter.getPosition(savedTimeFrame);
                timeFrame.setSelection(spinnerPosition);
            } else {
                savedTimeFrame = "3D";
                editor.putString(ticker + "_timeframe_", savedTimeFrame).apply();
                int spinnerPosition = adapter.getPosition(savedTimeFrame);
                timeFrame.setSelection(spinnerPosition);
            }
            vrSpinner.setVisibility(View.GONE);
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.detailsHome:
                Intent intent = new Intent(Details.this, MainActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String timeFrame = parent.getItemAtPosition(pos).toString();
        editor.putString(ticker + "_timeframe_", timeFrame).apply();

        switch (savedTimeFrame) {
            case "3D":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -192, Interval.DAILY);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            case "1W":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -12, Interval.DAILY);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            case "1M":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -12);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            case "3M":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -12);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            case "6M":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -12);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            case "1Y":
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -12);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;
            default:
                new loadingCandleChartTask().doInBackground(Calendar.HOUR, -5);
                detailsProgressSpinner.setVisibility(View.GONE);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void loadCandleChart(int calendar, int fromAmnt, Interval interval) {
        try {
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(calendar, fromAmnt); // from 5 years ago

            //set updated price
            Stock stock = YahooFinance.get(ticker);
            BigDecimal price = stock.getQuote().getPrice().setScale(2, RoundingMode.DOWN);
            detailsPrice.setText(String.format("$%,.2f", price));

            List<HistoricalQuote> stockHistQuotes = stock.getHistory(from, to, interval);
            for(HistoricalQuote hq: stockHistQuotes) {
                System.out.println(hq.toString());
            }

            candleStickChart.setBackgroundColor(Color.BLACK);


            candleStickChart.getDescription().setEnabled(false);

            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            candleStickChart.setMaxVisibleValueCount(60);

            // scaling can now only be done on x- and y-axis separately
            candleStickChart.setPinchZoom(false);

            candleStickChart.setDrawGridBackground(false);

            XAxis xAxis = candleStickChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(true);
            xAxis.setDrawLabels(true);
            xAxis.setLabelCount(5, false);

            YAxis leftAxis = candleStickChart.getAxisLeft();
//        leftAxis.setEnabled(false);
            leftAxis.setLabelCount(5, false);
            leftAxis.setDrawGridLines(true);
            leftAxis.setDrawAxisLine(true);
            leftAxis.setDrawLabels(true);

            YAxis rightAxis = candleStickChart.getAxisRight();
            rightAxis.setEnabled(false);
//        rightAxis.setStartAtZero(false);

            candleStickChart.getLegend().setEnabled(false);

            ArrayList<CandleEntry> yVals1 = new ArrayList<CandleEntry>();




        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
