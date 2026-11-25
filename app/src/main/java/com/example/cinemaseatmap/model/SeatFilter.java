package com.example.cinemaseatmap.model;
import java.util.Locale;
/** Shared filter rules used by state, Canvas and the selected-seat list. */
public final class SeatFilter {
 public enum Mode { ALL, STANDARD, VIP, COUPLE, AVAILABLE }
 private SeatFilter() { }
 public static boolean matches(Seat s,Mode mode,String query) {
  boolean type=mode==Mode.ALL || (mode==Mode.AVAILABLE && s.status==Seat.Status.AVAILABLE)
    || (mode==Mode.STANDARD && s.type==Seat.Type.STANDARD)
    || (mode==Mode.VIP && s.type==Seat.Type.VIP) || (mode==Mode.COUPLE && s.type==Seat.Type.COUPLE);
  return type && s.id.toUpperCase(Locale.ROOT).contains(query.trim().toUpperCase(Locale.ROOT));
 }
}
