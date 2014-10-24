package cc.kenai.mainlib.face;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

public class XFace {
	public final static int MAX_WIDTH=800;
	public final static int MAX_HEIGHT=600;
	
	
	public final static MyFace xCatchFace(Bitmap original, boolean isScaled,
			int maxFaces) throws NoresultException, NoResourseException {
		float scale = 1f;
		FaceDetector.Face[] faces = null;
		if (original == null) {
			throw new NoResourseException();
		}
		
		// step 2 : scaled and search faces
		if (isScaled && original.getHeight() > MAX_HEIGHT
				&& original.getHeight() > MAX_WIDTH) {
			scale = original.getWidth() / (float) MAX_WIDTH;
			Bitmap b = Bitmap.createScaledBitmap(original, MAX_WIDTH, MAX_WIDTH
					* original.getHeight() / original.getWidth(), true);
			if (b.isMutable() && b.getConfig() == Config.RGB_565) {
				faces = getFace(b, maxFaces);
				b.recycle();
			} else {
				Bitmap loc = b.copy(Config.RGB_565, true);
				b.recycle();
				faces = getFace(loc, maxFaces);
				loc.recycle();
			}
		} else {
			if (original.isMutable() && original.getConfig() == Config.RGB_565) {
				faces = getFace(original, maxFaces);
			} else {
				Bitmap loc = original.copy(Config.RGB_565, true);
				faces = getFace(loc, maxFaces);
				loc.recycle();
			}
		}
		if (faces == null) {
			throw new NoresultException();
		} else {
			return new MyFace(faces, scale);
		}
	}
	
	/**
	 * 
	 * @param original
	 * @param isScaled
	 * @param maxFaces
	 * @return : {x1,y1,x2,y2...}
	 * @throws NoresultException 
	 * @throws NoResourseException 
	 */
	public final static float[] xCatchFaceMidpoint(Bitmap original,
			boolean isScaled, int maxFaces) throws NoresultException, NoResourseException {
		// step 1 : necessary
		float scale = 1f;
		FaceDetector.Face[] faces = null;
		if (original == null) {
			throw new NoResourseException();
		}
		// step 2 : scaled and search faces
		if (isScaled && original.getHeight() > MAX_HEIGHT
				&& original.getHeight() > MAX_WIDTH) {
			scale = original.getWidth() / (float) MAX_WIDTH;
			Bitmap b = Bitmap.createScaledBitmap(original, MAX_WIDTH, MAX_WIDTH
					* original.getHeight() / original.getWidth(), true);
			if (b.isMutable() && b.getConfig() == Config.RGB_565) {
				faces = getFace(b, maxFaces);
				b.recycle();
			} else {
				Bitmap loc = b.copy(Config.RGB_565, true);
				b.recycle();
				faces = getFace(loc, maxFaces);
				loc.recycle();
			}
		} else {
			if (original.isMutable() && original.getConfig() == Config.RGB_565) {
				faces = getFace(original, maxFaces);
			} else {
				Bitmap loc = original.copy(Config.RGB_565, true);
				faces = getFace(loc, maxFaces);
				loc.recycle();
			}
		}

		// step 3 : return result;
		if (faces == null) {
			throw new NoresultException();
		} else {
			PointF midpoint = new PointF();
			int n = faces.length;
			float[] result = new float[2 * n];
			for (int i = 0; i < n; i++) {
				try {
					faces[i].getMidPoint(midpoint);
					result[2 * i] = midpoint.x * scale;
					result[2 * i + 1] = midpoint.y * scale;

				} catch (Exception e) {
					float[] newresult = new float[2 * i];
					for (int j = 0; j < 2 * i; j++) {
						newresult[j] = result[j];
					}
					return newresult;
				}
			}
			return result;
		}

	}

	private static FaceDetector.Face[] getFace(Bitmap deal, int maxFaces) {
		FaceDetector fd;
		FaceDetector.Face[] faces = new FaceDetector.Face[maxFaces];
		int count = 0;
		try {
			fd = new FaceDetector(deal.getWidth(), deal.getHeight(), maxFaces);
			count = fd.findFaces(deal, faces);
		} catch (Exception e) {
			return null;
		}
		if (count > 0) {
			return faces;
		} else {
			return null;
		}

	}
	
	public final static class NoresultException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
	public final static class NoResourseException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
	public final static class MyFace{
		public final Face[] faces;
		public final float scale;
		public MyFace(Face[] faces,float scale){
			this.faces=faces;
			this.scale=scale;
		}
	}
	public final static void drawRect(Canvas canvas,float cx,float cy,float radius){
		Paint p=new Paint();
		
		p.setColor(Color.rgb(50, 180, 235));
		p.setStrokeWidth(Math.max(8f, radius/23));
		p.setStyle(Paint.Style.STROKE);
		
		RectF r=new RectF(cx-radius, cy-radius, cx+radius, cy+radius);
//		canvas.s
		canvas.drawRect(r,p);
	}

}
