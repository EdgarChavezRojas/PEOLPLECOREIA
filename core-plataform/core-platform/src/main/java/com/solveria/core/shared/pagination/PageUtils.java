package com.solveria.core.shared.pagination;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

  private PageUtils() {}

  public static <T> Page<T> slice(List<T> items, Pageable pageable) {
    if (items == null || items.isEmpty()) {
      return Page.empty(pageable);
    }
    int start = (int) pageable.getOffset();
    if (start >= items.size()) {
      return Page.empty(pageable);
    }
    int end = Math.min(start + pageable.getPageSize(), items.size());
    return new PageImpl<>(items.subList(start, end), pageable, items.size());
  }

  /**
   * Sanitizes a Pageable object, filtering out any sort properties that do not exist on the target
   * entity class. Also cleans up JSON-formatted query parameter inputs like "[\"string\"]" or
   * similar default values.
   */
  public static Pageable sanitize(Pageable pageable, Class<?> entityClass) {
    if (pageable == null) {
      return null;
    }
    if (pageable.getSort().isUnsorted()) {
      return pageable;
    }

    List<Sort.Order> validOrders = new ArrayList<>();
    for (Sort.Order order : pageable.getSort()) {
      String cleanProperty = order.getProperty().replaceAll("[\\[\\]\"']", "").trim();
      if (isValidProperty(entityClass, cleanProperty)) {
        validOrders.add(new Sort.Order(order.getDirection(), cleanProperty));
      }
    }

    if (validOrders.isEmpty()) {
      return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    } else {
      return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(validOrders));
    }
  }

  private static boolean isValidProperty(Class<?> clazz, String property) {
    if (property == null || property.isBlank()) {
      return false;
    }
    String[] parts = property.split("\\.");
    Class<?> currentClass = clazz;
    for (String part : parts) {
      if (currentClass == null) {
        return false;
      }
      Field field = getDeclaredFieldRecursive(currentClass, part);
      if (field == null) {
        return false;
      }
      currentClass = field.getType();
    }
    return true;
  }

  private static Field getDeclaredFieldRecursive(Class<?> clazz, String fieldName) {
    if (clazz == null || Object.class.equals(clazz)) {
      return null;
    }
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      return getDeclaredFieldRecursive(clazz.getSuperclass(), fieldName);
    }
  }
}
