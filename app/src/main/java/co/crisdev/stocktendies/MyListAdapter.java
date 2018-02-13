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

        tenderDrawable.setImageDrawable(tendersArrayList.get(position).getTenderImage());
        tenderName.setText(tendersArrayList.get(position).getName());
        tenderHoldings.setText(Integer.toString(tendersArrayList.get(position).getHoldings()));
        tenderPrice.setText(tendersArrayList.get(position).getSymbol()+tendersArrayList.get(position).getPrice().toString());
        tenderMarketValue.setText(tendersArrayList.get(position).getSymbol()+tendersArrayList.get(position).getMarketValue().toString());
        tenderDayChange.setText(tendersArrayList.get(position).getDayChangePercent().toString());

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

                                editor.remove(tenderName.getText().toString()+"_holdings_list").commit();
                                editor.remove(tenderName.getText().toString()+"_trade_price_list").commit();
                                editor.remove(tenderName.getText().toString()+"_notes_list").commit();

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

}
