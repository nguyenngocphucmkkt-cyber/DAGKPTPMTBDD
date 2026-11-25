package com.example.cinemaseatmap;
import org.junit.Test;
import static org.junit.Assert.*;
import com.example.cinemaseatmap.ui.MapTransform;
import com.example.cinemaseatmap.data.MockSeatRepository;
import com.example.cinemaseatmap.model.Seat;
public class MapTransformTest {
 @Test public void zoomPreservesFocusAndBounds() {
  MapTransform t=new MapTransform(); t.resize(400,600,1160,1050,8);
  t.zoom(2,200,300);
  float wx=t.worldX(180),wy=t.worldY(270);
  t.zoom(1.2f,180,270);
  assertEquals(wx,t.worldX(180),.001f); assertEquals(wy,t.worldY(270),.001f);
  t.zoom(100,200,300); assertEquals(t.fitScale*5,t.scale,.001f);
  t.pan(100000,100000); assertEquals(8,t.x,.001f); assertEquals(8,t.y,.001f);
  t.pan(-100000,-100000);
  assertEquals(400-1160*t.scale-8,t.x,.001f);
  assertEquals(600-1050*t.scale-8,t.y,.001f);
  t.zoom(.00001f,200,300); assertEquals(t.fitScale,t.scale,.001f);
 }
 @Test public void seatEdgesRoundTripAfterZoomAndPan() {
  MapTransform t=new MapTransform(); t.resize(400,600,1160,1050,8);
  t.zoom(4,200,300); t.pan(-135,80);
  for(Seat s:MockSeatRepository.createSeats()) {
   float wx=s.x+.01f,wy=s.y+s.height-.01f;
   float sx=wx*t.scale+t.x,sy=wy*t.scale+t.y;
   assertEquals(wx,t.worldX(sx),.001f); assertEquals(wy,t.worldY(sy),.001f);
  }
  t.reset(); assertEquals(t.fitScale,t.scale,.001f);
  assertEquals(t.homeX(),t.x,.001f); assertEquals(t.homeY(),t.y,.001f);
 }
}
