package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateInfoDto {
    private String date;
    private Double price;
    private String status;
}
