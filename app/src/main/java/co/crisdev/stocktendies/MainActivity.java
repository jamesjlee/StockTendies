package co.crisdev.stocktendies;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends ListActivity {

    private ArrayList<Tender> tendiesList;
    private SharedPreferences sharedPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(this, AddTendiesToPortfolio.class);
                startActivity(intent);
                break;
            case R.id.reset:
                new AlertDialog.Builder(this)
                        .setTitle("Reset Portfolio")
                        .setMessage("Do you really want to drop all your tendies from your portfolio?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(MainActivity.this, "Dropped all your tendies.", Toast.LENGTH_SHORT).show();
                                tendiesList.clear();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.about:

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tendiesList = new ArrayList<Tender>();
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22, "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,   "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));
        tendiesList.add(new Tender(getResources().getDrawable(R.drawable.ic_launcher_foreground), "Venus", 2.22,  "$", new BigDecimal("0.02")));

        MyListAdapter listAdapter = new MyListAdapter(this, tendiesList);
        setListAdapter(listAdapter);
    }
}
