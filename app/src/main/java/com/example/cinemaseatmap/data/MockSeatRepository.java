package com.example.cinemaseatmap.data;

import com.example.cinemaseatmap.model.Seat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Repeatable mock layout: 120 standard + 60 VIP + 20 couple objects. */
public final class MockSeatRepository {
    public static final float WORLD_WIDTH = 1160f;
    public static final float WORLD_HEIGHT = 1050f;

    private MockSeatRepository() { }

    public static List<Seat> createSeats() {
        List<Seat> result = new ArrayList<>(200);
        for (int row = 0; row < 11; row++) {
            Seat.Type type = row < 6 ? Seat.Type.STANDARD
                    : row < 9 ? Seat.Type.VIP : Seat.Type.COUPLE;
            int count = type == Seat.Type.COUPLE ? 10 : 20;
            float y = rowY(row);
            for (int col = 0; col < count; col++) {
                int index = result.size();
                Seat.Status status = index % 17 == 0 ? Seat.Status.LOCKED
                        : index % 7 == 0 ? Seat.Status.BOOKED : Seat.Status.AVAILABLE;
                float x = type == Seat.Type.COUPLE
                        ? 80 + col * 100 + (col >= 5 ? 40 : 0)
                        : 80 + col * 50 + (col >= 10 ? 40 : 0);
                result.add(new Seat("" + (char) ('A' + row) + (col + 1), type, status,
                        type == Seat.Type.STANDARD ? 80000 : type == Seat.Type.VIP ? 120000 : 200000,
                        x, y, type == Seat.Type.COUPLE ? 90 : 40, 42));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static float rowY(int row) {
        if (row < 6) return 220 + row * 58;
        if (row < 9) return 640 + (row - 6) * 58;
        return 890 + (row - 9) * 58;
    }
}
