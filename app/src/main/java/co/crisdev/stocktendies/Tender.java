package co.crisdev.stocktendies;

import android.graphics.drawable.Drawable;

import java.math.BigDecimal;

/**
 * Created by lee on 1/28/18.
 */

public class Tender {
    private Drawable tenderImage;
    private String name;
    private int holdings;
    private BigDecimal price;
    private Drawable tenderImageFooter;
    private BigDecimal marketValue;
    private BigDecimal dayChangePercent;

    public Tender(Drawable tenderImage, String name, int holdings, BigDecimal price, BigDecimal dayChangePercent, BigDecimal marketValue) {
        super();
        this.tenderImage = tenderImage;
        this.name = name;
        this.holdings = holdings;
        this.price = price;
        this.dayChangePercent = dayChangePercent;
        this.marketValue = marketValue;
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

    public int getHoldings() {
        return holdings;
    }

    public void setHoldings(int holdings) {
        this.holdings = holdings;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Drawable getTenderImageFooter() {
        return tenderImageFooter;
    }

    public void setTenderImageFooter(Drawable tenderImageFooter) {
        this.tenderImageFooter = tenderImageFooter;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }
}
