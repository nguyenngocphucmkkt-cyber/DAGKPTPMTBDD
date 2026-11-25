package com.example.cinemaseatmap.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import com.example.cinemaseatmap.data.MockSeatRepository;
import com.example.cinemaseatmap.model.Seat;
import java.util.Collections;
import java.util.List;

/** A single Canvas; all geometry remains in world coordinates for future hit-testing. */
public final class SeatMapView extends View {
    private static final int ROSE = 0xFFD46D9C, SECTION = 0xFFE7B5D0;
    private static final int TEXT = 0xFFF9EDF5, MUTED = 0xFFAB8CA9;
    private static final String[] ROWS = {"A","B","C","D","E","F","G","H","I","J","K"};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path screen = new Path(), beam = new Path();
    private final Shader beamLight = new LinearGradient(0, 78, 0, 195,
            new int[]{0x40E8A2CC, 0x00E8A2CC}, null, Shader.TileMode.CLAMP);
    private final Shader floorLight = new LinearGradient(0, 0, 0, 1050,
            new int[]{0x66281A35, 0x663A163C}, null, Shader.TileMode.CLAMP);
    private final float[] dust = new float[90];
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private List<Seat> seats = Collections.emptyList();
    private final MapTransform transform = new MapTransform();
    private final java.util.Set<String> selected = new java.util.HashSet<>();
    public interface OnSeatTapListener { void onSeatTap(Seat seat); }
    private OnSeatTapListener listener;
    private final ScaleGestureDetector zoomDetector;
    private final GestureDetector tapDetector;
    private java.util.Set<String> matching;
    private int drawOpacity=255;
    public void setMatchingIds(List<String> ids) { matching=new java.util.HashSet<>(ids); invalidate(); }
    public void focusSeat(Seat s) {
        stopAnimation();
        transform.scale=transform.fitScale*3;
        transform.x=getWidth()/2f-(s.x+s.width/2)*transform.scale;
        transform.y=getHeight()/2f-(s.y+s.height/2)*transform.scale;
        transform.clamp(); invalidate();
    }
    public float getZoomRatio() { return transform.scale/transform.fitScale; }
    public float screenXForWorld(float x) { return x*transform.scale+transform.x; }
    public float screenYForWorld(float y) { return y*transform.scale+transform.y; }
    private final int touchSlop;
    private int activePointer=-1;
    private float lastX,lastY,downX,downY;
    private boolean dragging,multiTouch;
    private ValueAnimator resetAnimator;
    public void setOnSeatTapListener(OnSeatTapListener listener) { this.listener=listener; }
    public void setSelectedIds(List<String> ids) { selected.clear(); selected.addAll(ids); invalidate(); }

