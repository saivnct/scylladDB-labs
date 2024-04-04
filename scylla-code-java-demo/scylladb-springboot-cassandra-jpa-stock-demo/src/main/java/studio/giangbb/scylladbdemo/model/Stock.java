package studio.giangbb.scylladbdemo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by giangbb on 14/06/2023
 */
public class Stock {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("date")
    private Instant date;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonCreator
    public Stock(String symbol, Instant date, BigDecimal value) {
        this.symbol = symbol;
        this.date = date;
        this.value = value;
    }


    public String getSymbol() {
        return symbol;
    }

    public Instant getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Stock)) {
            return false;
        }

        Stock that = (Stock)obj;
        return symbol.equals(that.symbol) && date == that.date && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, date, value);
    }

    @Override
    public String toString() {
        return "Stock{" +
                "symbol='" + symbol + '\'' +
                ", date=" + date +
                ", value=" + value +
                '}';
    }
}
