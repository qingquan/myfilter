package lbs.wifiparticlefilter.application;

import java.util.ArrayList;
import java.util.List;

import lbs.wifiparticlefilter.data.Particle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class DrawView extends View
{
	/** tag for debugging */
	private static final String TAG = DrawView.class.getSimpleName();
	/** background colour */
	public static final int BG = Color.BLACK;
	/** particle colour */
	public static final int FG = Color.BLUE;
	/** list of all particles to be drawn */
	private List<Particle> particles = new ArrayList<Particle>();
	/** Paints for bg and fg */
	private Paint background = new Paint();
    private Paint paint = new Paint();
    //canvas size
    private int height = 0;
    private int width = 0;
    //coordinate scaling
    private int[] border = new int[4];
    private double xScale = 1.0;
    private double yScale = 1.0;

    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        background.setColor(BG);
        paint.setColor(FG);
        paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
    	//update size
    	height = canvas.getHeight();
    	width = canvas.getWidth();
    	//update scaling
    	this.xScale = width / (double)(border[1]);
    	this.yScale = height / (double)(border[3]);
    	//draws background plane
    	canvas.drawPaint(background);
    	//draws all particles
        for (Particle p : particles)
        	canvas.drawCircle((int)Math.round(p.getX() * this.xScale), (int)Math.round(p.getY() * this.yScale), 5, paint);
    }
    
    /**
     * setter for new particles
     * @param particles the particles
     */
    public void setParticles(List<Particle> particles)
    {
    	this.particles = particles;
    }
    
    /**
     * specify the bounding box of the particles
     * @param border the x/y-min/max
     */
    public void setBorder(int[] border)
    {
    	this.border = border;
    }
    
    /**
     * invalidates the view so it gets redrawn
     */
    public void drawParticles()
    {
    	invalidate();
    }
    
    /**
     * returns the canvas width
     * @return the width
     */
    public int getCanvasWidth()
    {
    	return width;
    }
    
    /**
     * returns the canvas height
     * @return the height
     */
    public int getCanvasHeight()
    {
    	return height;
    }
}