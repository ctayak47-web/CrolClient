package crol.client.util.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public record ScoreEntry(Text name, Text score, int scoreWidth) {
}
