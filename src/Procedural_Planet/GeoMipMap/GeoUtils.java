/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Procedural_Planet.GeoMipMap;

import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.IntBuffer;

/**
 *
 * @author Josh
 */
public class GeoUtils {
    public static IntBuffer[] indexArray0 = new IntBuffer[16];
    public static IntBuffer[] indexArray1 = new IntBuffer[16]; //reversed for sides 0,3,and 1
    //Optimization ints 9x9
    /**public static int level_width = 9;
    public static int triangle_count = 128;
    public static int TC_M3 = 384; //triangle count x3
    public static int LW_S1 = 8; //level width -1
    public static int LW_S2 = 7; //level width -2
    public static int LW_S3 = 6; //level width -3
    public static int LW_MLW = 81; //level width * level width
    public static int LW_MLW_S1 = 80; //level width * level width -1
    public static int LW_ALW_S1 = 17; //level width + level width -1
    public static int LW_ALW_S2 = 16; //level width + level width -2
    public static int LW_A1 = 10; //level width +1
    public static int LW_MCLWS1 = 72; //level width * (level width -1)*/
    //Optimization ints 33x33
    public static int level_width = 33;
    public static int triangle_count = 2048;
    public static int TC_M3 = 6144; //triangle count x3
    public static int LW_S1 = 32; //level width -1
    public static int LW_S2 = 31; //level width -2
    public static int LW_S3 = 30; //level width -3
    public static int LW_MLW = 1089; //level width * level width
    public static int LW_MLW_S1 = 1088; //level width * level width -1
    public static int LW_ALW_S1 = 65; //level width + level width -1
    public static int LW_ALW_S2 = 64; //level width + level width -2
    public static int LW_A1 = 34; //level width +1
    public static int LW_MCLWS1 = 1056; //level width * (level width -1)


    public static IntBuffer getIndex(int Side, boolean form_to_west, boolean form_to_south, boolean form_to_east, boolean form_to_north){
        int demux = 0;

        if(form_to_west){
            demux+=8;
        }
        if(form_to_south){
            demux+=4;
        }
        if(form_to_east){
            demux+=2;
        }
        if(form_to_north){
            demux+=1;
        }
        if(Side == 0 || Side == 1 || Side == 3){
            return indexArray1[demux];
        }else{
            return indexArray0[demux];
        }
    }

    /**
     *          D
     *      |--------|
     *      |        |
     *   A  |        |C
     *      |________|
     *          B
     *  Truth table for following set up, where high indicates side matching
     */
    public static void prepareIndex(){
        indexArray0[0] = createIndex(2,true,true,true,true);
        indexArray1[0] = createIndex(1,true,true,true,true);
        indexArray0[1] = createIndex(2,true,true,true,false);
        indexArray1[1] = createIndex(1,true,true,true,false);
        indexArray0[2] = createIndex(2,true,true,false,true);
        indexArray1[2] = createIndex(1,true,true,false,true);
        indexArray0[3] = createIndex(2,true,true,false,false);
        indexArray1[3] = createIndex(1,true,true,false,false);
        indexArray0[4] = createIndex(2,true,false,true,true);
        indexArray1[4] = createIndex(1,true,false,true,true);
        indexArray0[5] = createIndex(2,true,false,true,false);
        indexArray1[5] = createIndex(1,true,false,true,false);
        indexArray0[6] = createIndex(2,true,false,false,true);
        indexArray1[6] = createIndex(1,true,false,false,true);
        indexArray0[7] = createIndex(2,true,false,false,false);
        indexArray1[7] = createIndex(1,true,false,false,false);
        indexArray0[8] = createIndex(2,false,true,true,true);
        indexArray1[8] = createIndex(1,false,true,true,true);
        indexArray0[9] = createIndex(2,false,true,true,false);
        indexArray1[9] = createIndex(1,false,true,true,false);
        indexArray0[10] = createIndex(2,false,true,false,true);
        indexArray1[10] = createIndex(1,false,true,false,true);
        indexArray0[11] = createIndex(2,false,true,false,false);
        indexArray1[11] = createIndex(1,false,true,false,false);
        indexArray0[12] = createIndex(2,false,false,true,true);
        indexArray1[12] = createIndex(1,false,false,true,true);
        indexArray0[13] = createIndex(2,false,false,true,false);
        indexArray1[13] = createIndex(1,false,false,true,false);
        indexArray0[14] = createIndex(2,false,false,false,true);
        indexArray1[14] = createIndex(1,false,false,false,true);
        indexArray0[15] = createIndex(2,false,false,false,false);
        indexArray1[15] = createIndex(1,false,false,false,false);
    }

