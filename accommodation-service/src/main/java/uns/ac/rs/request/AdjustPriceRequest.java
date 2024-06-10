package uns.ac.rs.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPriceRequest {
    List<IntervalPrice> pricesPerInterval;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntervalPrice {
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate;
        double price;
    }
}

