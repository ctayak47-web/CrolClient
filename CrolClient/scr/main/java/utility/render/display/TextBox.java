
package crol.client.utility.render.display;

import lombok.Generated;
import net.minecraft.MathHelper;
import net.minecraft.InputUtil;
import net.minecraft.Vector2f;
import crol.client.base.animations.base.Animation;
import crol.client.base.animations.base.Easing;
import crol.client.base.font.Font;
import crol.client.utility.game.other.MouseButton;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.math.MathUtil;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

public class TextBox
implements IMinecraft {
    private String text = "";
    private boolean selected;
    private boolean selectAll;
    private int cursor;
    private float posX;
    private Font font;
    private Vector2f position;
    private String emptyText;
    private float width;
    private long lastInputTime = System.currentTimeMillis();
    private int maxLength = Integer.MAX_VALUE;
    private CharFilter charFilter = CharFilter.ANY;
    private float scrollOffset = 0.0f;
    Animation animation = new Animation(400L, 0.2f, Easing.QUAD_IN_OUT);

    public TextBox(Vector2f position, Font font, String emptyText, float width) {
        this.font = font;
        this.emptyText = emptyText;
        this.width = width;
        this.position = position;
    }

    public void render(CustomDrawContext context, float x, float y, ColorRGBA colorText, ColorRGBA colorEmpty) {
        int endIndex;
        this.position = new Vector2f(x, y);
        this.cursor = MathHelper.clamp((int)this.cursor, (int)0, (int)this.text.length());
        this.posX = x;
        boolean isEmpty = this.isEmpty();
        float cursorX = 0.0f;
        if (!isEmpty) {
            String textBeforeCursor = this.text.substring(0, this.cursor);
            cursorX = this.font.width(textBeforeCursor);
        }
        float availableWidth = this.width;
        int startIndex = 0;
        while (this.font.width(this.text.substring(startIndex, this.cursor)) > availableWidth) {
            ++startIndex;
        }
        for (endIndex = this.cursor; endIndex < this.text.length() && this.font.width(this.text.substring(startIndex, endIndex)) < availableWidth; ++endIndex) {
        }
        String visibleText = this.text.substring(startIndex, endIndex);
        if (isEmpty) {
            context.drawText(this.font, this.emptyText, x, y, colorEmpty);
        } else {
            context.drawText(this.font, visibleText, x, y, colorText);
        }
        if (this.selected && System.currentTimeMillis() - this.lastInputTime > 200L) {
            float cursorDrawX = this.posX + cursorX - this.scrollOffset;
            this.animation.setDuration(250L);
            context.drawRect(cursorDrawX, y - 1.0f, 1.0f, this.font.height() + 2.0f, colorText.mulAlpha(this.animation.update(this.animation.getValue() == 0.2f ? 1.0f : (this.animation.getValue() == 1.0f ? 0.2f : this.animation.getTargetValue()))));
        }
        if (this.selectAll) {
            context.drawRect(x - 1.0f, y - 1.0f, this.font.width(visibleText) + 2.0f, this.font.height() + 2.0f, colorEmpty.mulAlpha(0.5f));
        }
    }

    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        Vector2f pos = this.getPosition();
        boolean bl = this.selected = button.getButtonIndex() == 0 && MathUtil.isHovered(mouseX, mouseY, pos.getX(), pos.getY() - 1.0f, this.width, this.font.height() + 2.0f);
        if (this.selected) {
            this.selectAll = false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.selected) {
            return false;
        }
        this.lastInputTime = System.currentTimeMillis();
        this.cursor = MathHelper.clamp((int)this.cursor, (int)0, (int)this.text.length());
        if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)341)) {
            if (keyCode == 86) {
                String clipboard = TextBox.mc.keyboard.getClipboard();
                if (this.selectAll) {
                    this.text = "";
                    this.cursor = 0;
                    this.selectAll = false;
                }
                this.addText(clipboard, this.cursor);
                this.cursor += clipboard.length();
                this.selectAll = false;
            } else if (keyCode == 65) {
                this.selectAll = true;
                this.cursor = this.text.length();
            } else if (keyCode == 67 && this.selected && this.selectAll) {
                TextBox.mc.keyboard.setClipboard(this.text);
            }
        } else if (keyCode == 261 && !this.text.isEmpty()) {
            this.removeText(this.cursor + 1);
            this.selectAll = false;
        } else if (keyCode == 259 && !this.text.isEmpty()) {
            if (this.selectAll) {
                this.text = "";
                this.cursor = 0;
                this.selectAll = false;
            } else {
                this.removeText(this.cursor);
                --this.cursor;
                if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)341)) {
                    while (!this.text.isEmpty() && this.cursor > 0) {
                        this.removeText(this.cursor);
                        --this.cursor;
                    }
                }
            }
        } else if (keyCode == 262) {
            ++this.cursor;
            if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)341)) {
                this.cursor = this.text.length();
            }
            this.selectAll = false;
        } else if (keyCode == 263) {
            --this.cursor;
            if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)341)) {
                this.cursor = 0;
            }
            this.selectAll = false;
        } else if (keyCode == 269) {
            this.cursor = this.text.length();
            this.selectAll = false;
        } else if (keyCode == 268) {
            this.cursor = 0;
            this.selectAll = false;
        }
        this.cursor = MathHelper.clamp((int)this.cursor, (int)0, (int)this.text.length());
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.selected) {
            return false;
        }
        this.lastInputTime = System.currentTimeMillis();
        this.cursor = MathHelper.clamp((int)this.cursor, (int)0, (int)this.text.length());
        if (this.selectAll) {
            this.text = "";
            this.cursor = 0;
            this.selectAll = false;
        }
        this.addText(Character.toString(codePoint), this.cursor);
        ++this.cursor;
        this.cursor = MathHelper.clamp((int)this.cursor, (int)0, (int)this.text.length());
        return true;
    }

    private void addText(String newText, int position) {
        StringBuilder filteredText = new StringBuilder();
        for (char c : newText.toCharArray()) {
            if (!this.charFilter.isAllowed(c)) continue;
            filteredText.append(c);
        }
        String filtered = filteredText.toString();
        if (this.text.length() + filtered.length() > this.maxLength) {
            int available = this.maxLength - this.text.length();
            if (available <= 0) {
                return;
            }
            filtered = filtered.substring(0, Math.min(available, filtered.length()));
        }
        StringBuilder newFinalText = new StringBuilder();
        boolean inserted = false;
        for (int i = 0; i < this.text.length(); ++i) {
            if (i == position) {
                inserted = true;
                newFinalText.append(filtered);
            }
            newFinalText.append(this.text.charAt(i));
        }
        if (!inserted) {
            newFinalText.append(filtered);
        }
        this.text = newFinalText.toString();
    }

    private void removeText(int position) {
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < this.text.length(); ++i) {
            if (i == position - 1) continue;
            newText.append(this.text.charAt(i));
        }
        this.text = newText.toString();
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public boolean isSelected() {
        return this.selected;
    }

    @Generated
    public boolean isSelectAll() {
        return this.selectAll;
    }

    @Generated
    public int getCursor() {
        return this.cursor;
    }

    @Generated
    public float getPosX() {
        return this.posX;
    }

    @Generated
    public Font getFont() {
        return this.font;
    }

    @Generated
    public Vector2f getPosition() {
        return this.position;
    }

    @Generated
    public String getEmptyText() {
        return this.emptyText;
    }

    @Generated
    public float getWidth() {
        return this.width;
    }

    @Generated
    public long getLastInputTime() {
        return this.lastInputTime;
    }

    @Generated
    public int getMaxLength() {
        return this.maxLength;
    }

    @Generated
    public CharFilter getCharFilter() {
        return this.charFilter;
    }

    @Generated
    public float getScrollOffset() {
        return this.scrollOffset;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }

    @Generated
    public void setText(String text) {
        this.text = text;
    }

    @Generated
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Generated
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    @Generated
    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    @Generated
    public void setPosX(float posX) {
        this.posX = posX;
    }

    @Generated
    public void setFont(Font font) {
        this.font = font;
    }

    @Generated
    public void setPosition(Vector2f position) {
        this.position = position;
    }

    @Generated
    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText;
    }

    @Generated
    public void setWidth(float width) {
        this.width = width;
    }

    @Generated
    public void setLastInputTime(long lastInputTime) {
        this.lastInputTime = lastInputTime;
    }

    @Generated
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Generated
    public void setCharFilter(CharFilter charFilter) {
        this.charFilter = charFilter;
    }

    @Generated
    public void setScrollOffset(float scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    @Generated
    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TextBox)) {
            return false;
        }
        TextBox other = (TextBox)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isSelected() != other.isSelected()) {
            return false;
        }
        if (this.isSelectAll() != other.isSelectAll()) {
            return false;
        }
        if (this.getCursor() != other.getCursor()) {
            return false;
        }
        if (Float.compare(this.getPosX(), other.getPosX()) != 0) {
            return false;
        }
        if (Float.compare(this.getWidth(), other.getWidth()) != 0) {
            return false;
        }
        if (this.getLastInputTime() != other.getLastInputTime()) {
            return false;
        }
        if (this.getMaxLength() != other.getMaxLength()) {
            return false;
        }
        if (Float.compare(this.getScrollOffset(), other.getScrollOffset()) != 0) {
            return false;
        }
        String this$text = this.getText();
        String other$text = other.getText();
        if (this$text == null ? other$text != null : !this$text.equals(other$text)) {
            return false;
        }
        Font this$font = this.getFont();
        Font other$font = other.getFont();
        if (this$font == null ? other$font != null : !this$font.equals(other$font)) {
            return false;
        }
        Vector2f this$position = this.getPosition();
        Vector2f other$position = other.getPosition();
        if (this$position == null ? other$position != null : !this$position.equals(other$position)) {
            return false;
        }
        String this$emptyText = this.getEmptyText();
        String other$emptyText = other.getEmptyText();
        if (this$emptyText == null ? other$emptyText != null : !this$emptyText.equals(other$emptyText)) {
            return false;
        }
        CharFilter this$charFilter = this.getCharFilter();
        CharFilter other$charFilter = other.getCharFilter();
        if (this$charFilter == null ? other$charFilter != null : !((Object)((Object)this$charFilter)).equals((Object)other$charFilter)) {
            return false;
        }
        Animation this$animation = this.getAnimation();
        Animation other$animation = other.getAnimation();
        return !(this$animation == null ? other$animation != null : !this$animation.equals(other$animation));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof TextBox;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isSelected() ? 79 : 97);
        result = result * 59 + (this.isSelectAll() ? 79 : 97);
        result = result * 59 + this.getCursor();
        result = result * 59 + Float.floatToIntBits(this.getPosX());
        result = result * 59 + Float.floatToIntBits(this.getWidth());
        long $lastInputTime = this.getLastInputTime();
        result = result * 59 + (int)($lastInputTime >>> 32 ^ $lastInputTime);
        result = result * 59 + this.getMaxLength();
        result = result * 59 + Float.floatToIntBits(this.getScrollOffset());
        String $text = this.getText();
        result = result * 59 + ($text == null ? 43 : $text.hashCode());
        Font $font = this.getFont();
        result = result * 59 + ($font == null ? 43 : $font.hashCode());
        Vector2f $position = this.getPosition();
        result = result * 59 + ($position == null ? 43 : $position.hashCode());
        String $emptyText = this.getEmptyText();
        result = result * 59 + ($emptyText == null ? 43 : $emptyText.hashCode());
        CharFilter $charFilter = this.getCharFilter();
        result = result * 59 + ($charFilter == null ? 43 : ((Object)((Object)$charFilter)).hashCode());
        Animation $animation = this.getAnimation();
        result = result * 59 + ($animation == null ? 43 : $animation.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "TextBox(text=" + this.getText() + ", selected=" + this.isSelected() + ", selectAll=" + this.isSelectAll() + ", cursor=" + this.getCursor() + ", posX=" + this.getPosX() + ", font=" + String.valueOf(this.getFont()) + ", position=" + String.valueOf(this.getPosition()) + ", emptyText=" + this.getEmptyText() + ", width=" + this.getWidth() + ", lastInputTime=" + this.getLastInputTime() + ", maxLength=" + this.getMaxLength() + ", charFilter=" + String.valueOf((Object)this.getCharFilter()) + ", scrollOffset=" + this.getScrollOffset() + ", animation=" + String.valueOf(this.getAnimation()) + ")";
    }

    public static enum CharFilter {
        ANY,
        ENGLISH,
        ENGLISH_NUMBERS,
        CYRILLIC,
        NUMBERS_ONLY;

        public boolean isAllowed(char c) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> true;
                case 1 -> {
                    if (Character.isLetter(c) && c <= '' && Character.isAlphabetic(c)) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> {
                    if (Character.isLetterOrDigit(c) && c <= '') {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> String.valueOf(c).matches("[А-Яа-яЁё]");
                case 4 -> Character.isDigit(c);
            };
        }
    }
}

