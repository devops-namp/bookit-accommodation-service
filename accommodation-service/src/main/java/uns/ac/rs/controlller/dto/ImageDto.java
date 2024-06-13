package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private String base64Image;

    public byte[] getImageAsBytes() {
        return Base64.getDecoder().decode(base64Image);
    }
}
