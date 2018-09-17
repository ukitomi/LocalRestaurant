package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.MySQLConnection;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {

	  public List<Item> recommendItems(String userId, double lat, double lon) {
			List<Item> recommendedItems = new ArrayList<>();
			
			// Step 1, get all favorited itemids
			MySQLConnection connection = new MySQLConnection();
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
			
			// Step 2, get all categories, sort by count
			Map<String, Integer> allCategories = new HashMap<>();
			for (String itemId : favoritedItemIds) {
				Set<String> categories = connection.getCategories(itemId);
				for (String category : categories) {
					allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
				}
			}		
			
			List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
			Collections.sort(categoryList, (Entry<String, Integer> o1, Entry<String, Integer> o2) -> {
				return Integer.compare(o2.getValue(), o1.getValue());
			});
			
			// Step 3, search based on category, filter out favorite items
			Set<Item> visitedItems = new HashSet<>();
			for (Entry<String, Integer> category : categoryList) {
				List<Item> items = connection.searchItems(lat, lon, category.getKey());
				List<Item> filteredItems = new ArrayList<>();
				
				for (Item item : items) {
					if (!favoritedItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
						filteredItems.add(item);
					}
				}
				
				visitedItems.addAll(filteredItems);
				recommendedItems.addAll(filteredItems);
			}
			
			return recommendedItems;
	  }
}

