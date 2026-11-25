package com.example.cinemaseatmap;

import android.os.SystemClock;
import android.view.MotionEvent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.lifecycle.ViewModelProvider;
import com.example.cinemaseatmap.ui.SeatMapView;
import com.example.cinemaseatmap.ui.SeatSelectionViewModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SeatGestureTest {
 @Test public void pinchZoomsWithoutSelectingSeats() {
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
   scenario.onActivity(activity->{
    SeatMapView map=activity.findViewById(R.id.seat_map);
    assertTrue(map.getWidth()>0);
    float cx=map.getWidth()/2f,cy=map.getHeight()/2f;
    long down=SystemClock.uptimeMillis();
    dispatch(map,down,down,MotionEvent.ACTION_DOWN,cx,cy,60,1);
    dispatch(map,down,down+16,MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),cx,cy,60,2);
    for(int i=1;i<=10;i++) dispatch(map,down,down+16+i*16,MotionEvent.ACTION_MOVE,cx,cy,60+i*24,2);
    dispatch(map,down,down+200,MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),cx,cy,300,2);
    dispatch(map,down,down+216,MotionEvent.ACTION_UP,cx,cy,300,1);
    assertTrue("Pinch must zoom beyond fit",map.getZoomRatio()>1.3f);
    assertTrue(new ViewModelProvider(activity).get(SeatSelectionViewModel.class).selectedIds().isEmpty());
   });
  }
 }
 @Test public void doubleTapZoomsWithoutSelecting() {
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
   scenario.onActivity(activity->{
    SeatMapView map=activity.findViewById(R.id.seat_map);
    float x=map.getWidth()/2f,y=map.getHeight()/2f;
    long t=SystemClock.uptimeMillis();
    tap(map,t,x,y); tap(map,t+100,x,y);
    assertTrue(map.getZoomRatio()>1.5f);
    assertTrue(new ViewModelProvider(activity).get(SeatSelectionViewModel.class).selectedIds().isEmpty());
   });
  }
 }
 @Test public void filterAndSelectionSurviveRecreation() {
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
   scenario.onActivity(a->{
    SeatSelectionViewModel vm=new ViewModelProvider(a).get(SeatSelectionViewModel.class);
    vm.toggle(vm.seats.get(2));
    vm.setFilter(com.example.cinemaseatmap.model.SeatFilter.Mode.VIP);
    vm.setQuery("G");
   });
   scenario.recreate();
   scenario.onActivity(a->{
    SeatSelectionViewModel vm=new ViewModelProvider(a).get(SeatSelectionViewModel.class);
    assertEquals(1,vm.selectedIds().size()); assertEquals("A3",vm.selectedIds().get(0));
    assertEquals(com.example.cinemaseatmap.model.SeatFilter.Mode.VIP,vm.filter);
    assertEquals("G",vm.query);
    assertEquals(20,vm.matchingIds().size());
    assertEquals(1,((androidx.recyclerview.widget.RecyclerView)a.findViewById(R.id.selected_list)).getAdapter().getItemCount());
   });
  }
 }
 @Test public void sheetFocusAndRemoveSynchronizeCanvas() {
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
   scenario.onActivity(a->{
    SeatSelectionViewModel vm=new ViewModelProvider(a).get(SeatSelectionViewModel.class);
    vm.toggle(vm.seats.get(2));
    vm.setFilter(com.example.cinemaseatmap.model.SeatFilter.Mode.VIP);
    com.google.android.material.bottomsheet.BottomSheetBehavior.from(a.findViewById(R.id.selection_sheet))
       .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
   });
   SystemClock.sleep(500);
   scenario.onActivity(a->{
    SeatSelectionViewModel vm=new ViewModelProvider(a).get(SeatSelectionViewModel.class);
    androidx.recyclerview.widget.RecyclerView list=a.findViewById(R.id.selected_list);
    androidx.recyclerview.widget.RecyclerView.ViewHolder holder=list.findViewHolderForAdapterPosition(0);
    assertNotNull(holder);
    holder.itemView.performClick();
    assertEquals(com.example.cinemaseatmap.model.SeatFilter.Mode.ALL,vm.filter);
    assertEquals(3f,((SeatMapView)a.findViewById(R.id.seat_map)).getZoomRatio(),.01f);
    assertEquals(1,vm.selectedIds().size());
    holder.itemView.findViewById(R.id.remove_seat).performClick();
    assertTrue(vm.selectedIds().isEmpty()); assertEquals(0,vm.total());
   });
  }
 }
 private static void tap(SeatMapView map,long t,float x,float y) {
  MotionEvent d=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
  map.dispatchTouchEvent(d); d.recycle();
  MotionEvent u=MotionEvent.obtain(t,t+30,MotionEvent.ACTION_UP,x,y,0);
  map.dispatchTouchEvent(u); u.recycle();
 }
 private static void dispatch(SeatMapView map,long down,long time,int action,float cx,float cy,float halfSpan,int count) {
  MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[count];
  MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[count];
  for(int i=0;i<count;i++) {
   props[i]=new MotionEvent.PointerProperties(); props[i].id=i; props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;
   coords[i]=new MotionEvent.PointerCoords(); coords[i].x=cx+(i==0?-halfSpan:halfSpan); coords[i].y=cy; coords[i].pressure=1; coords[i].size=1;
  }
  MotionEvent e=MotionEvent.obtain(down,time,action,count,props,coords,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);
  map.dispatchTouchEvent(e); e.recycle();
 }
}
