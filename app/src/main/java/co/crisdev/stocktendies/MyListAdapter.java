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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
                        .setMessage("Do you really remove " +  tenderName.getText().toString() + " from your portfolio?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getContext(), "Removed Tendie: " + tenderName.getText().toString() + " from your portfolio!", Toast.LENGTH_SHORT).show();
                                Set<String> tendiesSet = new HashSet<>();

                                SharedPreferences.Editor editor;
                                sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.tendiesPrefs), Context.MODE_PRIVATE);
                                editor = sharedPreferences.edit();
                                tendiesSet = sharedPreferences.getStringSet("tendiesList", tendiesSet);
                                tendiesSet.remove(tenderName.getText().toString().toUpperCase());
                                while(tendiesSet.iterator().hasNext()) {
                                    if(tendiesSet.iterator().next().equals(tenderName.getText().toString().toUpperCase())) {
                                        tendiesSet.iterator().remove();
                                    }
                                }
                                editor.putStringSet("tendiesList", tendiesSet).commit();


                                String count = "";
                                count = sharedPreferences.getString(tenderName.getText() + "_count_", count);
                                int countNum = Integer.parseInt(count);

                                for(int i=1; i<=countNum; i++) {
                                    editor.remove(tenderName.getText() + "_holding_count_" + Integer.toString(i)).commit();
                                    editor.remove(tenderName.getText() + "_trade_price_count_" + Integer.toString(i)).commit();
                                    editor.remove(tenderName.getText() + "_note_count_" + Integer.toString(i)).commit();
                                    editor.remove(tenderName.getText() + "_count_" + Integer.toString(i)).commit();
                                }

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

}
