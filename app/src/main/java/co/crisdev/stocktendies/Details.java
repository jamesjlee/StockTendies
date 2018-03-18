package co.crisdev.stocktendies;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import static co.crisdev.stocktendies.ViewRow.vrSpinner;

/**
 * Created by lee on 3/16/18.
 */

public class Details extends Activity  {
    private String ticker;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        webView = (WebView) findViewById(R.id.webview);

        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();

        ticker = "";

        if (!bundle.isEmpty()) {
            boolean hasTicker = bundle.containsKey("ticker");
            boolean hasPrice = bundle.containsKey("price");

            if (hasTicker && hasPrice) {
                ticker = bundle.getString("ticker");
            }
        }

        webView.getSettings().setJavaScriptEnabled(true);

        String htmlString = "<!-- TradingView Widget BEGIN -->\n" +
                "<div class=\"tradingview-widget-container\">\n" +
                "  <div id=\"tradingview_1f617\"></div>\n" +
                "  <div class=\"tradingview-widget-copyright\"><a href=\"https://www.tradingview.com/symbols/NASDAQ-"+ticker+"/\" rel=\"noopener\" target=\"_blank\"><span class=\"blue-text\">"+ticker+"</span> <span class=\"blue-text\">chart</span> by TradingView</a></div>\n" +
                "  <script type=\"text/javascript\" src=\"https://s3.tradingview.com/tv.js\"></script>\n" +
                "  <script type=\"text/javascript\">\n" +
                "  new TradingView.widget(\n" +
                "  {\n" +
                "  \"autosize\": true,\n" +
                "  \"symbol\": \"NASDAQ:" + ticker + "\",\n" +
                "  \"interval\": \"D\",\n" +
                "  \"timezone\": \"Etc/UTC\",\n" +
                "  \"theme\": \"Light\",\n" +
                "  \"style\": \"1\",\n" +
                "  \"locale\": \"en\",\n" +
                "  \"toolbar_bg\": \"#f1f3f6\",\n" +
                "  \"enable_publishing\": false,\n" +
                "  \"save_image\": false,\n" +
                "  \"hideideas\": true,\n" +
                "  \"container_id\": \"tradingview_1f617\"\n" +
                "}\n" +
                "  );\n" +
                "  </script>\n" +
                "</div>\n" +
                "<!-- TradingView Widget END -->";

        webView.loadData(htmlString, "text/html", null);
        vrSpinner.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.detailsHome:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}
