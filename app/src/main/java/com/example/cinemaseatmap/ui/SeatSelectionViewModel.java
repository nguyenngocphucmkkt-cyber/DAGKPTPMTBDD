package com.example.cinemaseatmap.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.cinemaseatmap.data.MockSeatRepository;
import com.example.cinemaseatmap.model.Seat;
import com.example.cinemaseatmap.model.SeatFilter;
import java.util.*;

/** Single source of selection state, retained across Activity rotation. */
public final class SeatSelectionViewModel extends ViewModel {
    public SeatFilter.Mode filter=SeatFilter.Mode.ALL;
    public String query="";
    public boolean sheetExpanded;
    public final List<Seat> seats=MockSeatRepository.createSeats();
    private final LinkedHashSet<String> selected=new LinkedHashSet<>();
    private final Map<String,Seat> byId=new HashMap<>();
    public SeatSelectionViewModel() { for(Seat seat:seats) byId.put(seat.id,seat); }
    private final MutableLiveData<List<String>> changes=new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<String>> selection() { return changes; }
    public ArrayList<String> selectedIds() { return new ArrayList<>(selected); }
    public void toggle(Seat seat) {
        if(seat.status!=Seat.Status.AVAILABLE) return;
        if(!selected.remove(seat.id)) selected.add(seat.id);
        publish();
    }
    public void restore(List<String> ids) {
        selected.clear();
        for(String id:ids) {
            Seat seat=byId.get(id);
            if(seat!=null && seat.status==Seat.Status.AVAILABLE) selected.add(id);
        }
        publish();
    }
    public void setFilter(SeatFilter.Mode value) { if(filter!=value) { filter=value; publish(); } }
    public void setQuery(String value) { if(!query.equals(value)) { query=value; publish(); } }
    public void clearFilters() { filter=SeatFilter.Mode.ALL; query=""; publish(); }
    public boolean matches(Seat seat) { return SeatFilter.matches(seat,filter,query); }
    public List<String> matchingIds() {
        List<String> result=new ArrayList<>(); for(Seat s:seats) if(matches(s)) result.add(s.id); return result;
    }
    public List<Seat> selectedSeats() {
        List<Seat> result=new ArrayList<>();
        for(String id:selected) result.add(byId.get(id));
        return result;
    }
    public void remove(String id) { if(selected.remove(id)) publish(); }
    public int total() { int total=0; for(String id:selected) total+=byId.get(id).price; return total; }
    private void publish() { changes.setValue(Collections.unmodifiableList(new ArrayList<>(selected))); }
}
