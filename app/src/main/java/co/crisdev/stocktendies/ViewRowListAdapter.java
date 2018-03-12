package co.crisdev.stocktendies;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

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
        params.height = 350;
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
        BigDecimal change = MainActivity.percentChange(new BigDecimal(viewRowTendiesArrayList.get(position).getPrice()).multiply(holdings), cost);

        if(buyOrSell.equals("sell")) {
            viewRowCostLbl.setText("Proceeds:");
            viewRowChange.setVisibility(View.GONE);
            viewRowChangeLbl.setVisibility(View.GONE);
        } else if(buyOrSell.equals("buy")) {
            viewRowChange.setText(String.format("%,.2f", change));
        }
        viewRowHoldingLbl.setText(buyOrSell.toUpperCase()+":");
        viewRowHolding.setText(String.format("%,.0f", new BigDecimal(viewRowTendiesArrayList.get(position).getHoldings())));
        viewRowPrice.setText(String.format("$%,.2f", new BigDecimal(tradePrice).setScale(2, RoundingMode.HALF_UP)));
        viewRowNote.setText(viewRowTendiesArrayList.get(position).getNotes());
        viewRowDate.setText(date);
        viewRowCost.setText(String.format("$%,.2f", cost.setScale(2, RoundingMode.HALF_UP)));

        MainActivity.updateChangeTextColorNoSymbol(change, viewRowChange);

        return rowView;
    }
}
