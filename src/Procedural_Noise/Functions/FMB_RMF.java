package Procedural_Noise.Functions;

import java.util.Random;

/**
 * Special thanks to:
 * http://britonia-game.com/?p=60
 * 
 * @author jda1
 *
 */
public class FMB_RMF {

	         private static Random rand;  
	   
	         private static int[] ms_p = new int[512];  
	
	         public FMB_RMF(long seed){
	        	// Create an instance of the random number generator, making sure to pass in a seed.
	        	 rand = new Random(seed);

	        	 // Use the bitwise left shift operator to make sure the array of permutation values is a multiple of 2
	        	 int nbVals = (1 << 8); // result is 256
	        	 int[] ms_perm = new int[nbVals];

	        	 // set values in temp perm array as "unused", denoted by -1
	        	 for (int i = 0; i < nbVals; i++)
	        	 {
	        	 ms_perm[i] = -1;
	        	 }

	        	 for (int i = 0; i < nbVals; i++)
	        	 {
	        	 // for each value, find an empty spot, and place it in it
	        	 while (true)
	        	 {
	        	 // generate rand # with max a nbvals
	        	 int p = rand.nextInt(256);
	        	 if (ms_perm[p] == -1)
	        	 {
	        	 ms_perm[p] = i;
	        	 break;
	        	 }
	        	 }
	        	 }

	        	 for (int i = 0; i < nbVals; i++)
	        	 {
	        	 ms_p[nbVals + i] = ms_p[i] = ms_perm[i];
	        	 }

	         }
	
	    private static double noise (double x, double y, double z)  
	   {  
	    	int X = (int)x & 255;
	    	int Y = (int)y & 255;
	    	int Z = (int)z & 255;
	    	 x -= Math.floor(x);  
	    	 y -= Math.floor(y);  
	    	 z -= Math.floor(z);  
	    	 double u = fade(x);
	    	 double v = fade(y);
	    	 double w = fade(z);
	    	 int A = ms_p[X    ] + Y, AA = ms_p[A] + Z, AB = ms_p[A + 1] + Z;
	    	 int B = ms_p[X + 1] + Y, BA = ms_p[B] + Z, BB = ms_p[B + 1] + Z;
	    	 return lerp(w, lerp(v, lerp(u, grad(ms_p[AA  ], x  , y  , z   ),
                     grad(ms_p[BA  ], x-1, y  , z   )),
            lerp(u, grad(ms_p[AB  ], x  , y-1, z   ),
                     grad(ms_p[BB  ], x-1, y-1, z   ))),
  lerp(v, lerp(u, grad(ms_p[AA+1], x  , y  , z-1 ),
                     grad(ms_p[BA+1], x-1, y  , z-1 )),
            lerp(u, grad(ms_p[AB+1], x  , y-1, z-1 ),
                     grad(ms_p[BB+1], x-1, y-1, z-1 ))));

	    }  
	    
	    
	    /////MATH FUNCTIONS
	    private static double fade(double t)
	    {
	    return (t * t * t * (t * (t * 6 - 15) + 10));
	    }
	    private static double lerp(double t, double a, double b)
	    {
	    return (a + t * (b - a));
	    }
	    private static double grad(int hash, double x, double y, double z)
	    {
	    int h = hash & 15;
	    double u = h<8 ? x : y;
	    double v = h<4 ? y : h==12||h==14 ? x : z;

	    return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
	    }

	////FBM or RIGID
        public static double ridge(double h, float offset)
        {
            h = Math.abs(h);
            h = offset - h;
            h = h * h;
            return h;
        }

        public static double RidgedMF(double x, double y, double z, int octaves, float lacunarity, float gain, float offset)
        {
            double sum = 0;
            float amplitude = 0.5f;
            float frequency = 1.0f;
            double prev = 1.0f;

            for (int i = 0; i < octaves; i++)
            {
                double n = ridge(noise(x * frequency, y * frequency, z * frequency), offset);
                sum += n * amplitude * prev;
                prev = n;
                frequency *= lacunarity;
                amplitude *= gain;
            }

            return sum;
        }

        public static double fBm(double x, double y, double z, int octaves, float lacunarity, float gain)
        {
            double frequency = 1.0f;
            double amplitude = 0.5f;
            double sum = 0.0f;

            for (int i = 0; i < octaves; i++)
            {
                sum += noise(x * frequency, y * frequency, z * frequency) * amplitude;
                frequency *= lacunarity;
                amplitude *= gain;
            }
            return sum;
        }
    

}