    private static IntBuffer createIndex(int Side, boolean form_to_west, boolean form_to_south, boolean form_to_east, boolean form_to_north){
		IntBuffer index = BufferUtils.createIntBuffer(GeoUtils.TC_M3);

		if(form_to_south  && form_to_north && form_to_west && form_to_east){
			int startX = 0;
			int maxX = GeoUtils.LW_S1;
			int difference = GeoUtils.level_width;
                        boolean yOdd = false;
                        boolean xOdd = false;
			for(int y = 0; y < GeoUtils.LW_S1; y++){
				for(int x = startX; x < maxX; x++){
                                    if(!yOdd){
                                        if(!xOdd){
                                            index_static(index,Side,x+difference,x,x+1);
                                            index_static(index,Side,x+difference,x+1,x+difference+1);
                                        }else{
                                            index_static(index,Side,x+difference,x,x+difference+1);
                                            index_static(index,Side,x,x+1,x+difference+1);
                                        }
                                    }else{
                                        if(!xOdd){
                                            index_static(index,Side,x+difference,x,x+difference+1);
                                            index_static(index,Side,x,x+1,x+difference+1);
                                        }else{
                                            index_static(index,Side,x+difference,x,x+1);
                                            index_static(index,Side,x+difference,x+1,x+difference+1);
                                        }
                                    }
                                        xOdd = !xOdd;
				}
				startX += difference;
				maxX += difference;
                                yOdd = !yOdd;
			}
		}else{
			//build the inner core of this block
			int startX = GeoUtils.LW_A1;
			int maxX = (int) (startX+GeoUtils.LW_S3);
			int difference = GeoUtils.level_width;
                        boolean yOdd = true;
                        boolean xOdd = true;
			for(int y = 2; y < GeoUtils.LW_S1; y++){
				for(int x = startX; x < maxX; x++){
				if(!yOdd){
                                        if(!xOdd){
                                            index_static(index,Side,x+difference,x,x+1);
                                            index_static(index,Side,x+difference,x+1,x+difference+1);
                                        }else{
                                            index_static(index,Side,x+difference,x,x+difference+1);
                                            index_static(index,Side,x,x+1,x+difference+1);
                                        }
                                    }else{
                                        if(!xOdd){
                                            index_static(index,Side,x+difference,x,x+difference+1);
                                            index_static(index,Side,x,x+1,x+difference+1);
                                        }else{
                                            index_static(index,Side,x+difference,x,x+1);
                                            index_static(index,Side,x+difference,x+1,x+difference+1);
                                        }
                                    }
                                        xOdd = !xOdd;
				}
				startX += difference;
				maxX += difference;
                                yOdd = !yOdd;
                        }

			if(form_to_south){
				//build the bottom as usual
				index_static(index,Side,0,1,GeoUtils.LW_A1);
				index_static(index,Side,GeoUtils.LW_ALW_S2,GeoUtils.LW_S2,GeoUtils.LW_S1);
				for(int x = 1; x < GeoUtils.LW_S2; x++){
					index_static(index,Side,x+difference,x,x+1);
					index_static(index,Side,x+difference,x+1,x+difference+1);
				}
			}else{
				int skip_amount = 1;
				int currentvert = 0;
				int nextvert = (int) skip_amount+1;
				int yvert = GeoUtils.LW_A1;
				do{
					index_static(index,Side,currentvert,nextvert,yvert);
					for(int y = yvert; y < nextvert+GeoUtils.level_width; y++){
						if(y != GeoUtils.LW_ALW_S2){
							index_static(index,Side,y+1,y,nextvert);
						}
					}
					currentvert = nextvert;
					nextvert = currentvert+skip_amount+1;
					yvert = currentvert+GeoUtils.level_width;
				}while(nextvert <= GeoUtils.LW_S1);
			}
			if(form_to_north){
				//build the bottom as usual
				index_static(index,Side,GeoUtils.LW_MCLWS1,GeoUtils.LW_MCLWS1-GeoUtils.LW_S1,GeoUtils.LW_MCLWS1+1);
				index_static(index,Side,GeoUtils.LW_MCLWS1+GeoUtils.LW_S1,GeoUtils.LW_MCLWS1+GeoUtils.LW_S2,GeoUtils.LW_MCLWS1-2);
				for(int x = GeoUtils.LW_MCLWS1-GeoUtils.LW_S1; x < GeoUtils.LW_MCLWS1-GeoUtils.LW_S1+GeoUtils.LW_S3; x++){
					index_static(index,Side,x+difference,x,x+1);
					index_static(index,Side,x+difference,x+1,x+difference+1);
				}
			}else{
				int skip_amount = 1;
				int currentvert = GeoUtils.LW_MCLWS1;
				int nextvert = currentvert+skip_amount+1;
				int yvert =  ((GeoUtils.LW_A1)+(GeoUtils.level_width*(GeoUtils.LW_S3)));
				do{
					index_static(index,Side,currentvert,yvert,nextvert);
					for(int y = yvert; y < nextvert-GeoUtils.level_width; y++){
						if(y != (((GeoUtils.level_width*(GeoUtils.LW_S2)))+GeoUtils.LW_S2)){
							index_static(index,Side,y+1,nextvert,y);
						}
					}
					currentvert = nextvert;
					nextvert = currentvert+skip_amount+1;
					yvert = currentvert-GeoUtils.level_width;
				}while(nextvert <= GeoUtils.level_width*(GeoUtils.LW_S1)+GeoUtils.LW_S1);
			}
			if(form_to_west){
				//build the bottom as usual
				index_static(index,Side,GeoUtils.LW_MCLWS1,(GeoUtils.level_width*(GeoUtils.LW_S2)),(GeoUtils.level_width*(GeoUtils.LW_S2))+1);
				index_static(index,Side,GeoUtils.level_width,0,GeoUtils.LW_A1);
				for(int y = GeoUtils.level_width; y < GeoUtils.level_width*(GeoUtils.LW_S2); y=y+GeoUtils.level_width){
					index_static(index,Side,y+difference,y,y+1);
					index_static(index,Side,y+difference,y+1,y+difference+1);
				}
			}else{
				int skip_amount = 1;
				int currentvert = 0;
				int nextvert = GeoUtils.level_width *(1+skip_amount);
				int yvert = GeoUtils.LW_A1;
				do{
					index_static(index,Side,currentvert,yvert,nextvert);
					for(int y = yvert; y < nextvert+1; y=y+GeoUtils.level_width){
						if(y != (GeoUtils.level_width*(GeoUtils.LW_S2))+1){
							index_static(index,Side,nextvert,y,y+GeoUtils.level_width);
						}
					}
					currentvert = nextvert;
					nextvert = nextvert+(GeoUtils.level_width*(skip_amount+1));
					yvert = currentvert+1;
				}while(nextvert <= GeoUtils.LW_MCLWS1);
			}
			if(form_to_east){
                            	//build the bottom as usual
				index_static(index,Side,GeoUtils.LW_ALW_S2,GeoUtils.LW_S1,GeoUtils.LW_ALW_S1);
				index_static(index,Side,GeoUtils.LW_MCLWS1-2,GeoUtils.LW_MCLWS1-1,GeoUtils.LW_MCLWS1+GeoUtils.LW_S1);
				for(int y = GeoUtils.LW_ALW_S2; y < GeoUtils.level_width*(GeoUtils.LW_S2); y=y+GeoUtils.level_width){
					index_static(index,Side,y+difference,y,y+1);
					index_static(index,Side,y+difference,y+1,y+difference+1);
				}
			}else{
				int skip_amount = 1;
				int currentvert = GeoUtils.LW_S1;
				int nextvert = (GeoUtils.level_width *(1+skip_amount))+GeoUtils.LW_S1;
				int yvert = GeoUtils.LW_ALW_S2;
				do{
					index_static(index,Side,yvert,currentvert,nextvert);
					for(int y = yvert; y < nextvert-1; y=y+GeoUtils.level_width){
						if(y != GeoUtils.LW_MCLWS1-2){
							index_static(index,Side,nextvert,y+GeoUtils.level_width,y);
						}
					}
					currentvert = nextvert;
					nextvert = nextvert+(GeoUtils.level_width*(skip_amount+1));
					yvert = currentvert-1;
				}while(nextvert <= GeoUtils.LW_MCLWS1+GeoUtils.LW_S1);
			}
		}

		return index;
    }

