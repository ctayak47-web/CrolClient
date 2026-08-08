
package crol.client.base.comand.impl.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.CommandSource;
import net.minecraft.Text;
import crol.client.CrolClient;
import crol.client.base.config.ConfigManager;

public class CfgArgumentType
implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of("default");

    public static CfgArgumentType create() {
        return new CfgArgumentType();
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        String config = reader.readString();
        CrolClient.getInstance().getConfigManager();
        config = ConfigManager.normalizeConfigName(config);
        if (CrolClient.getInstance().getConfigManager().findConfig(config) == null) {
            throw new DynamicCommandExceptionType(name -> Text.literal((String)("Config " + name.toString() + " does not exist."))).create((Object)config);
        }
        return config;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(CrolClient.getInstance().getConfigManager().configNames(), (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

