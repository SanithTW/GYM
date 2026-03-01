package com.example.OOP_FitConnect.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // In-memory order storage
    private static final List<Map<String, Object>> ORDERS = new ArrayList<>();

    public void saveOrder(Map<String, Object> orderData) {
        // Add timestamp to order data
        String orderDate = LocalDateTime.now().format(DATE_FORMATTER);
        orderData.put("orderDate", orderDate);

        // Sanitize items: ensure quantity is Integer and price is Double
        Object itemsObj = orderData.get("items");
        if (itemsObj instanceof List) {
            List<?> itemsList = (List<?>) itemsObj;
            for (Object itemObj : itemsList) {
                if (itemObj instanceof Map) {
                    Map<String, Object> item = (Map<String, Object>) itemObj;
                    try {
                        Object quantityObj = item.get("quantity");
                        if (quantityObj instanceof Number) {
                            item.put("quantity", ((Number) quantityObj).intValue());
                        } else if (quantityObj instanceof String) {
                            item.put("quantity", Integer.parseInt((String) quantityObj));
                        }
                        Object priceObj = item.get("price");
                        if (priceObj instanceof Number) {
                            item.put("price", ((Number) priceObj).doubleValue());
                        } else if (priceObj instanceof String) {
                            item.put("price", Double.parseDouble((String) priceObj));
                        }
                    } catch (Exception e) {
                        System.out.println("Error sanitizing item: " + item + " - " + e.getMessage());
                    }
                }
            }
        }

        // Store in memory
        ORDERS.add(new HashMap<>(orderData));
    }

    public List<Map<String, Object>> getAllOrders() {
        // Return a copy to avoid accidental modification
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> order : ORDERS) {
            copy.add(new HashMap<>(order));
        }
        // Sort by orderDate (newest first)
        copy.sort((o1, o2) -> {
            String date1 = (String) o1.get("orderDate");
            String date2 = (String) o2.get("orderDate");
            return date2.compareTo(date1);
        });
        return copy;
    }
} 
