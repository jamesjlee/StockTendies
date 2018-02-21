package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by lee on 1/28/18.
 */

public class MyListAdapter extends ArrayAdapter<Tender>  {
    private final Context context;
    private final ArrayList<Tender> tendersArrayList;
    private SharedPreferences sharedPreferences;

    public MyListAdapter(Context context, ArrayList<Tender>tendersArrayList) {
        super(context, R.layout.tendies_row, tendersArrayList);
        this.context = context;
        this.tendersArrayList = tendersArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.tendies_row, parent, false);

        ImageView tenderDrawable = (ImageView) rowView.findViewById(R.id.rowTenderImage);
        TextView tenderName = (TextView) rowView.findViewById(R.id.rowTenderName);
        TextView tenderMarketValue = (TextView) rowView.findViewById(R.id.rowTenderMarketValue);
        TextView tenderPrice = (TextView) rowView.findViewById(R.id.rowTenderPrice);
        TextView tenderDayChange = (TextView) rowView.findViewById(R.id.rowTenderDayChange);
        TextView tenderHoldings = (TextView) rowView.findViewById(R.id.rowTenderHoldings);
        ImageView tenderDrop = (ImageView) rowView.findViewById(R.id.rowTenderDrop);

        // add color to percent change
        if(tendersArrayList.get(position).getDayChangePercent().compareTo(BigDecimal.ZERO) > 0) {
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.positive));
        } else if (tendersArrayList.get(position).getDayChangePercent().compareTo(BigDecimal.ZERO) < 0){
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.negative));
        } else {
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.neutral));
        }

        tenderDrawable.setImageDrawable(tendersArrayList.get(position).getTenderImage());
        tenderName.setText(tendersArrayList.get(position).getName());
        tenderHoldings.setText(Integer.toString(tendersArrayList.get(position).getHoldings()));
        tenderPrice.setText(tendersArrayList.get(position).getSymbol()+tendersArrayList.get(position).getPrice().toString());
        tenderMarketValue.setText(tendersArrayList.get(position).getSymbol()+tendersArrayList.get(position).getMarketValue().toString());
        tenderDayChange.setText(tendersArrayList.get(position).getDayChangePercent().toString());


        SharedPreferences.Editor editor;
        sharedPreferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchView(tenderName.getText().toString(), tenderPrice.getText().toString(), view);
            }
        });

        tenderDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove from Portfolio")
                        .setMessage("Do you really remove " +  tenderName.getText() + " from your portfolio?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getContext(), "Removed Tendie: " + tenderName.getText() + " from your portfolio!", Toast.LENGTH_SHORT).show();
                                Set<String> tendiesSet = new HashSet<>();

                                SharedPreferences.Editor editor;
                                sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                                editor = sharedPreferences.edit();
                                tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);

                                String count = "";
                                String yesterdaysMarketVal = "";
                                yesterdaysMarketVal = sharedPreferences.getString(MainActivity.yesterdayDate + "market-val", yesterdaysMarketVal);
                                MainActivity.mainTendiesVal = new BigDecimal(0.00);

                                BigDecimal totalDayPercentChange = new BigDecimal(0);
                                for(String tendie : tendiesSet) {
                                    try {
                                        Stock stock = YahooFinance.get(tendie);
                                        BigDecimal price = stock.getQuote().getPrice();
                                        BigDecimal change = stock.getQuote().getChangeInPercent();
                                        int holdings = 0;
                                        BigDecimal cumulativeForCurrentStock = new BigDecimal(0.00);
                                        count = sharedPreferences.getString(tendie + "_count_", count);

                                        int countNumber = Integer.parseInt(count);
                                        for(int i=1; i<=countNumber; i++) {
                                            String holding = "";
                                            String tradePrice = "";
                                            BigDecimal tradePriceBigDecimal = new BigDecimal(0.00);
                                            BigDecimal holdingBigDecimal = new BigDecimal(0.00);
                                            if(tendie.equals(tenderName.getText())) {
                                                editor.remove(tenderName.getText() + "_holding_count_" + Integer.toString(i));
                                                editor.remove(tenderName.getText() + "_trade_price_count_" + Integer.toString(i));
                                                editor.remove(tenderName.getText() + "_note_count_" + Integer.toString(i));
                                                editor.remove(tenderName.getText() + "_count_" + Integer.toString(i));
                                                editor.apply();
                                            } else {
                                                holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(i), holding);
                                                tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(i), tradePrice);

                                                int holdingNum = Integer.parseInt(holding);
                                                holdings += holdingNum;

                                                tradePriceBigDecimal = new BigDecimal(tradePrice);
                                                holdingBigDecimal = new BigDecimal(holding);
                                            }
                                            MainActivity.cumulativeMarketValAtTradePrices = MainActivity.cumulativeMarketValAtTradePrices.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                                            cumulativeForCurrentStock = cumulativeForCurrentStock.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                                        }

                                        MainActivity.mainTendiesVal = MainActivity.mainTendiesVal.add(cumulativeForCurrentStock);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if(MainActivity.mainTendiesVal.toString().equals("0.00")) {
                                    MainActivity.mainTendiesVal = new BigDecimal(0).setScale(0);
                                    MainActivity.totalTendiesValue.setText("0");
                                } else {
                                    MainActivity.totalTendiesValue.setText(MainActivity.mainTendiesVal.toString());
                                }

                                if(!MainActivity.mainTendiesVal.equals(BigDecimal.ZERO.setScale(2))) {
                                    if(yesterdaysMarketVal != null && !yesterdaysMarketVal.isEmpty()) {
                                        totalDayPercentChange = MainActivity.percentChange(MainActivity.mainTendiesVal, new BigDecimal(yesterdaysMarketVal));
                                    } else {
                                        totalDayPercentChange = MainActivity.percentChange(MainActivity.mainTendiesVal, MainActivity.cumulativeMarketValAtTradePrices.subtract(MainActivity.mainTendiesVal));
                                    }

                                    updatePercentChangeColor(totalDayPercentChange);
                                    editor.putString(MainActivity.todayDate + "_market_val_", MainActivity.cumulativeMarketValAtTradePrices.toString());
                                    editor.apply();
                                    MainActivity.totalDayChange.setText(totalDayPercentChange.toString());
                                } else {
                                    updatePercentChangeColor(new BigDecimal(0.00));
                                    MainActivity.totalDayChange.setText("0.00");
                                }

                                tendiesSet.remove(tenderName.getText());
                                editor.putStringSet("tendiesList", tendiesSet).apply();

                                tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);

                                //remove item from list view
                                tendersArrayList.remove(position);
                                MyListAdapter.this.notifyDataSetChanged();
                                Log.i("DELETE", "delete called from clicking trash can");;
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return rowView;
    }


    public void switchView(String ticker, String price, View view) {
        Intent intentBundle = new Intent(view.getContext(), ViewRow.class);
        Bundle bundle = new Bundle();
        bundle.putString("ticker", ticker);
        bundle.putString("price", price);
        intentBundle.putExtras(bundle);
        view.getContext().startActivity(intentBundle);
    }

    public void updatePercentChangeColor(BigDecimal percentChange) {
        if(percentChange.compareTo(BigDecimal.ZERO) > 0) {
            MainActivity.totalDayChange.setTextColor(ContextCompat.getColor(getContext(), R.color.positive));
        } else if (percentChange.compareTo(BigDecimal.ZERO) < 0) {
            MainActivity.totalDayChange.setTextColor(ContextCompat.getColor(getContext(), R.color.negative));
        } else if (percentChange.equals(BigDecimal.ZERO)){
            MainActivity.totalDayChange.setTextColor(ContextCompat.getColor(getContext(), R.color.neutral));
        }
    }
}
