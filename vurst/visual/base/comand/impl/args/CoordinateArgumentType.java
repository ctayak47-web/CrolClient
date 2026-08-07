
package vurst.visual.base.comand.impl.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.CommandSource;

public class CoordinateArgumentType
implements ArgumentType<Double> {
    private static final Collection<String> DEFAULT_EXAMPLES = List.of("X", "Z");
    private final Collection<String> examples;

    public CoordinateArgumentType(Collection<String> examples) {
        this.examples = examples == null || examples.isEmpty() ? DEFAULT_EXAMPLES : examples;
    }

    public static CoordinateArgumentType create() {
        return new CoordinateArgumentType(DEFAULT_EXAMPLES);
    }

    public static CoordinateArgumentType create(String hint) {
        return new CoordinateArgumentType(List.of(hint));
    }

    public Double parse(StringReader reader) throws CommandSyntaxException {
        try {
            return Double.parseDouble(reader.readString());
        }
        catch (NumberFormatException e) {
            throw new CommandSyntaxException(null, () -> "Invalid number.");
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.examples, (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        return this.examples;
    }
}

