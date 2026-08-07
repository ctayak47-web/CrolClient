
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.BlockView;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.MathHelper;
import vurst.visual.base.events.impl.player.EventAttack;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Hit Effects", category=Category.RENDER, description="Волна блоков при попадании по цели.")
public final class HitEffects
extends Module {
    public static final HitEffects INSTANCE = new HitEffects();
    private static final long WAVE_DURATION_MS = 1500L;
    private static final float WAVE_WIDTH = 2.5f;
    private static final int MAX_PER_FRAME = 400;
    private final List<WaveEffect> waveEffects = new ArrayList<WaveEffect>();
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final NumberSetting waveLengthBlocks = new NumberSetting("Длина (блоки)", 12.0f, 3.0f, 30.0f, 1.0f);

    private HitEffects() {
    }

    @Override
    public void onDisable() {
        this.waveEffects.clear();
        super.onDisable();
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getAction() != EventAttack.Action.POST || HitEffects.mc.world == null || event.getTarget() == null) {
            return;
        }
        Vec3d pos = event.getTarget().getPos();
        BlockPos basePos = BlockPos.ofFloored((double)pos.x, (double)(pos.y - 0.1), (double)pos.z);
        this.waveEffects.add(new WaveEffect(basePos, System.currentTimeMillis()));
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (HitEffects.mc.world == null || this.waveEffects.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<WaveEffect> iterator2 = this.waveEffects.iterator();
        while (iterator2.hasNext()) {
            WaveEffect wave = iterator2.next();
            if (wave.isExpired(now)) {
                iterator2.remove();
                continue;
            }
            wave.render(now);
        }
    }

    private static int withAlpha(int color, int alpha) {
        int a = MathHelper.clamp((int)alpha, (int)0, (int)255);
        return a << 24 | color & 0xFFFFFF;
    }

    private final class WaveEffect {
        private final BlockPos centerPos;
        private final long startTime;

        private WaveEffect(BlockPos centerPos, long startTime) {
            this.centerPos = centerPos;
            this.startTime = startTime;
        }

        private boolean isExpired(long now) {
            return now - this.startTime > 1500L;
        }

        private void render(long now) {
            if (IMinecraft.mc.world == null) {
                return;
            }
            int radius = Math.max(1, Math.round(HitEffects.this.waveLengthBlocks.getCurrent()));
            long elapsed = now - this.startTime;
            float progress = (float)elapsed / 1500.0f;
            float currentRadius = progress * (float)radius;
            float globalAlpha = (float)Math.pow(1.0f - progress, 0.6);
            float minRadSq = (currentRadius - 2.5f) * (currentRadius - 2.5f);
            float maxRadSq = (currentRadius + 0.5f) * (currentRadius + 0.5f);
            int baseColor = HitEffects.this.color.getColor().getRGB();
            int rendered = 0;
            int frameLimit = Math.max(400, radius * 40);
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockState state;
                    VoxelShape shape;
                    BlockPos checkPos;
                    BlockPos renderPos;
                    if (rendered >= frameLimit) {
                        return;
                    }
                    float distSq = x * x + z * z;
                    if (distSq < minRadSq || distSq > maxRadSq || (renderPos = this.findSurface(checkPos = this.centerPos.add(x, 0, z))) == null || (shape = (state = IMinecraft.mc.world.getBlockState(renderPos)).getOutlineShape((BlockView)IMinecraft.mc.world, renderPos)).isEmpty()) continue;
                    float distance = (float)Math.sqrt(distSq);
                    float localAlpha = 1.0f - Math.abs(distance - currentRadius) / 2.5f;
                    if ((localAlpha = MathHelper.clamp((float)localAlpha, (float)0.0f, (float)1.0f) * globalAlpha) <= 0.05f) continue;
                    ++rendered;
                    int color = HitEffects.withAlpha(baseColor, (int)(localAlpha * 255.0f));
                    float lineWidth = 1.0f + localAlpha * 2.5f;
                    try {
                        Render3DUtil.drawShapeAlternative(renderPos, shape, color, lineWidth, true, true);
                        continue;
                    }
                    catch (Exception exception) {
                        
                    }
                }
            }
        }

        private BlockPos findSurface(BlockPos pos) {
            if (IMinecraft.mc.world == null) {
                return null;
            }
            for (int y = 2; y >= -4; --y) {
                BlockPos p = pos.add(0, y, 0);
                if (IMinecraft.mc.world.getBlockState(p).isAir() || !IMinecraft.mc.world.getBlockState(p.up()).isAir()) continue;
                return p;
            }
            return null;
        }
    }
}

