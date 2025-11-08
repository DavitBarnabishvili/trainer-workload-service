package com.gym.crm.workloadservice.entity;

import com.gym.crm.workload.entity.MonthSummary;
import com.gym.crm.workload.entity.YearSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class YearSummaryTest {

    @Test
    void testAddOrUpdateMonth_NewMonth() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        year.addOrUpdateMonth(5, 120);

        assertEquals(1, year.getMonths().size());
        assertEquals(120, year.getMonths().getFirst().getTotalDuration());
        assertEquals(5, year.getMonths().getFirst().getMonth());
    }

    @Test
    void testAddOrUpdateMonth_ExistingMonth() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        year.addOrUpdateMonth(5, 120);
        year.addOrUpdateMonth(5, 30);

        assertEquals(1, year.getMonths().size());
        assertEquals(150, year.getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testRemoveFromMonth() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        year.addOrUpdateMonth(5, 100);
        year.removeFromMonth(5, 40);

        assertEquals(60, year.getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testRemoveFromMonth_BelowZero() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        year.addOrUpdateMonth(5, 50);
        year.removeFromMonth(5, 100);

        assertEquals(0, year.getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testBidirectionalLinkIsSet() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        year.addOrUpdateMonth(5, 50);

        MonthSummary month = year.getMonths().getFirst();
        assertSame(year, month.getYearSummary(), "Month must reference its parent YearSummary");
    }
}
