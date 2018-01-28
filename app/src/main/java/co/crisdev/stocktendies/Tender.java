package co.crisdev.stocktendies;

import android.graphics.drawable.Drawable;
import java.math.BigDecimal;

/**
 * Created by lee on 1/28/18.
 */

public class Tender {
    private Drawable tenderImage;
    private String name;
    private double holdings;
    private String symbol;
    private BigDecimal price;
    private Drawable tenderImageFooter;

    public Tender(Drawable tenderImage, String name, double holdings, String symbol, BigDecimal price) {
        super();
        this.tenderImage = tenderImage;
        this.name = name;
        this.holdings = holdings;
        this.symbol = symbol;
        this.price = price;
    }

    public Drawable getTenderImage() {
        return tenderImage;
    }

    public void setTenderImage(Drawable tenderImage) {
        this.tenderImage = tenderImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHoldings() {
        return holdings;
    }

    public void setHoldings(double holdings) {
        this.holdings = holdings;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
