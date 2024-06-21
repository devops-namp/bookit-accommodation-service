package uns.ac.rs.entity.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    private String text;
    private String recipientId;
    private NotificationType notificationType;
    //private LocalDateTime time;



}
