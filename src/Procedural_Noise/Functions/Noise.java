package Procedural_Noise.Functions;

/**
 * Main Noise class.
 * @authors Ken Perlin, Joshua Aurich, etc.
 *
 */
public class Noise {
	Perlin perlin;
	Improved improved;
	Simplex simplex;	
	FMB_RMF n_p;
	Voronoi v;
	
	public Noise(long seed, int size){
		long start = System.currentTimeMillis();
		perlin = new Perlin();
		System.out.println("Perlin: "+(System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		improved = new Improved();
		System.out.println("Improved: "+(System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		simplex = new Simplex(seed);
		System.out.println("Simplex: "+(System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		n_p = new FMB_RMF(seed);
		System.out.println("FMB_RMF: "+(System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		v = new Voronoi(size,seed,(int)Math.ceil(50f*(size/512f)));
		System.out.println("Voronoi: "+(System.currentTimeMillis() - start));
	}
	
	public void reseed_Simplex(long seed){
		simplex = new Simplex(seed);
	}
	
	public void reseed_Noise(long seed){
		n_p = new FMB_RMF(seed);
	}
	
	public double Simplex_Noise(double xin, double yin, boolean smooth){
		return simplex.Simplex_Noise(xin,yin,smooth);
	}
	
	public double Voronoi(int xin, int yin, int octaves, float gain){
		return v.get(xin,yin,octaves,gain);
	}
	
	public double fbm(double x, double y, double z, int octaves, float lacunarity, float gain){
		return n_p.fBm(x,y,z, octaves,lacunarity,gain);
	}
	
	public double rmf(double x, double y, double z, int octaves, float lacunarity, float gain, float offset){
		return n_p.RidgedMF(x,y, z, octaves, lacunarity, gain, offset);
	}
	
	public double Improved_Noise(double x,double y, double z){
		return improved.ImprovedNoise(x,y,z);
	}
	
	public double Perlin_Noise(float x, float y,float zoom,int octaves,float frequency, float persistence){
		return perlin.Perlin_Noise(x,y,zoom,octaves,frequency, persistence);
	}
	
	/**
	 * Takes in a float and returns a number to be added to a greyscale heightmap image
	 * 
	 * -1< @param f < 1
	 * @return
	 */
	public int convert_Pixel(double f){
		f =(int)(f*128)+128;
		if(f < 0){
			f = 0;
		}
		if(f > 255){
			f = 255;
		}
		return ((int)f << 16) + ((int)f << 8) + (int)f;
	}
	
	/**
	 * Creates height data.
	 * @return
	 */
	public float[] height_data(int tsize){
		int size = tsize;
		float[] f = new float[size*size];
	    float x = 0, y = 0, z = 0, x2 = 0, y2 = 0;	      
	    for (int v = 0; v < size; v++){
	       x += 0.5f;  y = 0; x2 += 0.005f; y2= 0;
	       for (int u = 0; u < size; u++){
		    	  double pixel2 = Voronoi(v,u,7,.1f);
		    	  double temp = ((float)-fbm(x, y, z, 8, 0.45f, 1.0f));
		          double pixel = ((((float)rmf(x2, y2, z, 16, 2.85f, 0.45f, 1.0f))+(.05f*Math.min(temp,-temp))*.5f)*.5f)+(pixel2);
	          f[(int) (v+(u*size))] = (float) (((pixel+1)*.5f)*255);
	          y += 0.5f; y2 += 0.005f;
	       }
	    }
		return f;
	}
	double stripes(double x, double f) {
             double t = .5 + .5 * Math.sin(f * 2*Math.PI * x);
             return t * t - .5;
        }
        double turbulence(double x, double y, double z) {
            double t = -.5;
            for (double f = 1 ; f <= 8 ; f *= 2)
            t += Math.abs(Improved_Noise(f*x,f*y,f*z) / f);
            return t;
            }

	public double fancy_noise(double x, double y, double z){
		x = x *5f;
		y = y *5f;
		z = z *5f;
               // double temp = fbm(x, y, z, 6, 0.45f, 1.0f);
  	//  double pixel2 = Voronoi((int)(x/.005f),(int)(y/.005f),7,.1f);
//	  double temp = fbm(x, y, z, 6, 0.45f, 1.0f);
//      return ((rmf(x, y, z, 4, 2.85f, 0.45f, 1.0f))+(.05d*Math.min(temp,-temp))*.25d);
		//return Improved_Noise(x/3.0f,y/3.0f,z/3.0f);
                //return (-.10 * turbulence(x,y,z))+Improved_Noise(x/3.0f,y/3.0f,z/3.0f);
                //System.out.println((5*.01 * stripes(x + 2*turbulence(x,y,z), 1.6))+(2*-.10 * turbulence(x+5,y+5,z+5))+Improved_Noise(x-2,y-2,z-2));
                //System.out.println(turbulence(x,y,z)-Math.max(0, turbulence(x*.25,y*.25,z*.25))-(0.5*Math.max(turbulence(x*.3,y*.3,z*.3),turbulence(x*.32,y*.32,z*.32))));
                return turbulence(x,y,z)-Math.max(0, turbulence(x*.25,y*.25,z*.25))-(0.5*Math.max(turbulence(x*.3,y*.3,z*.3),turbulence(x*.32,y*.32,z*.32)));//temp;//(rmf(x, y, z, 4, 2.85f, 0.45f, 1.0f));//+(.05f*Math.min(temp,-temp))*.25f);//(5*.01 * stripes(x + 2*turbulence(x,y,z), 1.6))+(2*-.10 * turbulence(x+5,y+5,z+5))+Improved_Noise(x-2,y-2,z-2);
	}
	
	public float fancy_noise(double x, double y){
		x = x *.003f;
		y = y *.003f;
  	//  double pixel2 = Voronoi((int)(x/.005f),(int)(y/.005f),7,.1f);
	  double temp = fbm(x, y, 0, 6, 0.45f, 1.0f);
      return (float) ((rmf(x, y, 0, 4, 2.85f, 0.45f, 1.0f))+(.05f*Math.min(temp,-temp))*.25f);

	}
}
