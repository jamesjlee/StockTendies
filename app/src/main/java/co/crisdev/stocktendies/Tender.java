package co.crisdev.stocktendies;

import android.graphics.drawable.Drawable;

import java.math.BigDecimal;

/**
 * Created by lee on 1/28/18.
 */

public class Tender {
    private Drawable tenderImage;
    private String name;
    private BigDecimal holdings;
    private BigDecimal price;
    private Drawable tenderImageFooter;
    private BigDecimal marketValue;
    private BigDecimal dayChangePercent;
    private String symbol;
    private BigDecimal changeInDollars;

    public Tender(Drawable tenderImage, String name, BigDecimal holdings, BigDecimal price, BigDecimal dayChangePercent, BigDecimal marketValue, String symbol, BigDecimal changeInDollars) {
        super();
        this.tenderImage = tenderImage;
        this.name = name;
        this.holdings = holdings;
        this.price = price;
        this.dayChangePercent = dayChangePercent;
        this.marketValue = marketValue;
        this.symbol = symbol;
        this.changeInDollars = changeInDollars;
    }

    public BigDecimal getChangeInDollars() {
        return changeInDollars;
    }

    public void setChangeInDollars(BigDecimal changeInDollars) {
        this.changeInDollars = changeInDollars;
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

    public BigDecimal getHoldings() {
        return holdings;
    }

    public void setHoldings(BigDecimal holdings) {
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
