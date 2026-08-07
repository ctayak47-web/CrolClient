
package vurst.visual.base.events.impl.input;

import com.darkmagician6.eventapi.events.Event;
import lombok.Generated;
import net.minecraft.Screen;

public class EventSetScreen
implements Event {
    private Screen screen;

    @Generated
    public EventSetScreen(Screen screen) {
        this.screen = screen;
    }

    @Generated
    public Screen getScreen() {
        return this.screen;
    }

    @Generated
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EventSetScreen)) {
            return false;
        }
        EventSetScreen other = (EventSetScreen)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Screen this$screen = this.getScreen();
        Screen other$screen = other.getScreen();
        return !(this$screen == null ? other$screen != null : !this$screen.equals(other$screen));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof EventSetScreen;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Screen $screen = this.getScreen();
        result = result * 59 + ($screen == null ? 43 : $screen.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "EventSetScreen(screen=" + String.valueOf(this.getScreen()) + ")";
    }
}