    public static double lodDistance(int level, int maxLevels){
        double percentage = 1d;
      //  switch(maxLevels){
      //      case (8):
                switch(level){
                    case (0): percentage = 1.3d;
                        break;
                    case (1): percentage = 0.55d;
                        break;
                    case (2): percentage = 0.25d;
                        break;
                    case (3): percentage = 0.10d;
                        break;
                    case (4): percentage = 0.01d;
                        break;
                    case (5): percentage = 0.005d;
                        break;
                    case (6): percentage = 0.001d;
                        break;
                    case (7): percentage = 0.002d;
                        break;
      //          }
      //          break;
        }
        return percentage;
    }

    public static boolean isInverse(int Side1, int Side2){
            boolean inverse = false;
            switch(Side1){
                case (0):
                    switch(Side2){
                        case (1): inverse = true; break;
                        case (2): inverse = false; break;
                        case (3): inverse = true; break;
                        case (4): inverse = false; break;
                    }break;
                case (1):
                    switch(Side2){
                        case (0): inverse = true; break;
                        case (2): inverse = false; break;
                        case (3): inverse = true; break;
                        case (5): inverse = false; break;
                    }break;
                case (2):
                    switch(Side2){
                        case (0): inverse = false; break;
                        case (1): inverse = false; break;
                        case (4): inverse = true; break;
                        case (5): inverse = true; break;
                    }break;
                case (3):
                    switch(Side2){
                        case (0): inverse = true; break;
                        case (1): inverse = true; break;
                        case (4): inverse = false; break;
                        case (5): inverse = false; break;
                    }break;
                case (4):
                    switch(Side2){
                        case (0): inverse = false; break;
                        case (2): inverse = true; break;
                        case (3): inverse = false; break;
                        case (5): inverse = true; break;
                    }break;
                case (5):
                    switch(Side2){
                        case (1): inverse = false; break;
                        case (2): inverse = true; break;
                        case (3): inverse = false; break;
                        case (4): inverse = true; break;
                    }break;
            }

            return inverse;
        }

