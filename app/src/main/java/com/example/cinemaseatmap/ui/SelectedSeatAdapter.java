package com.example.cinemaseatmap.ui;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinemaseatmap.R;
import com.example.cinemaseatmap.model.Seat;
import java.util.*;
import java.text.NumberFormat;
public final class SelectedSeatAdapter extends RecyclerView.Adapter<SelectedSeatAdapter.Holder> {
 public interface Actions { void focus(Seat seat); void remove(Seat seat); }
 private final Actions actions;
 private List<Seat> seats=Collections.emptyList();
 private Set<String> matching=Collections.emptySet();
 private final NumberFormat money=NumberFormat.getIntegerInstance(new Locale("vi","VN"));
 public SelectedSeatAdapter(Actions actions) { this.actions=actions; }
 public void submit(List<Seat> seats,Set<String> matching) {
  this.seats=seats; this.matching=matching; notifyDataSetChanged();
 }
 @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
  return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_seat_row,parent,false));
 }
 @Override public void onBindViewHolder(@NonNull Holder h,int position) {
  Seat s=seats.get(position);
  String type=h.itemView.getContext().getString(s.type==Seat.Type.STANDARD?R.string.filter_standard:s.type==Seat.Type.VIP?R.string.filter_vip:R.string.filter_couple);
  h.title.setText(s.id+" · "+type);
  h.detail.setText(money.format(s.price)+"đ · "+h.itemView.getContext().getString(matching.contains(s.id)?R.string.tap_to_locate:R.string.outside_filter));
  h.itemView.setOnClickListener(v->actions.focus(s));
  h.remove.setContentDescription(h.itemView.getContext().getString(R.string.remove_seat_named,s.id));
  h.remove.setOnClickListener(v->actions.remove(s));
 }
 @Override public int getItemCount() { return seats.size(); }
 static final class Holder extends RecyclerView.ViewHolder {
  final TextView title,detail; final View remove;
  Holder(View view) { super(view); title=view.findViewById(R.id.row_title); detail=view.findViewById(R.id.row_detail); remove=view.findViewById(R.id.remove_seat); }
 }
}
