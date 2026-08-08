package ru.crolclient.implement.events.player;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.event.events.Event;
import ru.crolclient.common.util.player.MovingUtil;
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = false)
public class MovementInputEvent implements Event {
    private MovingUtil.DirectionalInput directionalInput;
    private boolean jumping;
    private boolean sneaking;
}