            //Used to get int neighbor values in correct order if neighbor.Side != Side
        public static int[] saved_neighbor_int(boolean divide, int startSide, int targetSide){
            int[] result = new int[2];
            if(divide){

            }else{
                switch(startSide){
                    case (0):
                        switch(targetSide){
                            case (1): result[0] = 1; result[1] = 0; break;
                            case (2): result[0] = 2; result[1] = 0; break;
                            case (3): result[0] = 3; result[1] = 1; break;
                            case (4): result[0] = 3; result[1] = 2; break;
                        } break;
                    case (1):
                        switch(targetSide){
                            case (0): result[0] = 2; result[1] = 3; break;
                            case (2): result[0] = 2; result[1] = 0; break;
                            case (3): result[0] = 3; result[1] = 1; break;
                            case (5): result[0] = 0; result[1] = 1; break;
                        } break;
                    case (2):
                        switch(targetSide){
                            case (0): result[0] = 3; result[1] = 0; break;
                            case (1): result[0] = 2; result[1] = 0; break;
                            case (4): result[0] = 3; result[1] = 1; break;
                            case (5): result[0] = 1; result[1] = 0; break;
                        } break;
                    case (3):
                        switch(targetSide){
                            case (0): result[0] = 2; result[1] = 3; break;
                            case (1): result[0] = 0; result[1] = 2; break;
                            case (4): result[0] = 1; result[1] = 3; break;
                            case (5): result[0] = 0; result[1] = 1; break;
                        } break;
                    case (4):
                        switch(targetSide){
                            case (0): result[0] = 3; result[1] = 2; break;
                            case (2): result[0] = 0; result[1] = 2; break;
                            case (3): result[0] = 1; result[1] = 3; break;
                            case (5): result[0] = 1; result[1] = 0; break;
                        } break;
                    case (5):
                        switch(targetSide){
                            case (1): result[0] = 0; result[1] = 1; break;
                            case (2): result[0] = 0; result[1] = 2; break;
                            case (3): result[0] = 1; result[1] = 3; break;
                            case (4): result[0] = 2; result[1] = 3; break;
                        } break;
                }
            }
            return result;
        }

        public static double[] orient_location(double x,double y,double z, int Side){
		double[] d = new double[3];
		switch(Side){
			case(0): d[0]=x; d[1]=1+y; d[2]=z; break;
			case(1): d[0]=x; d[1]=z; d[2]=-1-y; break;
			case(2): d[0]=-1-y; d[1]=z; d[2]=x; break;
			case(3): d[0]=1+y; d[1]=z; d[2]=x; break;
			case(4): d[0]=x; d[1]=z; d[2]=1+y; break;
			case(5): d[0]=x; d[1]=-1-y; d[2]=z; break;
		}
		return d;
	}

        public static double[] gather_target(int direction, Chunk_Spherical target, int Side, int thisSide) {
		double[] targets = new double[2];
		//Start with the basics and then change if nessessary
		switch(direction){
		case(0): targets[0] = target.minY; targets[1] = target.maxY; break;
		case(1): targets[0] = target.minX; targets[1] = target.maxX; break;
		case(2): targets[0] = target.minY; targets[1] = target.maxY; break;
		case(3): targets[0] = target.minX; targets[1] = target.maxX; break;
		}
		//Check for special cases of sides 2 and 3
		if( (direction == 3 && Side == 2 && target.Side == 0) ||
				(direction == 1 && Side == 2 && target.Side == 5) ||
				(direction == 3 && Side == 3 && target.Side == 0) ||
				(direction == 1 && Side == 3 && target.Side == 5) ||
                                (direction == 1 && thisSide == 0 && target.Side == 2)||
                                (direction == 3 && thisSide == 0 && target.Side == 2)||
                                (direction == 1 && thisSide == 0 && target.Side == 3)||
                                (direction == 3 && thisSide == 0 && target.Side == 3)||
                                (direction == 1 && thisSide == 5 && target.Side == 2)||
                                (direction == 3 && thisSide == 5 && target.Side == 2)||
                                (direction == 1 && thisSide == 5 && target.Side == 3)||
                                (direction == 3 && thisSide == 5 && target.Side == 3)
                                                                                    ){	targets[0] = target.minY; targets[1] = target.maxY; }
		//Check for special cases of side 0 and 5
		if( (direction == 0 && Side == 0 && target.Side == 2) ||
				(direction == 2 && Side == 0 && target.Side == 3) ||
				(direction == 0 && Side == 5 && target.Side == 2) ||
				(direction == 2 && Side == 5 && target.Side == 3)){	targets[0] = target.minX; targets[1] = target.maxX;	}
		return targets;
	}

