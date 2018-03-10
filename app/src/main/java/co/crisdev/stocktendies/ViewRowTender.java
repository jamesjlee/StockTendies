package co.crisdev.stocktendies;

import android.graphics.drawable.Drawable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by lee on 1/28/18.
 */

public class ViewRowTender {
    private String ticker;
    private String holdings;
    private String price;
    private String notes;
    private String change;
    private String buyOrSell;

    public ViewRowTender(String ticker, String holdings, String price, String notes, String change, String buyOrSell) {
        this.ticker = ticker;
        this.holdings = holdings;
        this.price = price;
        this.notes = notes;
        this.change = change;
        this.buyOrSell = buyOrSell;
    }

    public String getBuyOrSell() {
        return buyOrSell;
    }

    public void setBuyOrSell(String buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getHoldings() {
        return holdings;
    }

    public void setHoldings(String holdings) {
        this.holdings = holdings;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }
}
