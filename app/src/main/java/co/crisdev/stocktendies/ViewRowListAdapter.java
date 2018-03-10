package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
        params.height = 300;
        rowView.setLayoutParams(params);

        TextView viewRowHolding = (TextView) rowView.findViewById(R.id.viewRowHolding);
        TextView viewRowPrice = (TextView) rowView.findViewById(R.id.viewRowPrice);
        TextView viewRowNote = (TextView) rowView.findViewById(R.id.viewRowNote);
        TextView viewRowChange = (TextView) rowView.findViewById(R.id.viewRowChange);
        TextView viewRowDate = (TextView) rowView.findViewById(R.id.viewRowDate);
        TextView viewRowCost = (TextView) rowView.findViewById(R.id.viewRowCost);
        TextView viewRowHoldingLbl = (TextView) rowView.findViewById(R.id.viewRowHoldingLabel);
        TextView viewRowCostLbl = (TextView) rowView.findViewById(R.id.viewRowCostLabel);


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
        }
        viewRowHoldingLbl.setText(buyOrSell.toUpperCase()+":");
        viewRowHolding.setText(viewRowTendiesArrayList.get(position).getHoldings());
        viewRowPrice.setText("$"+tradePrice);
        viewRowChange.setText(change.toString()+"%");
        viewRowNote.setText(viewRowTendiesArrayList.get(position).getNotes());
        viewRowDate.setText(date);
        viewRowCost.setText("$"+cost.toString());

        MainActivity.updateChangeTextColor(change, viewRowChange);

        return rowView;
    }
}
