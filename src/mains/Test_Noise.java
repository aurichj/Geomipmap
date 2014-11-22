package mains;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import Procedural_Noise.Functions.Noise;

/**
 * Generates a .png image of the noise functions specified.
 * File is written to the root directory of this project.
 * @author Joshua Aurich
 *
 */
public class Test_Noise {	
	private static BufferedImage	image;
	static int sizex= 1024; static int sizey = sizex;
	static long seed = 945435435;
	static Noise n = new Noise(seed,sizex);
	static double[][] f = new double[sizex][sizex];
	
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		System.out.println("start");
		n.reseed_Simplex(seed);
		image = new BufferedImage(sizex, sizey, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
		
		//FBM + RMF
		long start1 = System.nanoTime();
	    float x = 0, y = 0, z = 0, x2 = 0, y2 = 0;	      
	    for (int v = 0; v < sizex; v++){
	       x += .1f;  y = 0; x2 += 0.003f; y2= 0;
	       for (int u = 0; u < sizex; u++){ 
	    	  double pixel = n.fbm(x, y, z, 6, 0.55f, .9f);
	          double pixel2 = (((n.rmf(x2, y2, z, 3, 2.85f, 0.55f, 1.0f))))-.5f;
	    	  // double pixel = n.Improved_Noise(x2,y2,0);
	    	   //double pixel =  n.Voronoi(v,u,6,.5f);
	          f[u][v] = pixel2*.9f+pixel*.1f;
	          y += .1f; y2 += 0.003f;   
	       }
	    }
		System.out.println("Produce: "+((System.nanoTime() - start1)/1000000f)+" Milliseconds");
	    
	    for(int y1 = 0; y1 < sizey; y1++){
		    for(int x1 = 0; x1 < sizex; x1++){
		          image.setRGB(x1, y1, n.convert_Pixel(f[x1][y1]));	
		    }
	    }
		g.dispose();
		try{ImageIO.write(image, "png", new File("noise_out.png"));}catch (IOException e){e.printStackTrace();}	
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("done");


    }
}
