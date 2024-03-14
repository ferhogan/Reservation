package com.resutaurant;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation {
    private String customerName;
    private int tableSize;
    private LocalDate date;
    private LocalTime time;

    public Reservation() {
    }

    public Reservation(String customerName, int tableSize, LocalDate date, LocalTime time) {
        this.customerName = customerName;
        this.tableSize = tableSize;
        this.date = date;
        this.time = time;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTableSize() {
        return tableSize;
    }

    public void setTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    // Equals and hashCode methods to compare reservations by date and time
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return date.equals(that.date) && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time);
    }
}
