package co.crisdev.stocktendies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by lee on 1/28/18.
 */

public class MyListAdapter extends ArrayAdapter<Tender> {
    private final Context context;
    private final ArrayList<Tender> tendersArrayList;

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
        TextView tenderHoldings = (TextView) rowView.findViewById(R.id.rowTenderHoldings);
        TextView tenderSymbol = (TextView) rowView.findViewById(R.id.rowTenderPriceSymbol);
        TextView tenderPrice = (TextView) rowView.findViewById(R.id.rowTenderPrice);

        tenderDrawable.setImageDrawable(tendersArrayList.get(position).getTenderImage());
        tenderName.setText(tendersArrayList.get(position).getName());
        tenderHoldings.setText(Double.toString(tendersArrayList.get(position).getHoldings()));
        tenderSymbol.setText(tendersArrayList.get(position).getSymbol());
        tenderPrice.setText(tendersArrayList.get(position).getPrice().toString());

        return rowView;
    }
}
