package ru.abramov.FinFlow.FinFlow.dto.Analytics;

public enum PeriodUnit {
    DAY, WEEK, MONTH, YEAR;

    public static PeriodUnit fromString(String value) {
        return switch (value.toLowerCase()) {
            case "day" -> DAY;
            case "week" -> WEEK;
            case "month" -> MONTH;
            case "year" -> YEAR;
            default -> throw new IllegalArgumentException("Unknown period: " + value);
        };
    }
}
