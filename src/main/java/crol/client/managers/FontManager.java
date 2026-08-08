package crol.client.managers;

import com.google.common.base.Suppliers;
import crol.client.util.render.msdf.MsdfFont;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FontManager {
   public static final Supplier<MsdfFont> MONTSERRAT = Suppliers.memoize(() -> MsdfFont.builder().atlas("montserrat").data("montserrat").build());
   public static final Supplier<MsdfFont> BOLD = Suppliers.memoize(() -> MsdfFont.builder().atlas("bold").data("bold").build());
   public static final Supplier<MsdfFont> CATEGORY = Suppliers.memoize(() -> MsdfFont.builder().atlas("font").data("font").build());
   public static final Supplier<MsdfFont> ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons").data("icons").build());
   public static final Supplier<MsdfFont> ICONS2 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons2").data("icons2").build());
   public static final Supplier<MsdfFont> MAINMENU = Suppliers.memoize(() -> MsdfFont.builder().atlas("mainmenu").data("mainmenu").build());
   public static final Supplier<MsdfFont> ICONS3 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons3").data("icons3").build());
   public static final Supplier<MsdfFont> ICONS4 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons4").data("icons4").build());
   public static final Supplier<MsdfFont> ICONS5 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons5").data("icons5").build());
   public static final Supplier<MsdfFont> ICONS6 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons6").data("icons6").build());
   public static final Supplier<MsdfFont> COORDS = Suppliers.memoize(() -> MsdfFont.builder().atlas("coords").data("coords").build());
   public static final Supplier<MsdfFont> SFPRO = Suppliers.memoize(() -> MsdfFont.builder().atlas("sfpro").data("sfpro").build());
   public static final Supplier<MsdfFont> WILD = Suppliers.memoize(() -> MsdfFont.builder().atlas("crol").data("crol").build());
   public static final Supplier<MsdfFont> SUISSEINTREGULAR = Suppliers.memoize(() -> MsdfFont.builder().atlas("suisseintlregular").data("suisseintlregular").build());
   public static final Supplier<MsdfFont> SUISSEINTMEDIUM = Suppliers.memoize(() -> MsdfFont.builder().atlas("suisseintlmedium").data("suisseintlmedium").build());
}
