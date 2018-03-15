package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by lee on 2/19/18.
 */

public class ViewRowListAdapter extends ArrayAdapter<ViewRowTender> {
    private final Context context;
    private final ArrayList<ViewRowTender> viewRowTendiesArrayList;
    private SharedPreferences sharedPreferences;

    public ViewRowListAdapter(Context context, ArrayList<ViewRowTender> viewRowTendiesArrayList) {
        super(context, R.layout.view_row_tendies, viewRowTendiesArrayList);
        this.context = context;
        this.viewRowTendiesArrayList = viewRowTendiesArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.view_row_tendies, parent, false);

        ViewGroup.LayoutParams params = rowView.getLayoutParams();
        params.height = 500;
        rowView.setLayoutParams(params);

        TextView viewRowHolding = (TextView) rowView.findViewById(R.id.viewRowHolding);
        TextView viewRowPrice = (TextView) rowView.findViewById(R.id.viewRowPrice);
        TextView viewRowNote = (TextView) rowView.findViewById(R.id.viewRowNote);
        TextView viewRowChange = (TextView) rowView.findViewById(R.id.viewRowChange);
        TextView viewRowDate = (TextView) rowView.findViewById(R.id.viewRowDate);
        TextView viewRowCost = (TextView) rowView.findViewById(R.id.viewRowCost);
        TextView viewRowHoldingLbl = (TextView) rowView.findViewById(R.id.viewRowHoldingLabel);
        TextView viewRowCostLbl = (TextView) rowView.findViewById(R.id.viewRowCostLabel);
        TextView viewRowChangeLbl = (TextView) rowView.findViewById(R.id.viewRowChangeLabel);
        ImageView viewRowDelete = (ImageView) rowView.findViewById(R.id.viewRowDelete);

        sharedPreferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);

        String tradePrice = "";
        String date = "";
        String buyOrSell = "";
        int posPlusOne = position + 1;

        tradePrice = sharedPreferences.getString(viewRowTendiesArrayList.get(position).getTicker() + "_trade_price_count_" + Integer.toString(posPlusOne), tradePrice);
        date = sharedPreferences.getString(viewRowTendiesArrayList.get(position).getTicker() + "_date_count_" + Integer.toString(posPlusOne), date);
        buyOrSell = sharedPreferences.getString(viewRowTendiesArrayList.get(position).getTicker() + "_buy_or_sell_count_" + Integer.toString(posPlusOne), buyOrSell);

        BigDecimal holdings = new BigDecimal(viewRowTendiesArrayList.get(position).getHoldings());
        BigDecimal cost = new BigDecimal(tradePrice).multiply(holdings);
        Double change = Double.parseDouble(viewRowTendiesArrayList.get(position).getChange());

        if (buyOrSell.equals("sell")) {
            viewRowCostLbl.setText("Proceeds:");
            viewRowChange.setVisibility(View.GONE);
            viewRowChangeLbl.setVisibility(View.GONE);
        } else if (buyOrSell.equals("buy")) {
            if(Double.isInfinite(change) || Double.isNaN(change)) {
                viewRowChange.setText(change.toString()+"%");
                MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, viewRowChange);
            } else {
                viewRowChange.setText(String.format("%,.2f%%", new BigDecimal(viewRowTendiesArrayList.get(position).getChange())));
                MainActivity.updateChangeTextColorNoSymbol(new BigDecimal(change.toString()), viewRowChange);
            }
        }
        viewRowHoldingLbl.setText(buyOrSell.toUpperCase() + ":");
        viewRowHolding.setText(String.format("%,.0f", new BigDecimal(viewRowTendiesArrayList.get(position).getHoldings())));
        viewRowPrice.setText(String.format("$%,.2f", new BigDecimal(tradePrice).setScale(2, RoundingMode.DOWN)));
        viewRowNote.setText(viewRowTendiesArrayList.get(position).getNotes());
        viewRowDate.setText(date);
        viewRowCost.setText(String.format("$%,.2f", cost.setScale(2, RoundingMode.DOWN)));

        viewRowDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove from Portfolio")
                        .setMessage("Do you really remove the " + viewRowTendiesArrayList.get(position).getTicker() + " transaction from your portfolio?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getContext(), "Removed " + viewRowTendiesArrayList.get(position).getTicker() + " transaction from your portfolio!", Toast.LENGTH_SHORT).show();
                                Set<String> tendiesSet = new HashSet<>();

                                SharedPreferences.Editor editor;
                                editor = sharedPreferences.edit();
                                tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);

                                String count = "";
                                MainActivity.cumulativeMarketValAtTradePrices = BigDecimal.ZERO;
                                MainActivity.cumulativeMarketValAtCurrPrices = BigDecimal.ZERO;
                                MainActivity.totalTendiesChangeInDollars = BigDecimal.ZERO;

                                BigDecimal netCost = BigDecimal.ZERO;
                                BigDecimal totalMarketValue = BigDecimal.ZERO;
                                BigDecimal totalHoldings = BigDecimal.ZERO;
                                int positionPlusOne = position + 1;

                                for (String tendie : tendiesSet) {
                                    try {
                                        Stock stock = YahooFinance.get(tendie);
                                        BigDecimal price = stock.getQuote().getPrice();
                                        BigDecimal changeInDollars = stock.getQuote().getChange();
                                        int holdings = 0;
                                        BigDecimal marketVal = BigDecimal.ZERO;
                                        count = sharedPreferences.getString(viewRowTendiesArrayList.get(position).getTicker() + "_count_", count);

                                        int countNum = Integer.parseInt(count);

                                        for (int i = 1; i <= countNum; i++) {
                                            if(positionPlusOne == i && viewRowTendiesArrayList.get(position).getTicker().equals(tendie)) {
                                                editor.remove(tendie + "_holding_count_" + Integer.toString(i)).apply();
                                                editor.remove(tendie + "_trade_price_count_" + Integer.toString(i)).apply();
                                                editor.remove(tendie + "_note_count_" + Integer.toString(i)).apply();
                                                editor.remove(tendie + "_date_count_" + Integer.toString(i)).apply();
                                                editor.remove(tendie + "_buy_or_sell_count_" + Integer.toString(i)).apply();
                                                int nCount = countNum - 1;
                                                editor.putString(tendie + "_count_", Integer.toString(nCount)).apply();

                                                if(positionPlusOne != countNum) {
                                                    for(int k=positionPlusOne; k<countNum;k++) {
                                                        String holding = "";
                                                        String tradePrice = "";
                                                        String note = "";
                                                        String date = "";
                                                        String buyOrSell = "";
                                                        holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(k+1), holding);
                                                        tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(k+1), holding);
                                                        note = sharedPreferences.getString(tendie + "_note_count_" + Integer.toString(k+1), holding);
                                                        date = sharedPreferences.getString(tendie + "_date_count_" + Integer.toString(k+1), holding);
                                                        buyOrSell = sharedPreferences.getString(tendie + "_buy_or_sell_count_" + Integer.toString(k+1), holding);

                                                        //remove trailing tendie
                                                        editor.remove(tendie + "_holding_count_" + Integer.toString(k)).apply();
                                                        editor.remove(tendie + "_trade_price_count_" + Integer.toString(k)).apply();
                                                        editor.remove(tendie + "_note_count_" + Integer.toString(k)).apply();
                                                        editor.remove(tendie + "_date_count_" + Integer.toString(k)).apply();
                                                        editor.remove(tendie + "_buy_or_sell_count_" + Integer.toString(k)).apply();

                                                        //add tendie with new index
                                                        editor.putString(tendie + "_holding_count_" + Integer.toString(k), holding).apply();
                                                        editor.putString(tendie + "_trade_price_count_" + Integer.toString(k), tradePrice).apply();
                                                        editor.putString(tendie + "_note_count_" + Integer.toString(k), note).apply();
                                                        editor.putString(tendie + "_date_count_" + Integer.toString(k), date).apply();
                                                        editor.putString(tendie + "_buy_or_sell_count_" + Integer.toString(k), buyOrSell).apply();
                                                    }
                                                }
                                            } else {
                                                String holding = "";
                                                String tradePrice = "";
                                                int holdingNum = 0;

                                                holding = sharedPreferences.getString(tendie + "_holding_count_" + Integer.toString(i), holding);
                                                tradePrice = sharedPreferences.getString(tendie + "_trade_price_count_" + Integer.toString(i), tradePrice);

                                                if (holding.isEmpty() && tradePrice.isEmpty()) {
                                                    holding = "0";
                                                    tradePrice = "0.00";
                                                }
                                                holdingNum = Integer.parseInt(holding);
                                                holdings += holdingNum;

                                                BigDecimal tradePriceBigDecimal = new BigDecimal(tradePrice);
                                                BigDecimal holdingBigDecimal = new BigDecimal(holding);
                                                totalHoldings = totalHoldings.add(holdingBigDecimal);

                                                MainActivity.cumulativeMarketValAtTradePrices = MainActivity.cumulativeMarketValAtTradePrices.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                                                MainActivity.cumulativeMarketValAtCurrPrices = MainActivity.cumulativeMarketValAtCurrPrices.add(price.multiply(holdingBigDecimal));
                                                marketVal = marketVal.add(price.multiply(holdingBigDecimal));

                                                BigDecimal currentMarketVal = price.multiply(holdingBigDecimal);
                                                netCost = netCost.add(tradePriceBigDecimal.multiply(holdingBigDecimal));
                                                totalMarketValue = totalMarketValue.add(currentMarketVal);
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                BigDecimal pAndL = totalMarketValue.subtract(netCost);
                                MainActivity.totalTendiesChangeInDollars = MainActivity.cumulativeMarketValAtCurrPrices.subtract(MainActivity.cumulativeMarketValAtTradePrices);
                                MainActivity.totalPortfolioCost = MainActivity.cumulativeMarketValAtTradePrices;

                                if (!(MainActivity.totalTendiesChangeInDollars.compareTo(BigDecimal.ZERO) == 0)) {
                                    if( MainActivity.percentChange( MainActivity.cumulativeMarketValAtTradePrices,  MainActivity.cumulativeMarketValAtCurrPrices).isInfinite() ||  MainActivity.percentChange( MainActivity.cumulativeMarketValAtTradePrices,  MainActivity.cumulativeMarketValAtCurrPrices).isNaN()) {
                                        MainActivity.totalTendiesChange.setText( MainActivity.percentChange( MainActivity.cumulativeMarketValAtTradePrices,  MainActivity.cumulativeMarketValAtCurrPrices).toString()+"%");
                                    } else {
                                        MainActivity.totalDayPercentChange = new BigDecimal( MainActivity.percentChange( MainActivity.cumulativeMarketValAtTradePrices,  MainActivity.cumulativeMarketValAtCurrPrices), MathContext.DECIMAL64).setScale(2, RoundingMode.DOWN);
                                        MainActivity.totalTendiesChange.setText( MainActivity.totalDayPercentChange.setScale(2, RoundingMode.DOWN).toString()+"%");
                                    }                                    MainActivity.totalTendiesChange.setText(MainActivity.totalDayPercentChange.setScale(2, RoundingMode.DOWN).toString()+"%");
                                    MainActivity.totalTendiesValue.setText(String.format("$%,.2f", MainActivity.cumulativeMarketValAtCurrPrices.setScale(2, RoundingMode.DOWN)));
                                    MainActivity.updateChangeTextColorNoSymbol(MainActivity.totalTendiesChangeInDollars, MainActivity.totalChangeInCash);
                                    MainActivity.totalChangeInCash.setText(String.format("$%,.2f", MainActivity.totalTendiesChangeInDollars.setScale(2, RoundingMode.DOWN)));
                                    MainActivity.updateChangeTextColorNoSymbol(MainActivity.totalDayPercentChange, MainActivity.totalTendiesChange);
                                    MainActivity.totalPortfolioCostTv.setText(String.format("$%,.2f", MainActivity.totalPortfolioCost.setScale(2, RoundingMode.DOWN)));
                                } else if((MainActivity.totalTendiesChangeInDollars.compareTo(BigDecimal.ZERO) == 0) && ((MainActivity.cumulativeMarketValAtCurrPrices.compareTo(BigDecimal.ZERO) > 0) || (MainActivity.cumulativeMarketValAtCurrPrices.compareTo(BigDecimal.ZERO) < 0))){
                                    MainActivity.totalChangeInCash.setText("$0.00");
                                    MainActivity.totalTendiesChange.setText("0.00%");
                                    MainActivity.totalTendiesValue.setText(String.format("$%,.2f", MainActivity.cumulativeMarketValAtCurrPrices.setScale(2, RoundingMode.DOWN)));
                                    MainActivity.totalPortfolioCostTv.setText(String.format("$%,.2f", MainActivity.totalPortfolioCost.setScale(2, RoundingMode.DOWN)));
                                    MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, MainActivity.totalChangeInCash);
                                    MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, MainActivity.totalTendiesChange);
                                } else {
                                    MainActivity.totalChangeInCash.setText("$0.00");
                                    MainActivity.totalTendiesChange.setText("0.00%");
                                    MainActivity.totalTendiesValue.setText("$0.00");
                                    MainActivity.totalPortfolioCostTv.setText("$0.00");
                                    MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, MainActivity.totalChangeInCash);
                                    MainActivity.updateChangeTextColorNoSymbol(BigDecimal.ZERO, MainActivity.totalTendiesChange);
                                }

                                //update viewrow views
                                ViewRow.vrHoldings.setText(String.format("%,.0f", totalHoldings));
                                ViewRow.vrNetCost.setText(String.format("$%,.2f", netCost.setScale(2, RoundingMode.DOWN)));
                                ViewRow.vrPandL.setText(String.format("$%,.2f", pAndL.setScale(2, RoundingMode.DOWN)));
                                ViewRow.vrMarketValue.setText(String.format("$%,.2f", totalMarketValue.setScale(2, RoundingMode.DOWN)));

                                MainActivity.updateChangeTextColorNoSymbol(pAndL, ViewRow.vrPandL);

                                viewRowTendiesArrayList.remove(position);
                                ViewRowListAdapter.this.notifyDataSetChanged();
                                Log.i("DELETE", "delete called from clicking trash can");
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return rowView;
    }
}
