package co.crisdev.stocktendies;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;

public class MainActivity extends ListActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Tender> tendiesList = new ArrayList<Tender>();
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22, "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,   "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));

        MyListAdapter listAdapter = new MyListAdapter(this, tendiesList);
        setListAdapter(listAdapter);
    }
}