        public static int[] normal_targets(int Side, int direction){
		int t[] = new int[2];
		switch(direction){ // 0 - west 1 - south 2 - east 3 - north
		case(0):
			switch(Side){
			case(0): t[0] = 2; t[1] = 3; break;
			case(1): t[0] = 0; t[1] = 2; break;
			case(2): t[0] = 0; t[1] = 2; break;
			case(3): t[0] = 1; t[1] = 3; break;
			case(4): t[0] = 1; t[1] = 3; break;
			case(5): t[0] = 0; t[1] = 1; break;
			}break;
		case(1):
			switch(Side){
			case(0): t[0] = 2; t[1] = 3; break;
			case(1): t[0] = 0; t[1] = 1; break;
			case(2): t[0] = 0; t[1] = 2; break;
			case(3): t[0] = 1; t[1] = 3; break;
			case(4): t[0] = 2; t[1] = 3; break;
			case(5): t[0] = 0; t[1] = 1; break;
			}break;
		case(2):
			switch(Side){
			case(0): t[0] = 2; t[1] = 3; break;
			case(1): t[0] = 0; t[1] = 2; break;
			case(2): t[0] = 0; t[1] = 2; break;
			case(3): t[0] = 1; t[1] = 3; break;
			case(4): t[0] = 1; t[1] = 3; break;
			case(5): t[0] = 0; t[1] = 1; break;
			}break;
		case(3):
			switch(Side){
			case(0): t[0] = 2; t[1] = 3; break;
			case(1): t[0] = 0; t[1] = 1; break;
			case(2): t[0] = 0; t[1] = 2; break;
			case(3): t[0] = 1; t[1] = 3; break;
			case(4): t[0] = 2; t[1] = 3; break; //TODO: Changed t[0] from 2 to 1
			case(5): t[0] = 0; t[1] = 1; break;
			}break;
		}
		return t;
	}

