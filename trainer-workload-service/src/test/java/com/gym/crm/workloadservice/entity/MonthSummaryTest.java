package com.gym.crm.workloadservice.entity;

import com.gym.crm.workload.entity.MonthSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonthSummaryTest {

    @Test
    void testGetMonthName() {
        MonthSummary month = MonthSummary.builder()
                .month(1)
                .totalDuration(0)
                .build();

        assertEquals("JANUARY", month.getMonthName());
    }

    @Test
    void testMonthStoresDuration() {
        MonthSummary month = MonthSummary.builder()
                .month(2)
                .totalDuration(90)
                .build();

        assertEquals(90, month.getTotalDuration());
    }
}
