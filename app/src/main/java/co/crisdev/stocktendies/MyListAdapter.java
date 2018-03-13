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

import org.w3c.dom.Text;

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
        TextView tenderPercentSymbol = (TextView) rowView.findViewById(R.id.rowTenderPercent);

        // add color to percent change
        MainActivity.updateChangeTextColor(tendersArrayList.get(position).getDayChangePercent(), tenderDayChange, tenderPercentSymbol);

        if(tendersArrayList.get(position).getDayChangePercent().compareTo(BigDecimal.ZERO) > 0) {
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.positive));
        } else if (tendersArrayList.get(position).getDayChangePercent().compareTo(BigDecimal.ZERO) < 0){
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.negative));
        } else {
            tenderDayChange.setTextColor(ContextCompat.getColor(context, R.color.neutral));
        }


        SharedPreferences.Editor editor;
        sharedPreferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        tenderDrawable.setImageDrawable(tendersArrayList.get(position).getTenderImage());
        tenderName.setText(tendersArrayList.get(position).getName());
        tenderHoldings.setText(String.format("%,.0f", tendersArrayList.get(position).getHoldings()));
        tenderPrice.setText(String.format("$%,.2f", tendersArrayList.get(position).getPrice()));
        tenderMarketValue.setText(String.format("$%,.2f", tendersArrayList.get(position).getMarketValue()));
        tenderDayChange.setText(String.format("%,.2f", tendersArrayList.get(position).getDayChangePercent()));

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchView(tenderName.getText().toString(), tenderPrice.getText().toString(), tendersArrayList.get(position).getChangeInDollars().toString(), view);
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
                        MainActivity.cumulativeMarketValAtTradePrices = BigDecimal.ZERO;
                        MainActivity.cumulativeMarketValAtCurrPrices = BigDecimal.ZERO;


                        for(String tendie : tendiesSet) {
                            try {
                                Stock stock = YahooFinance.get(tendie);
                                BigDecimal price = stock.getQuote().getPrice();
                                BigDecimal change = stock.getQuote().getChangeInPercent();
                                BigDecimal changeInDollars = stock.getQuote().getChange();
                                int holdings = 0;
                                BigDecimal marketVal = BigDecimal.ZERO;
                                count = sharedPreferences.getString(tendie + "_count_", count);

                                int countNum = Integer.parseInt(count);
                                for(int i=1; i<=countNum; i++) {
                                    if(tendie.equals(tenderName.getText())) {
                                        editor.remove(tendie + "_holding_count_" + Integer.toString(i)).apply();
                                        editor.remove(tendie + "_trade_price_count_" + Integer.toString(i)).apply();
                                        editor.remove(tendie + "_note_count_" + Integer.toString(i)).apply();
                                        editor.remove(tendie + "_date_count_" + Integer.toString(i)).apply();
                                        editor.remove(tendie + "_buy_or_sell_count_" + Integer.toString(i)).apply();
                                    }
                                    String holding = "";
                                    String tradePrice = "";
                                    int holdingNum = 0;

                                    holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(i), holding);
                                    tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(i), tradePrice);

                                    if(holding.isEmpty() && tradePrice.isEmpty()) {
                                        holding = "0";
                                        tradePrice = "0.00";
                                    }
                                    holdingNum = Integer.parseInt(holding);
                                    holdings += holdingNum;

                                    BigDecimal tradePriceBigDecimal = new BigDecimal(tradePrice);
                                    BigDecimal holdingBigDecimal = new BigDecimal(holding);

                                    MainActivity.cumulativeMarketValAtTradePrices = MainActivity.cumulativeMarketValAtTradePrices.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                                    MainActivity.cumulativeMarketValAtCurrPrices = MainActivity.cumulativeMarketValAtCurrPrices.add(price.multiply(holdingBigDecimal));

                                    MainActivity.totalTendiesChangeInDollars = MainActivity.totalTendiesChangeInDollars.add(holdingBigDecimal.multiply(changeInDollars)).abs();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if(!(MainActivity.cumulativeMarketValAtCurrPrices.compareTo(BigDecimal.ZERO) == 0)) {
                            MainActivity.totalDayPercentChange = MainActivity.percentChange(MainActivity.cumulativeMarketValAtCurrPrices, MainActivity.cumulativeMarketValAtTradePrices);
                            MainActivity.totalTendiesChange.setText(MainActivity.totalDayPercentChange.setScale(2, RoundingMode.HALF_UP).toString());
                            MainActivity.totalTendiesValue.setText(String.format("$%,.2f", MainActivity.cumulativeMarketValAtCurrPrices.setScale(2, RoundingMode.HALF_UP)));
                            MainActivity.updateChangeTextColorNoSymbol(MainActivity.totalDayPercentChange, MainActivity.totalTendiesChange);
                        } else {
                            MainActivity.totalTendiesChange.setText("0.00%");
                            MainActivity.totalTendiesValue.setText("$0.00");
                            MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, MainActivity.totalTendiesChange);
                        }


                        tendiesSet.remove(tenderName.getText());
                        editor.putStringSet("tendiesList", tendiesSet).apply();

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


    public void switchView(String ticker, String price, String changeInDollars, View view) {
        Intent intentBundle = new Intent(view.getContext(), ViewRow.class);
        Bundle bundle = new Bundle();
        bundle.putString("ticker", ticker);
        bundle.putString("price", price);
        intentBundle.putExtras(bundle);
        view.getContext().startActivity(intentBundle);
    }

    public void updatePercentChangeColor(BigDecimal percentChange) {
        if(percentChange.compareTo(BigDecimal.ZERO) > 0) {
            MainActivity.totalTendiesChange.setTextColor(ContextCompat.getColor(getContext(), R.color.positive));
        } else if (percentChange.compareTo(BigDecimal.ZERO) < 0) {
            MainActivity.totalTendiesChange.setTextColor(ContextCompat.getColor(getContext(), R.color.negative));
        } else if (percentChange.compareTo(BigDecimal.ZERO) == 0){
            MainActivity.totalTendiesChange.setTextColor(ContextCompat.getColor(getContext(), R.color.neutral));
        }
    }
}
