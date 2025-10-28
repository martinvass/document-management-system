package hu.martinvass.dms.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class PageUtils {

    public int safePageIndex(int requestedPage, Page<?> page) {
        if (requestedPage < 1) return 1;

        if (page.getTotalPages() > 0 && requestedPage > page.getTotalPages())
            return page.getTotalPages();

        return requestedPage;
    }
}