package ru.crolclient.common.util.auction;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionPriceParser {
    private final Pattern funTimePricePattern = Pattern.compile("\\$(\\d+(?:\\s\\d{3})*(?:\\.\\d{2})?)");

    public int getPrice(ItemStack stack) {
        String displayName = stack.getName() != null ? stack.getName().getString() : "";
        Matcher matcher = funTimePricePattern.matcher(displayName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1).replaceAll("[^\\d.]", ""));
        }
        return -1;
    }
}