    public SeatMapView(Context context) { this(context, null); }
    public SeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setTypeface(medium);
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        zoomDetector=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                transform.zoom(detector.getScaleFactor(),detector.getFocusX(),detector.getFocusY());
                invalidate(); return true;
            }
        });
        tapDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                if(!dragging && !multiTouch) hitTest(e.getX(),e.getY());
                return true;
            }
            @Override public boolean onDoubleTap(MotionEvent e) {
                multiTouch=true;
                if(transform.scale>=transform.fitScale*4.9f) resetView();
                else { transform.zoom(1.6f,e.getX(),e.getY()); invalidate(); }
                return true;
            }
        });
        // Disable platform quick-scale: explicit double-tap zoom is handled above.
        zoomDetector.setQuickScaleEnabled(false);
        setClickable(true);
        java.util.Random random = new java.util.Random(2024);
        for (int i=0; i<dust.length; i+=3) {
            dust[i] = 30 + random.nextFloat()*1100;
            dust[i+1] = 135 + random.nextFloat()*880;
            dust[i+2] = 1 + random.nextFloat()*2;
        }
        screen.moveTo(205, 76); screen.quadTo(580, 20, 955, 76);
        beam.moveTo(205, 78); beam.quadTo(580, 22, 955, 78);
        beam.lineTo(1070, 193); beam.quadTo(580, 132, 90, 193); beam.close();
        setContentDescription("Phòng chiếu 01. 200 đối tượng ghế: 120 thường, 60 VIP, 20 ghế đôi. Ghế đã đặt tím trầm, ghế khóa có dấu chéo.");
    }
    public void setSeats(List<Seat> seats) { this.seats = seats; invalidate(); }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        stopAnimation();
        transform.resize(w,h,MockSeatRepository.WORLD_WIDTH,MockSeatRepository.WORLD_HEIGHT,
                8*getResources().getDisplayMetrics().density);
    }
    public void zoomAtCenter(float factor) {
        stopAnimation(); transform.zoom(factor,getWidth()/2f,getHeight()/2f); invalidate();
    }
    public void resetView() {
        stopAnimation();
        final float startScale=transform.scale,startX=transform.x,startY=transform.y;
        resetAnimator=ValueAnimator.ofFloat(0,1);
        resetAnimator.setDuration(280); resetAnimator.setInterpolator(new DecelerateInterpolator());
        resetAnimator.addUpdateListener(animation -> {
            float t=(float)animation.getAnimatedValue();
            transform.scale=startScale+(transform.fitScale-startScale)*t;
            transform.x=startX+(transform.homeX()-startX)*t;
            transform.y=startY+(transform.homeY()-startY)*t;
            invalidate();
        });
        resetAnimator.start();
    }
    private void stopAnimation() { if(resetAnimator!=null) { resetAnimator.cancel(); resetAnimator=null; } }
    @Override protected void onDetachedFromWindow() { stopAnimation(); super.onDetachedFromWindow(); }
    @Override public boolean performClick() { super.performClick(); return true; }
    @Override public boolean onTouchEvent(MotionEvent e) {
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN) {
            stopAnimation(); getParent().requestDisallowInterceptTouchEvent(true);
            activePointer=e.getPointerId(0); downX=lastX=e.getX(); downY=lastY=e.getY();
            dragging=false; multiTouch=false;
        }
        if(e.getPointerCount()>1) multiTouch=true;
        zoomDetector.onTouchEvent(e);
        if(action==MotionEvent.ACTION_MOVE) {
            int index=e.findPointerIndex(activePointer);
            if(index>=0) {
                float x=e.getX(index),y=e.getY(index);
                if(!dragging && Math.hypot(x-downX,y-downY)>touchSlop) dragging=true;
                if(dragging && e.getPointerCount()==1 && !zoomDetector.isInProgress()) {
                    transform.pan(x-lastX,y-lastY); invalidate();
                }
                lastX=x; lastY=y;
            }
        } else if(action==MotionEvent.ACTION_POINTER_UP) {
            int remaining=e.getActionIndex()==0 ? 1 : 0;
            activePointer=e.getPointerId(remaining); lastX=e.getX(remaining); lastY=e.getY(remaining);
            downX=lastX; downY=lastY; dragging=true;
        } else if(action==MotionEvent.ACTION_UP) {

            activePointer=-1; getParent().requestDisallowInterceptTouchEvent(false);
        } else if(action==MotionEvent.ACTION_CANCEL) {
            activePointer=-1; multiTouch=true; getParent().requestDisallowInterceptTouchEvent(false);
        }
        tapDetector.onTouchEvent(e);
        return true;
    }
    private void hitTest(float x,float y) {
        performClick();
        float wx=transform.worldX(x),wy=transform.worldY(y);
        for(Seat seat:seats) {
            if(matching!=null && !matching.contains(seat.id)) continue;
            if(wx>=seat.x && wx<=seat.x+seat.width && wy>=seat.y && wy<=seat.y+seat.height) {
                if(listener!=null) listener.onSeatTap(seat);
                return;
            }
        }
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int saved = c.save(); c.translate(transform.x,transform.y); c.scale(transform.scale,transform.scale);
        paint.setStyle(Paint.Style.FILL); paint.setShader(floorLight);
        rect.set(8,8,1152,1042); c.drawRoundRect(rect,36,36,paint); paint.setShader(null);
        paint.setStyle(Paint.Style.FILL); color(0x45C594B4);
        for (int i=0; i<dust.length; i+=3) c.drawCircle(dust[i],dust[i+1],dust[i+2],paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2); color(0x334F3555);
        c.drawRoundRect(rect,36,36,paint);
        paint.setStyle(Paint.Style.FILL); paint.setShader(beamLight); c.drawPath(beam,paint); paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND);
        color(0x12F1B3DA); paint.setStrokeWidth(24); c.drawPath(screen,paint);
        color(0x40E8A2CC); paint.setStrokeWidth(12); c.drawPath(screen,paint);
        color(0xFFF2CBE1); paint.setStrokeWidth(4); c.drawPath(screen,paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        text(c,"M À N  H Ì N H",580,120,21,0xFFD2B4CC);
        section(c,"STANDARD", "80.000đ", 185, SECTION);
        section(c,"PREMIUM VIP", "120.000đ", 604, SECTION);
        section(c,"SWEETBOX", "200.000đ / cặp", 853, SECTION);
        for(int i=0;i<ROWS.length;i++) {
            float y=MockSeatRepository.rowY(i)+27;
            text(c,ROWS[i],44,y,21,MUTED); text(c,ROWS[i],1127,y,21,MUTED);
        }
        for(int y=233;y<990;y+=30) {
            paint.setStyle(Paint.Style.FILL); color(0xFF73506E); c.drawCircle(594,y,1.8f,paint);
        }
        for(Seat seat:seats) {
            drawOpacity=matching==null || matching.contains(seat.id) ? 255 : 38;
            drawSeat(c,seat);
        }
        drawOpacity=255;
        text(c,"LỐI VÀO",128,1011,18,MUTED); text(c,"LỐI VÀO",1040,1011,18,MUTED);
        c.restoreToCount(saved);
    }
    private void section(Canvas c,String title,String price,float y,int color) {
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.5f); color(0x665F3A5B);
        c.drawLine(80,y-8,350,y-8,paint); c.drawLine(810,y-8,1110,y-8,paint);
        text(c,title,580,y-8,22,color); text(c,price,580,y+17,18,MUTED);
    }
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color) {
        paint.setStyle(Paint.Style.FILL); color(color); rect.set(l,t,r,b);
        c.drawRoundRect(rect,radius,radius,paint);
    }
    private void drawSeat(Canvas c,Seat s) {
        boolean available=s.status==Seat.Status.AVAILABLE;
        boolean isSelected=selected.contains(s.id);
        int color=isSelected ? 0xFFFFE5F2 : available
                ? (s.type==Seat.Type.STANDARD ? 0xFFB95683 : s.type==Seat.Type.VIP ? 0xFFDA85AB : 0xFFF1B7D1)
                : s.status==Seat.Status.BOOKED ? 0xFF665264 : 0xFF271D2C;
        float x=s.x,y=s.y,w=s.width;
        // Layered backrest, cushion and armrests stay inside the object's bounds.
        round(c,x+2,y+5,x+w-2,y+42,7,0xFF1B1024);
        round(c,x+3,y,x+w-3,y+31,7,color);
        round(c,x+5,y+25,x+w-5,y+39,5,color);
        color(available?0x665A2847:0xFF2C1830); paint.setStrokeWidth(2);
        c.drawLine(x+6,y+25,x+w-6,y+25,paint);
        round(c,x,y+16,x+5,y+36,2,color); round(c,x+w-5,y+16,x+w,y+36,2,color);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.5f);
        color(available ? (s.type==Seat.Type.VIP ? 0xFFF2CBE1 : 0x66FFFFFF) : 0x446A4668);
        paint.setStrokeWidth(s.type==Seat.Type.VIP ? 2.8f : 1.5f);
        rect.set(x+5,y+2,x+w-5,y+22); c.drawRoundRect(rect,5,5,paint);
        if(s.type==Seat.Type.COUPLE) {
            color(0x665A2847); paint.setStrokeWidth(2);
            c.drawLine(x+w/2,y+5,x+w/2,y+34,paint);
        }
        if(isSelected) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(3); color(0xFFFFF7FC);
            rect.set(x-1,y-1,x+w+1,y+41); c.drawRoundRect(rect,8,8,paint);
        }
        if(s.status==Seat.Status.LOCKED) {
            color(0xFF9C7C9A); paint.setStrokeWidth(2.5f);
            float cx=x+w/2,cy=y+14;
            c.drawLine(cx-4,cy-4,cx+4,cy+4,paint); c.drawLine(cx+4,cy-4,cx-4,cy+4,paint);
        } else if(isSelected) {
            color(0xFF74314F); paint.setStrokeWidth(3);
            float cx=x+w/2;
            c.drawLine(cx-5,y+12,cx-1,y+17,paint); c.drawLine(cx-1,y+17,cx+6,y+8,paint);
        } else text(c,s.id,x+w/2,y+17,13,
                available && s.type!=Seat.Type.STANDARD ? 0xFF321B30 : 0xFFF4DFED);
    }
    private void color(int value) {
        paint.setColor((value & 0x00FFFFFF) | ((Color.alpha(value)*drawOpacity/255)<<24));
    }
    private void text(Canvas c,String value,float x,float y,float size,int color) {
        paint.setStyle(Paint.Style.FILL); color(color); paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size); c.drawText(value,x,y,paint);
    }
}
