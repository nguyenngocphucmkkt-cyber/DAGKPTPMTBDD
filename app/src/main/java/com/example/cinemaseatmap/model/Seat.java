package com.example.cinemaseatmap.model;

/** Immutable world-space data, independent of the drawing layer. */
public final class Seat {
    public enum Type { STANDARD, VIP, COUPLE }
    public enum Status { AVAILABLE, BOOKED, LOCKED }

    public final String id;
    public final Type type;
    public final Status status;
    public final int price;
    public final float x, y, width, height;

    public Seat(String id, Type type, Status status, int price,
                float x, float y, float width, float height) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.price = price;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