        /**
	 *
	 * @param neighbor_side
	 * @param direction 0 = down, 1 = up
	 * @return
	 */
	public static int normal_neighbor_select(int neighbor_side, int direction, int Side){
		int select = 0;
		switch(Side){
		case(0):
			switch(neighbor_side){
			case(2):
				switch(direction){
				case(0): select = 0; break;
				case(1): select = 2; break;
				}
				break;
			case(3):
				switch(direction){
				case(0): select = 0; break;
				case(1): select = 2; break;
				}
				break;
			case(0):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		case(1):
			switch(neighbor_side){
			case(2):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(3):
				switch(direction){
				case(0): select =1; break;
				case(1): select =3; break;
				}
				break;
			case(1):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		case(2):
			switch(neighbor_side){
			case(1):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(4):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(2):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		case(3):
			switch(neighbor_side){
			case(1):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(4):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(3):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		case(4):
			switch(neighbor_side){
			case(2):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(3):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			case(4):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		case(5):
			switch(neighbor_side){
			case(2):
				switch(direction){
				case(0): select = 0; break;
				case(1): select = 2; break;
				}
				break;
			case(3):
				switch(direction){
				case(0): select = 0; break;
				case(1): select = 2; break;
				}
				break;
			case(5):
				switch(direction){
				case(0): select = 1; break;
				case(1): select = 3; break;
				}
				break;
			}
			break;
		}
		return select;
	}

        /**
         * This function takes in the side of the neighbor which will be used to calculate the corner normal and will
         * return 2 int values to represent the 2 children numbers to focus on.
         * @param neighbor_side
         * @return int[2]
         */
        public static int[] neighbor_normal_wall(int neighbor_side, int Side){
            int[] sides = new int[2];
            switch(Side){
                case (0):
                    switch(neighbor_side){
                        case (1): sides[0] = 2; sides[1] = 3;
                            break;
                        case (2): sides[0] = 2; sides[1] = 3;
                            break;
                        case (3): sides[0] = 2; sides[1] = 3;
                            break;
                        case (4): sides[0] = 2; sides[1] = 3;
                            break;
                    }
                    break;
                case (1):
                    switch(neighbor_side){
                        case (0): sides[0] = 0; sides[1] = 1;
                            break;
                        case (2): sides[0] = 0; sides[1] = 2;
                            break;
                        case (3): sides[0] = 0; sides[1] = 2;
                            break;
                        case (5): sides[0] = 0; sides[1] = 1;
                            break;
                    }
                    break;
                case (2):
                    switch(neighbor_side){
                        case (0): sides[0] = 0; sides[1] = 2;
                            break;
                        case (1): sides[0] = 0; sides[1] = 2;
                            break;
                        case (4): sides[0] = 0; sides[1] = 2;
                            break;
                        case (5): sides[0] = 0; sides[1] = 2;
                            break;
                    }
                    break;
                case (3):
                    switch(neighbor_side){
                        case (0): sides[0] = 1; sides[1] = 3;
                            break;
                        case (1): sides[0] = 1; sides[1] = 3;
                            break;
                        case (4): sides[0] = 1; sides[1] = 3;
                            break;
                        case (5): sides[0] = 1; sides[1] = 3;
                            break;
                    }
                    break;
                case (4):
                    switch(neighbor_side){
                        case (0): sides[0] = 2; sides[1] = 3;
                            break;
                        case (2): sides[0] = 1; sides[1] = 3;
                            break;
                        case (3): sides[0] = 1; sides[1] = 3;
                            break;
                        case (5): sides[0] = 2; sides[1] = 3;
                            break;
                    }
                    break;
                case (5):
                    switch(neighbor_side){
                        case (1): sides[0] = 0; sides[1] = 1;
                            break;
                        case (2): sides[0] = 0; sides[1] = 1;
                            break;
                        case (3): sides[0] = 0; sides[1] = 1;
                            break;
                        case (4): sides[0] = 0; sides[1] = 1;
                            break;
                    }
                    break;
            }
            return sides;
        }

        public static int normal_corner(int target_side, int direction, int Side){
		int output = 0;
		// 2 ______ 3
		//  |      |
		//  |      |
		// 0|______|1
		// 0 = south west
		// 2 = north west
		// 1 = south east
		// 3 = north east
		switch(direction){
		case(0): /**SOUTH WEST **/
			switch(Side){
			case(0):
				switch(target_side){
				case(0): output = LW_MLW_S1; break;
				case(1): output = LW_MLW_S1; break;
				case(2): output = LW_MLW_S1; break;
				}break;
			case(1):
				switch(target_side){
				case(1): output = LW_MLW_S1; break;
				case(2): output = LW_MCLWS1; break;
				case(5): output = LW_S1; break;
				}break;
			case(2):
				switch(target_side){
				case(1): output = LW_MCLWS1; break;
				case(2): output = LW_MLW_S1; break;
				case(5): output = LW_MCLWS1; break; //edited for bugs here <---
				}break;
			case(3):
				switch(target_side){
				case(1): output = LW_MLW_S1; break;
				case(3): output = LW_MLW_S1; break;
				case(5): output = LW_MLW_S1; break;
				}break;
			case(4):
				switch(target_side){
				case(2): output = LW_MLW_S1; break;
				case(4): output = LW_MLW_S1; break;
				case(5): output = LW_MLW_S1; break;
				}break;
			case(5):
				switch(target_side){
				case(1): output = LW_S1; break;
				case(2): output = LW_S1; break;
				case(5): output = LW_MLW_S1; break;
				}
			}break;

		case(2): /**NORTH WEST **/
			switch(Side){
			case(0):
				switch(target_side){
				case(0): output = LW_S1; break;
				case(2): output = LW_MCLWS1; break;
				case(4): output = LW_MLW_S1; break;
				}break;
			case(1):
				switch(target_side){
				case(0): output = LW_S1; break;
				case(1): output = LW_S1; break;
				case(2): output = 0; break;
				}break;
			case(2):
				switch(target_side){
				case(0): output = LW_MCLWS1; break;
				case(1): output = 0; break;
				case(2): output = LW_S1; break;
				}break;
			case(3):
				switch(target_side){
				case(0): output = LW_MLW_S1; break; //edited here <---
				case(1): output = LW_S1; break;
				case(3): output = LW_S1; break;
				}break;
			case(4):
				switch(target_side){
				case(0): output = LW_MLW_S1; break;
				case(2): output = LW_S1; break;
				case(4): output = LW_S1; break;
				}break;
			case(5):
				switch(target_side){
				case(2): output = 0; break;
				case(4): output = LW_S1; break;
				case(5): output = LW_S1; break;
				}
			}break;

		case(1): /**SOUTH EAST **/
			switch(Side){
			case(0):
				switch(target_side){
				case(0): output = LW_MCLWS1; break;
				case(1): output = LW_MCLWS1; break;
				case(3): output = LW_MLW_S1; break;
				}break;
			case(1):
				switch(target_side){
				case(1): output = LW_MCLWS1; break;
				case(3): output = LW_MCLWS1; break;
				case(5): output = 0; break;
				}break;
			case(2):
				switch(target_side){
				case(2): output = LW_MCLWS1; break;
				case(4): output = LW_MCLWS1; break;
				case(5): output = 0; break;
				}break;
			case(3):
				switch(target_side){
				case(3): output = LW_MCLWS1; break;
				case(4): output = LW_MLW_S1; break;
				case(5): output = LW_S1; break;
				}break;
			case(4):
				switch(target_side){
				case(3): output = LW_MLW_S1; break;
				case(4): output = LW_MCLWS1; break;
				case(5): output = LW_MCLWS1; break;
				}break;
			case(5):
				switch(target_side){
				case(1): output = 0; break;
				case(3): output = LW_S1; break;
				case(5): output = LW_MCLWS1; break;
				}
			}break;

			case(3): /**NORTH EAST **/
			switch(Side){
			case(0):
				switch(target_side){
				case(0): output = 0; break;
				case(3): output = LW_MCLWS1; break;
				case(4): output = LW_MCLWS1; break;
				}break;
			case(1):
				switch(target_side){
				case(0): output = 0; break;
				case(1): output = 0; break;
				case(3): output = 0; break;
				}break;
			case(2):
				switch(target_side){
				case(0): output = 0; break;
				case(2): output = 0; break;
				case(4): output = 0; break;
				}break;
			case(3):
				switch(target_side){
				case(0): output = LW_S1; break;
				case(3): output = 0; break;
				case(4): output = LW_S1; break;
				}break;
			case(4):
				switch(target_side){
				case(0): output = LW_MCLWS1; break;
				case(3): output = LW_S1; break;
				case(4): output = 0; break;
				}break;
			case(5):
				switch(target_side){
				case(3): output = 0; break;
				case(4): output = 0; break;
				case(5): output = 0; break;
				}
			}break;
		}
		return output;
	}

        /**
         * This function takes in 3 integers representing sides of the cube and returns true if there are 2 or less unique sides.
         **/
        public static boolean compare_sides(int side1, int side2, int side3){
            boolean combination1 = side1 == side2;
            boolean combination2 = side1 == side3;
            boolean combination3 = side2 == side3;
            return combination1 || combination2 || combination3;
        }

                /**
         * This function takes in a direction (north or south) to gather the height level limit of a neighbor to determine if
         * is is the proper corner neighbor
         * @param direction1 - 0 = south,  1 = north
         * @param direction2 - 0 = west,   1 = east
         * @param c - the neighbor to gather limit from
         * @return limit
         */
        public static double[] gather_limit(int direction1, int direction2, Chunk_Spherical c, Chunk_Spherical master, int Side){
            double[] limit = new double[4];
            limit[2] = master.minX;
            limit[3] = master.maxX;
            if((Side == 0 || Side == 5) && (master.Side == 2 || master.Side == 3)){
                limit[2] = master.minY;
                limit[3] = master.maxY;
            }
            if(c.Side == Side){
               if(direction1 == 1){
                   limit[0] = c.minY;
               }else{
                   limit[0] = c.maxY;
               }
               if(direction2 == 1){
                   limit[1] = c.minX;
               }else{
                   limit[1] = c.maxX;
               }
            }else{
                if(direction1 == 1){
                    switch(Side){
                        case (0):
                            switch(c.Side){
                                case (2): limit[0] = c.minX;
                                    break;
                                case (3): limit[0] = c.minX;
                                    break;
                                case (4): limit[0] = c.maxY;
                                    break;
                            }
                            break;
                        case (1):
                            switch(c.Side){
                                case (0): limit[0] = c.minY + 2.0;
                                    break;
                                case (2): limit[0] = c.minY;
                                    break;
                                case (3): limit[0] = c.minY;
                                    break;
                            }
                            break;
                        case (2):
                            switch(c.Side){
                                case (0): limit[0] = c.minX + 2.0;
                                    break;
                                case (1): limit[0] = c.minY;
                                    break;
                                case (4): limit[0] = c.minY;
                                    break;
                            }
                            break;
                        case (3):
                            switch(c.Side){
                                case (0): limit[0] = c.maxX;
                                    break;
                                case (1): limit[0] = c.minY;
                                    break;
                                case (4): limit[0] = c.minY;
                                    break;
                            }
                            break;
                        case (4):
                            switch(c.Side){
                                case (0): limit[0] = c.maxY;
                                    break;
                                case (2): limit[0] = c.minY;
                                    break;
                                case (3): limit[0] = c.minY;
                                    break;
                            }
                            break;
                        case (5):
                            switch(c.Side){
                                case (2): limit[0] = c.minX;
                                    break;
                                case (3): limit[0] = c.minX;
                                    break;
                                case (4): limit[0] = c.minY + 2.0;
                                    break;
                            }
                            break;
                    }
                }else{
                    switch(Side){
                        case (0):
                            switch(c.Side){
                                case (1): limit[0] = c.maxY - 2.0;
                                    break;
                                case (2): limit[0] = c.maxX;
                                    break;
                                case (3): limit[0] = c.maxX;
                                    break;
                            }
                            break;
                        case (1):
                            switch(c.Side){
                                case (2): limit[0] = c.maxY;
                                    break;
                                case (3): limit[0] = c.maxY;
                                    break;
                                case (5): limit[0] = c.minY;
                                    break;
                            }
                            break;
                        case (2):
                            switch(c.Side){
                                case (1): limit[0] = c.maxY;
                                    break;
                                case (4): limit[0] = c.maxY;
                                    break;
                                case (5): limit[0] = c.minX;
                                    break;
                            }
                            break;
                        case (3):
                            switch(c.Side){
                                case (1): limit[0] = c.maxY;
                                    break;
                                case (4): limit[0] = c.maxY;
                                    break;
                                case (5): limit[0] = c.maxX - 2.0;
                                    break;
                            }
                            break;
                        case (4):
                            switch(c.Side){
                                case (2): limit[0] = c.maxY;
                                    break;
                                case (3): limit[0] = c.maxY;
                                    break;
                                case (5): limit[0] = c.maxY - 2.0;
                                    break;
                            }
                            break;
                        case (5):
                            switch(c.Side){
                                case (1): limit[0] = c.minY;
                                    break;
                                case (2): limit[0] = c.maxX;
                                    break;
                                case (3): limit[0] = c.maxX;
                                    break;
                            }
                            break;
                    }
                }
                /////////////////////////////////////////////
                if(direction2 == 1){
                    switch(Side){
                        case (0):
                            switch(c.Side){
                                case (1): limit[1] = c.minX;
                                    break;
                                case (3): limit[1] = c.maxY;
                                    break;
                                case (4): limit[1] = c.minX;
                                    break;
                            }
                            break;
                        case (1):
                            switch(c.Side){
                                case (0): limit[1] = c.minX;
                                    break;
                                case (3): limit[1] = c.minX + 2.0;
                                    break;
                                case (5): limit[1] = c.minX;
                                    break;
                            }
                            break;
                        case (2):
                            switch(c.Side){
                                case (0): limit[1] = c.minY;
                                    break;
                                case (4): limit[1] = c.minX + 2.0;
                                    break;
                                case (5): limit[1] = c.minY;
                                    break;
                            }
                            break;
                        case (3):
                            switch(c.Side){
                                case (0): limit[1] = c.minY;
                                    break;
                                case (4): limit[1] = c.maxX;
                                    break;
                                case (5): limit[1] = c.minY;
                                    break;
                            }
                            break;
                        case (4):
                            switch(c.Side){
                                case (0): limit[1] = c.minX;
                                    break;
                                case (3): limit[1] = c.maxX;
                                    break;
                                case (5): limit[1] = c.minX;
                                    break;
                            }
                            break;
                        case (5):
                            switch(c.Side){
                                case (1): limit[1] = c.minX;
                                    break;
                                case (3): limit[1] = c.minY + 2.0;
                                    break;
                                case (4): limit[1] = c.minX;
                                    break;
                            }
                            break;
                    }
                }else{
                    switch(Side){
                        case (0):
                            switch(c.Side){
                                case (1): limit[1] = c.maxX;
                                    break;
                                case (2): limit[1] = c.maxY - 2.0;
                                    break;
                                case (4): limit[1] = c.maxX;
                                    break;
                            }
                            break;
                        case (1):
                            switch(c.Side){
                                case (0): limit[1] = c.maxX;
                                    break;
                                case (2): limit[1] = c.minX;
                                    break;
                                case (5): limit[1] = c.maxX;
                                    break;
                            }
                            break;
                        case (2):
                            switch(c.Side){
                                case (0): limit[1] = c.maxY;
                                    break;
                                case (1): limit[1] = c.minX;
                                    break;
                                case (5): limit[1] = c.maxY;
                                    break;
                            }
                            break;
                        case (3):
                            switch(c.Side){
                                case (0): limit[1] = c.maxY;
                                    break;
                                case (1): limit[1] = c.maxX - 2.0;
                                    break;
                                case (5): limit[1] = c.maxY;
                                    break;
                            }
                            break;
                        case (4):
                            switch(c.Side){
                                case (0): limit[1] = c.maxX;
                                    break;
                                case (2): limit[1] = c.maxX - 2.0;
                                    break;
                                case (5): limit[1] = c.maxX;
                                    break;
                            }
                            break;
                        case (5):
                            switch(c.Side){
                                case (1): limit[1] = c.maxX;
                                    break;
                                case (2): limit[1] = c.minY;
                                    break;
                                case (4): limit[1] = c.maxX;
                                    break;
                            }
                            break;
                    }
                }
            }

            return limit;
        }

        public static float form_to_west(Chunk_Spherical a){
            float form = a.level;
            if(a.saved_neighbors[0] != null){
                form = Math.min(form,a.saved_neighbors[0].level);
            }
            if(a.saved_neighbors[2] != null){
                form = Math.min(form,a.saved_neighbors[2].level);
                if(a.saved_neighbors[1] != null){
                    form = Math.min(form,a.saved_neighbors[1].level);
                }
                if(a.saved_neighbors[3] != null){
                    form = Math.min(form,a.saved_neighbors[3].level);
                }
            }
            return form;
        }

        public static float form_to_south(Chunk_Spherical a){
            float form = a.level;
            if(a.saved_neighbors[4] != null){
                form = Math.min(form,a.saved_neighbors[4].level);
            }
            if(a.saved_neighbors[6] != null){
                form = Math.min(form,a.saved_neighbors[6].level);
                if(a.saved_neighbors[5] != null){
                    form = Math.min(form,a.saved_neighbors[5].level);
                }
                if(a.saved_neighbors[7] != null){
                    form = Math.min(form,a.saved_neighbors[7].level);
                }
            }
            return form;
        }

        public static float form_to_east(Chunk_Spherical a){
            float form = a.level;
            if(a.saved_neighbors[8] != null){
                form = Math.min(form,a.saved_neighbors[8].level);
            }
            if(a.saved_neighbors[10] != null){
                form = Math.min(form,a.saved_neighbors[10].level);
                if(a.saved_neighbors[9] != null){
                    form = Math.min(form,a.saved_neighbors[9].level);
                }
                if(a.saved_neighbors[11] != null){
                    form = Math.min(form,a.saved_neighbors[11].level);
                }
            }
            return form;
        }

        public static float form_to_north(Chunk_Spherical a){
            float form = a.level;
            if(a.saved_neighbors[12] != null){
                form = Math.min(form,a.saved_neighbors[12].level);
            }
            if(a.saved_neighbors[14] != null){
                form = Math.min(form,a.saved_neighbors[14].level);
                if(a.saved_neighbors[13] != null){
                    form = Math.min(form,a.saved_neighbors[13].level);
                }
                if(a.saved_neighbors[15] != null){
                    form = Math.min(form,a.saved_neighbors[15].level);
                }
            }
            return form;
        }

        private static void index_static(IntBuffer index, int Side, int v1, int v2, int v3){
		if(Side == 0 || Side == 3 || Side == 1){
			index.put(v1).put(v3).put(v2);
		}else{
			index.put(v1).put(v2).put(v3);
		}
	}
}
