package com.example.cinemaseatmap.ui;

/** Screen = world * scale + translation. Pure Java so transform math can be verified. */
public final class MapTransform {
    public float scale=1, x, y, fitScale=1;
    private float width, height, worldWidth, worldHeight, padding;
    public void resize(float width,float height,float worldWidth,float worldHeight,float padding) {
        this.width=width; this.height=height; this.worldWidth=worldWidth;
        this.worldHeight=worldHeight; this.padding=padding;
        fitScale=Math.max(.01f,Math.min((width-2*padding)/worldWidth,(height-2*padding)/worldHeight));
        reset();
    }
    public void reset() { scale=fitScale; x=homeX(); y=homeY(); }
    public float homeX() { return (width-worldWidth*fitScale)/2; }
    public float homeY() { return (height-worldHeight*fitScale)/2; }
    public float worldX(float screenX) { return (screenX-x)/scale; }
    public float worldY(float screenY) { return (screenY-y)/scale; }
    public void zoom(float factor,float focusX,float focusY) {
        float wx=worldX(focusX), wy=worldY(focusY);
        scale=Math.max(fitScale,Math.min(fitScale*5,scale*factor));
        x=focusX-wx*scale; y=focusY-wy*scale;
        clamp();
    }
    public void pan(float dx,float dy) { x+=dx; y+=dy; clamp(); }
    public void clamp() {
        float w=worldWidth*scale,h=worldHeight*scale;
        x=w<=width-2*padding ? (width-w)/2 : Math.max(width-w-padding,Math.min(padding,x));
        y=h<=height-2*padding ? (height-h)/2 : Math.max(height-h-padding,Math.min(padding,y));
    }
}
