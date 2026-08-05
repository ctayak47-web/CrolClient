package com.crolclient.buyer.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BuyerData {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinter().create();
	private static final String CONFIG_DIR = "config/doublechest";
	private static final String DATA_FILE = CONFIG_DIR + "/items.json";
	
	private static double balance = 1000.0;
	private static Map<String, List<BuyerItem>> categories = new LinkedHashMap<>();
	private static List<BuyerItem> hotItems = new ArrayList<>();
	
	public static void load() {
		try {
			Files.createDirectories(Paths.get(CONFIG_DIR));
			File file = new File(DATA_FILE);
			
			if (file.exists()) {
				try (FileReader reader = new FileReader(file)) {
					JsonObject json = GSON.fromJson(reader, JsonObject.class);
					balance = json.has("balance") ? json.get("balance").getAsDouble() : 1000.0;
					
					if (json.has("categories")) {
						JsonObject cats = json.getAsJsonObject("categories");
						for (String catName : cats.keySet()) {
							JsonArray items = cats.getAsJsonArray(catName);
							List<BuyerItem> list = new ArrayList<>();
							for (var elem : items) {
								JsonObject obj = elem.getAsJsonObject();
								list.add(new BuyerItem(
									obj.get("name").getAsString(),
									obj.get("icon").getAsString(),
									obj.get("price").getAsDouble(),
									obj.get("maxStack").getAsInt()
								));
							}
							categories.put(catName, list);
						}
					}
					
					if (json.has("hotItems")) {
						JsonArray hot = json.getAsJsonArray("hotItems");
						for (var elem : hot) {
							JsonObject obj = elem.getAsJsonObject();
							hotItems.add(new BuyerItem(
								obj.get("name").getAsString(),
								obj.get("icon").getAsString(),
								obj.get("price").getAsDouble(),
								obj.get("maxStack").getAsInt()
							));
						}
					}
				}
			} else {
				createDefaults();
				save();
			}
		} catch (Exception e) {
			e.printStackTrace();
			createDefaults();
		}
	}
	
	private static void createDefaults() {
		balance = 10000.0;
		categories.clear();
		hotItems.clear();
		
		List<BuyerItem> blocks = Arrays.asList(
			new BuyerItem("Stone", "stone", 10.0, 64),
			new BuyerItem("Dirt", "dirt", 5.0, 64),
			new BuyerItem("Oak Log", "oak_log", 20.0, 64),
			new BuyerItem("Spruce Log", "spruce_log", 22.0, 64)
		);
		categories.put("Blocks", blocks);
		
		List<BuyerItem> ores = Arrays.asList(
			new BuyerItem("Coal Ore", "coal_ore", 50.0, 64),
			new BuyerItem("Iron Ore", "iron_ore", 100.0, 64),
			new BuyerItem("Gold Ore", "gold_ore", 200.0, 64),
			new BuyerItem("Diamond Ore", "diamond_ore", 500.0, 64)
		);
		categories.put("Ores", ores);
		
		List<BuyerItem> ingots = Arrays.asList(
			new BuyerItem("Iron Ingot", "iron_ingot", 50.0, 64),
			new BuyerItem("Gold Ingot", "gold_ingot", 100.0, 64),
			new BuyerItem("Diamond", "diamond", 250.0, 64)
		);
		categories.put("Materials", ingots);
		
		hotItems = Arrays.asList(
			new BuyerItem("Diamond", "diamond", 250.0, 64),
			new BuyerItem("Gold Ingot", "gold_ingot", 100.0, 64),
			new BuyerItem("Iron Ingot", "iron_ingot", 50.0, 64)
		);
	}
	
	public static void save() {
		try {
			JsonObject root = new JsonObject();
			root.addProperty("balance", balance);
			
			JsonObject cats = new JsonObject();
			for (String catName : categories.keySet()) {
				JsonArray items = new JsonArray();
				for (BuyerItem item : categories.get(catName)) {
					JsonObject obj = new JsonObject();
					obj.addProperty("name", item.name);
					obj.addProperty("icon", item.icon);
					obj.addProperty("price", item.price);
					obj.addProperty("maxStack", item.maxStack);
					items.add(obj);
				}
				cats.add(catName, items);
			}
			root.add("categories", cats);
			
			JsonArray hot = new JsonArray();
			for (BuyerItem item : hotItems) {
				JsonObject obj = new JsonObject();
				obj.addProperty("name", item.name);
				obj.addProperty("icon", item.icon);
				obj.addProperty("price", item.price);
				obj.addProperty("maxStack", item.maxStack);
				hot.add(obj);
			}
			root.add("hotItems", hot);
			
			Files.createDirectories(Paths.get(CONFIG_DIR));
			try (FileWriter writer = new FileWriter(DATA_FILE)) {
				GSON.toJson(root, writer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double getBalance() { return balance; }
	public static void setBalance(double b) { balance = b; save(); }
	public static void addBalance(double amount) { balance += amount; save(); }
	
	public static Map<String, List<BuyerItem>> getCategories() { return categories; }
	public static List<BuyerItem> getHotItems() { return hotItems; }
	public static List<BuyerItem> getCategoryItems(String name) { return categories.getOrDefault(name, new ArrayList<>()); }
}
