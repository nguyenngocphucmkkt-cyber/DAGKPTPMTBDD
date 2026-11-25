package com.example.cinemaseatmap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.ChipGroup;
import com.example.cinemaseatmap.model.*;
import com.example.cinemaseatmap.ui.*;
import java.util.*;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {
    private SeatSelectionViewModel model;
    private BottomSheetBehavior<View> sheet;
    private final int[] filterIds={R.id.filter_all,R.id.filter_standard,R.id.filter_vip,R.id.filter_couple,R.id.filter_available};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        WindowInsetsControllerCompat bars=new WindowInsetsControllerCompat(getWindow(),getWindow().getDecorView());
        bars.setAppearanceLightStatusBars(false); bars.setAppearanceLightNavigationBars(false);
        View root=findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)->{
            Insets system=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(system.left,system.top,system.right,system.bottom);
            return insets;
        });
        model=new ViewModelProvider(this).get(SeatSelectionViewModel.class);
        if(savedInstanceState!=null) {
            try { model.filter=SeatFilter.Mode.valueOf(savedInstanceState.getString("filter","ALL")); }
            catch(IllegalArgumentException ignored) { model.filter=SeatFilter.Mode.ALL; }
            model.query=savedInstanceState.getString("query","");
            model.sheetExpanded=savedInstanceState.getBoolean("sheet_expanded",false);
            ArrayList<String> ids=savedInstanceState.getStringArrayList("selected_seats");
            if(ids!=null) model.restore(ids);
        }
        SeatMapView map=findViewById(R.id.seat_map);
        map.setSeats(model.seats);
        map.setOnSeatTapListener(seat->{
            if(seat.status!=Seat.Status.AVAILABLE) Toast.makeText(this,
                    seat.status==Seat.Status.BOOKED?R.string.seat_booked:R.string.seat_locked,Toast.LENGTH_SHORT).show();
            else model.toggle(seat);
        });
        findViewById(R.id.reset_view).setOnClickListener(v->map.resetView());
        findViewById(R.id.zoom_in).setOnClickListener(v->map.zoomAtCenter(1.5f));
        findViewById(R.id.zoom_out).setOnClickListener(v->map.zoomAtCenter(1/1.5f));

        View sheetView=findViewById(R.id.selection_sheet);
        sheet=BottomSheetBehavior.from(sheetView);
        sheet.setHideable(false); sheet.setFitToContents(true);
        final boolean restoreExpanded=model.sheetExpanded;
        Button toggle=findViewById(R.id.toggle_sheet);
        View.OnClickListener toggleSheet=v->sheet.setState(sheet.getState()==BottomSheetBehavior.STATE_EXPANDED
                ? BottomSheetBehavior.STATE_COLLAPSED:BottomSheetBehavior.STATE_EXPANDED);
        toggle.setOnClickListener(toggleSheet);
        findViewById(R.id.sheet_header).setOnClickListener(toggleSheet);
        sheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(View view,int state) {
                if(state==BottomSheetBehavior.STATE_EXPANDED || state==BottomSheetBehavior.STATE_COLLAPSED) {
                    model.sheetExpanded=state==BottomSheetBehavior.STATE_EXPANDED;
                    toggle.setText(model.sheetExpanded?R.string.hide_details:R.string.show_details);
                }
            }
            @Override public void onSlide(View view,float offset) { }
        });
        // Expanded sheet occupies 55% at most; the upper map stays available.
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            int available=root.getHeight()-root.getPaddingTop()-root.getPaddingBottom();
            int desired=Math.max(dp(150),(int)(available*.55f));
            if(sheetView.getLayoutParams().height!=desired) {
                android.view.ViewGroup.LayoutParams params=sheetView.getLayoutParams();
                params.height=desired; sheetView.setLayoutParams(params);
            }
        });
        root.post(()->sheet.setState(restoreExpanded?BottomSheetBehavior.STATE_EXPANDED:BottomSheetBehavior.STATE_COLLAPSED));

        EditText query=findViewById(R.id.seat_search);
        ChipGroup group=findViewById(R.id.filter_group);
        group.setOnCheckedStateChangeListener((g,ids)->{
            if(!ids.isEmpty()) for(int i=0;i<filterIds.length;i++)
                if(ids.get(0)==filterIds[i]) model.setFilter(SeatFilter.Mode.values()[i]);
        });
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) { model.setQuery(s.toString()); }
            @Override public void afterTextChanged(Editable text) { }
        });
        findViewById(R.id.clear_filters).setOnClickListener(v->{ model.clearFilters(); closeKeyboard(query); });
        RecyclerView list=findViewById(R.id.selected_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        SelectedSeatAdapter adapter=new SelectedSeatAdapter(new SelectedSeatAdapter.Actions() {
            @Override public void focus(Seat seat) {
                model.clearFilters(); closeKeyboard(query);
                sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                map.focusSeat(seat);
            }
            @Override public void remove(Seat seat) { model.remove(seat.id); }
        });
        list.setAdapter(adapter);
        TextView summary=findViewById(R.id.selection_summary);
        TextView filterStatus=findViewById(R.id.filter_status);
        NumberFormat money=NumberFormat.getIntegerInstance(new Locale("vi","VN"));
        int[] filterLabels={R.string.filter_all,R.string.filter_standard,R.string.filter_vip,R.string.filter_couple,R.string.filter_available};
        model.selection().observe(this,ids->{
            List<String> matching=model.matchingIds();
            map.setSelectedIds(ids); map.setMatchingIds(matching);
            summary.setText(getString(R.string.selection_summary,ids.size(),money.format(model.total())));
            Set<String> matchedSet=new HashSet<>(matching);
            int outside=0; for(String id:ids) if(!matchedSet.contains(id)) outside++;
            String label=getString(filterLabels[model.filter.ordinal()]);
            if(!model.query.trim().isEmpty()) label+=" · "+model.query.trim().toUpperCase(Locale.ROOT);
            filterStatus.setText(getString(R.string.filter_status,label,matching.size(),outside));
            findViewById(R.id.no_results).setVisibility(matching.isEmpty()?View.VISIBLE:View.GONE);
            findViewById(R.id.selection_empty).setVisibility(ids.isEmpty()?View.VISIBLE:View.GONE);
            adapter.submit(model.selectedSeats(),matchedSet);
            group.check(filterIds[model.filter.ordinal()]);
            if(!query.getText().toString().equals(model.query)) query.setText(model.query);
        });
    }
    private void closeKeyboard(View source) {
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(source.getWindowToken(),0);
        source.clearFocus();
    }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putStringArrayList("selected_seats",model.selectedIds());
        state.putString("filter",model.filter.name()); state.putString("query",model.query);
        state.putBoolean("sheet_expanded",model.sheetExpanded);
        super.onSaveInstanceState(state);
    }
}
