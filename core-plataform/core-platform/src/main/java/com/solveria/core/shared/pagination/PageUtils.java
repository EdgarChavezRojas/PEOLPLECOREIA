package com.solveria.core.shared.pagination;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
}

