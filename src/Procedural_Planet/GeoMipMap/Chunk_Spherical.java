package Procedural_Planet.GeoMipMap;

import Procedural_Noise.Functions.Noise;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;


import java.util.ArrayList;

public class Chunk_Spherical extends Node{
	public float level;
	Chunk_Spherical parent;
	public Chunk_Spherical[] children = new Chunk_Spherical[4];
	Geometry g;
	Material mat;
	public boolean has_child = false;
	public boolean finished_building = false;
        public boolean unite_called = false; //debug only
	boolean verbose = true;
	double size;
	double radius;
	Noise n;
	int Side;
	public Mesh q = new Mesh();
	double minX;
	double minY;
	double maxX;
	double maxY;
	Vector3f midpoint;
	public Optimizer opt;
        //sibling
	//33 x 33
        double level_width = 33;
	float triangle_count = 2048;

	//17 x 17
    //float level_width = 17;
    //float triangle_count = 512;
	
	//9 x 9
       // float level_width = 9;
	//float triangle_count = 128;
	

	public boolean divide = false;
	int childNum=0;
	double posX =0;
	double posY =0;
	double detail = 1024;
	double maxWidth;
	double noise_scale;
	int maxLevel = 4;
	

	
	//Neighbors for indexing
	//Chunk_Spherical[] neighbors = new Chunk_Spherical[4];
        //     |      |      |
        //     |14(15)|12(13)|
        //     |--------------  10(11)
        //   0 |             |
        //  (1)|             |
        //  ___|             |___ 
        //     |             |
        //     |             |
        //  2  |--------------  8(9)
        //  (3)|      |      |
        //     | 4 (5)| 6 (7)|
        Chunk_Spherical[] saved_neighbors = new Chunk_Spherical[16];

	
	Vector3f build_normals[] = new Vector3f[GeoUtils.TC_M3];
	int build_normals_count[] = new int[GeoUtils.TC_M3];

	Vector3f buffer_normal_top[] = new Vector3f[GeoUtils.level_width];
	Vector3f buffer_normal_bottom[] = new Vector3f[GeoUtils.level_width];
	Vector3f buffer_normal_left[] = new Vector3f[GeoUtils.LW_S2];
	Vector3f buffer_normal_right[] = new Vector3f[GeoUtils.LW_S2];
	int buffer_normal_top_count[] = new int[GeoUtils.level_width];
	int buffer_normal_bottom_count[] = new int[GeoUtils.level_width];
	int buffer_normal_left_count[] = new int[GeoUtils.LW_S2];
	int buffer_normal_right_count[] = new int[GeoUtils.LW_S2];

        Geomipmap_Cube master = null;

	
	public Chunk_Spherical(double noise_scale,double radius,Node rootNode, int Side, Material mat){
                init_buffers();
                master = (Geomipmap_Cube)rootNode;
                opt = ((Geomipmap_Cube)rootNode).opt;
                opt.countObject(1);
		level = 0;
		this.Side = Side;
		this.size = 2;
		this.radius=radius;
		this.mat = mat;
		this.noise_scale = noise_scale;
		rootNode.attachChild(this);
		n = new Noise(5675,2048);
                double size_d2 = size/2;
		maxX=(size_d2);
		maxY=(size_d2);
		minX=(-size_d2);
		minY=(-size_d2);
		maxWidth = ((level+1)*level_width)-level;
		produce_vertex();
		build_level(null,true);
		computeNormals(false,false,"ME");
		finished_building = true;
                setName(Side+":0");
                
	}
	
	private Chunk_Spherical(double minX, double minY, Chunk_Spherical parent, int childNum,double posX,double posY, Geomipmap_Cube geoc){
                init_buffers();
                opt = parent.opt;
                master = geoc;
		level = parent.level + 1;
		Side = parent.Side;
		size = parent.size/2;
		radius = parent.radius;
		noise_scale = parent.noise_scale;
		this.minX=minX;
		this.minY=minY;
		mat = parent.mat;
		maxX= (minX + size);
		maxY= (minY + size);
		this.posX=posX;
		this.posY=posY;
		this.parent = parent;
		this.parent.attachChild(this);
		this.n = parent.n;
		this.childNum = childNum;
		this.mat = parent.mat;
		maxWidth = ((level+1)*level_width)-level;
		produce_vertex();
                setName(parent.getName()+"|"+childNum);
	}
	
	private void init_buffers(){
                Vector3f zero = new Vector3f(0,0,0);
		for(int i = 0; i < GeoUtils.LW_MLW; i++){
			build_normals[i] = zero;
			build_normals_count[i] = 0;
		}
		for(int i = 0; i < GeoUtils.level_width; i++){
			buffer_normal_top[i] = zero;
			buffer_normal_bottom[i] = zero;
			buffer_normal_top_count[i] = 0;
			buffer_normal_bottom_count[i] = 0;
			if(i < GeoUtils.LW_S2){
				buffer_normal_left[i] = zero;
				buffer_normal_right[i] = zero;
				buffer_normal_left_count[i] = 0;
				buffer_normal_right_count[i] = 0;
			}
		}
	}
	

	
	public void update(Vector3f camLoc, Camera cam){
		boolean distance_check = midpoint.add(this.getWorldTranslation()).subtract(camLoc).length() - (level) > radius * GeoUtils.lodDistance((int) level,maxLevel);
                if(has_child){
                    if(!children[0].has_child && !children[1].has_child && !children[2].has_child && !children[3].has_child && canUnite()){
			if(distance_check){                     
                                    compute_walls(true,null,true);
					if(children[0].finished_building && children[1].finished_building && children[2].finished_building && children[3].finished_building){
                                            unite_called = true;
                                            do{
                                                prep_unite();
                                            }while(children[0].has_child == true || children[1].has_child == true || children[2].has_child == true || children[3].has_child == true);
                                            unite("Distance check");
					}				
			}else{
				for(int i = 0; i < 4; i++){
					children[i].update(camLoc,cam);
				}				
			}
                    }else{
                        for(int i = 0; i < 4; i++){
					children[i].update(camLoc,cam);
			}
                    }
		}else if(!distance_check && level < maxLevel){
			divide("distance check",null,true);
		}
	}
        private boolean canUnite(){
            int position = 0;
            float max_side = 0;
            for(int i = 0; i < 4; i++){
                for(int j = 0; j < 4; j++){
                    if(children[i].saved_neighbors[position] != null){
                        max_side = Math.max(children[i].saved_neighbors[position].level,max_side);
                        if(children[i].saved_neighbors[position+2] != null){
                            max_side = Math.max(children[i].saved_neighbors[position+2].level,max_side);
                            if(children[i].saved_neighbors[position+1] != null){
                                max_side = Math.max(children[i].saved_neighbors[position+1].level,max_side);
                            }
                            if(children[i].saved_neighbors[position+3] != null){
                                max_side = Math.max(children[i].saved_neighbors[position+3].level,max_side);
                            }
                        }
                    }
                    position+=4;
                }
                position=0;
            }
            return !(max_side-level > 1);
        }
	
	private void frustum_check(Camera cam){		
		if(has_child){
			for(int i = 0; i < 4; i++){
				int pstate = cam.getPlaneState();
				cam.setPlaneState(0);
				//if(cam.contains(q.getModelBound()) == frustrumIntersects.Outside){
				children[i].q.updateBound();
				FrustumIntersect fi = cam.contains(children[i].q.getBound());
				if(fi== FrustumIntersect.Outside){
					if(!(children[i].parent == null)){
						children[i].removeFromParent();
					}
				}else{
					if(children[i].parent == null){
						this.attachChild(children[i]);
					}
					children[i].frustum_check(cam);
				}
				cam.setPlaneState(pstate);
			}
		}
	}
	
	public void divide(String wherefrom, ArrayList<Chunk_Spherical> ignore_Chunk, boolean initial){
                //System.out.println(wherefrom);
                opt.write("D: "+name+" "+wherefrom);
		long start = 0;
                opt.countObject(4); //remove
		if(verbose){ System.out.println("Start Divide "+name);	start = System.nanoTime(); };

		has_child=true;
		double space = (detail / Math.pow(2,level+1));
		children[0]=new Chunk_Spherical(minX,minY,this,0,posX,posY,master);
		children[1]=new Chunk_Spherical((minX+(size/2)),minY,this,1,posX + space,posY,master);
		children[2]=new Chunk_Spherical(minX, (minY+(size/2)),this,2,posX,posY+space,master);
		children[3]=new Chunk_Spherical((minX+(size/2)), (minY+(size/2)),this,3,posX+space,posY+space,master);
                children[0].saved_neighbors[8] = children[1];
                children[0].saved_neighbors[12] = children[2];
                children[1].saved_neighbors[0] = children[0];
                children[1].saved_neighbors[12] = children[3];
                children[2].saved_neighbors[4] = children[0];
                children[2].saved_neighbors[8] = children[3];
                children[3].saved_neighbors[0] = children[2];
                children[3].saved_neighbors[4] = children[1];

                if(saved_neighbors[1] != null){
                    children[0].saved_neighbors[0] = saved_neighbors[2];
                    children[2].saved_neighbors[0] = saved_neighbors[0];
                    children[2].saved_neighbors[2] = saved_neighbors[1];
                    if(saved_neighbors[0].Side == Side){
                       saved_neighbors[0].saved_neighbors[8] = children[2];
                       saved_neighbors[1].saved_neighbors[8] = children[2];
                       saved_neighbors[2].saved_neighbors[8] = children[0];
                    }else{
                        saved_neighbors[0].replaceNeighbor(this,children[2],null);
                        saved_neighbors[1].replaceNeighbor(this,children[2],null);
                        saved_neighbors[2].replaceNeighbor(this,children[0],null);
                    }
                }else if(saved_neighbors[3] != null){
                    children[0].saved_neighbors[0] = saved_neighbors[2];
                    children[0].saved_neighbors[2] = saved_neighbors[3];
                    children[2].saved_neighbors[0] = saved_neighbors[0];
                    if(saved_neighbors[0].Side == Side){
                        saved_neighbors[0].saved_neighbors[8] = children[2];
                        saved_neighbors[2].saved_neighbors[8] = children[0];
                        saved_neighbors[3].saved_neighbors[8] = children[0];
                    }else{
                        saved_neighbors[0].replaceNeighbor(this,children[2],null);
                        saved_neighbors[2].replaceNeighbor(this,children[0],null);
                        saved_neighbors[3].replaceNeighbor(this,children[0],null);
                    }
                }else if(saved_neighbors[2] != null){
                    children[0].saved_neighbors[0] = saved_neighbors[2];
                    children[2].saved_neighbors[0] = saved_neighbors[0];
                    if(saved_neighbors[0].Side == Side){
                        saved_neighbors[0].saved_neighbors[8] = children[2];
                        saved_neighbors[2].saved_neighbors[8] = children[0];
                    }else{
                        saved_neighbors[0].replaceNeighbor(this,children[2],null);
                        saved_neighbors[2].replaceNeighbor(this,children[0],null);
                    }
                }else{
                    children[0].saved_neighbors[0] = saved_neighbors[0];
                    children[2].saved_neighbors[0] = saved_neighbors[0];
                    if(saved_neighbors[0].Side == Side){
                        if(saved_neighbors[0].level == level){
                          saved_neighbors[0].saved_neighbors[8] = children[0];
                          saved_neighbors[0].saved_neighbors[10] = children[2];
                        }else if(childNum == 0){
                          saved_neighbors[0].saved_neighbors[8] = children[0];
                          saved_neighbors[0].saved_neighbors[9] = children[2];
                        }else{
                          saved_neighbors[0].saved_neighbors[10] = children[0];
                          saved_neighbors[0].saved_neighbors[11] = children[2];
                        }
                        
                    }else{
                        if(GeoUtils.isInverse(Side,saved_neighbors[0].Side)){
                            saved_neighbors[0].replaceNeighbor(this,children[0],children[2]);
                        }else{
                            saved_neighbors[0].replaceNeighbor(this,children[2],children[0]);
                        }
                    }
                }
               
                   
                
                if(saved_neighbors[5] != null){
                    children[0].saved_neighbors[4] = saved_neighbors[4];
                    children[0].saved_neighbors[6] = saved_neighbors[5];
                    children[1].saved_neighbors[4] = saved_neighbors[6];
                    if(saved_neighbors[4].Side == Side){
                        saved_neighbors[4].saved_neighbors[12] = children[0];
                        saved_neighbors[5].saved_neighbors[12] = children[0];
                        saved_neighbors[6].saved_neighbors[12] = children[1];
                    }else{                        
                        saved_neighbors[4].replaceNeighbor(this,children[0],null);
                        saved_neighbors[5].replaceNeighbor(this,children[0],null);                        
                        saved_neighbors[6].replaceNeighbor(this,children[1],null);
                    }
                }else if(saved_neighbors[7] != null){
                    children[0].saved_neighbors[4] = saved_neighbors[4];
                    children[1].saved_neighbors[4] = saved_neighbors[6];
                    children[1].saved_neighbors[6] = saved_neighbors[7];
                    if(saved_neighbors[4].Side == Side){
                        saved_neighbors[4].saved_neighbors[12] = children[0];
                        saved_neighbors[6].saved_neighbors[12] = children[1];
                        saved_neighbors[7].saved_neighbors[12] = children[1];
                    }else{
                        saved_neighbors[4].replaceNeighbor(this,children[0],null);
                        saved_neighbors[6].replaceNeighbor(this,children[1],null);
                        saved_neighbors[7].replaceNeighbor(this,children[1],null);
                    }
                }else if(saved_neighbors[6] != null){
                    children[0].saved_neighbors[4] = saved_neighbors[4];
                    children[1].saved_neighbors[4] = saved_neighbors[6];
                    if(saved_neighbors[6].Side == Side){
                        saved_neighbors[4].saved_neighbors[12] = children[0];
                        saved_neighbors[6].saved_neighbors[12] = children[1];
                    }else{
                        saved_neighbors[4].replaceNeighbor(this,children[0],null);
                        saved_neighbors[6].replaceNeighbor(this,children[1],null);
                    }
                }else{
                    children[0].saved_neighbors[4] = saved_neighbors[4];
                    children[1].saved_neighbors[4] = saved_neighbors[4];
                    if(saved_neighbors[4].Side == Side){
                        if(saved_neighbors[4].level == level){
                          saved_neighbors[4].saved_neighbors[12] = children[1];
                          saved_neighbors[4].saved_neighbors[14] = children[0];
                        }else if(childNum == 0){
                          saved_neighbors[4].saved_neighbors[14] = children[1];
                          saved_neighbors[4].saved_neighbors[15] = children[0];
                        }else{
                          saved_neighbors[4].saved_neighbors[12] = children[1];
                          saved_neighbors[4].saved_neighbors[13] = children[0];
                        }
                        
                    }else{                        
                        if(GeoUtils.isInverse(Side,saved_neighbors[4].Side)){
                            saved_neighbors[4].replaceNeighbor(this,children[1],children[0]);
                        }else{
                            saved_neighbors[4].replaceNeighbor(this,children[0],children[1]);
                        }
                    }
                }

                if(saved_neighbors[9] != null){
                    children[1].saved_neighbors[8] = saved_neighbors[8];
                    children[1].saved_neighbors[10] = saved_neighbors[9];
                    children[3].saved_neighbors[8] = saved_neighbors[10];
                    if(saved_neighbors[8].Side == Side){
                        saved_neighbors[8].saved_neighbors[0] = children[1];
                        saved_neighbors[9].saved_neighbors[0] = children[1];
                        saved_neighbors[10].saved_neighbors[0] = children[3];
                    }else{
                        saved_neighbors[8].replaceNeighbor(this,children[1],null);
                        saved_neighbors[9].replaceNeighbor(this,children[1],null);
                        saved_neighbors[10].replaceNeighbor(this,children[3],null);
                    }
                }else if(saved_neighbors[11] != null){
                    children[1].saved_neighbors[8] = saved_neighbors[8];
                    children[3].saved_neighbors[8] = saved_neighbors[10];
                    children[3].saved_neighbors[10] = saved_neighbors[11];
                    if(saved_neighbors[8].Side == Side){
                        saved_neighbors[8].saved_neighbors[0] = children[1];
                        saved_neighbors[10].saved_neighbors[0] = children[3];
                        saved_neighbors[11].saved_neighbors[0] = children[3];
                    }else{
                        saved_neighbors[8].replaceNeighbor(this,children[1],null);
                        saved_neighbors[10].replaceNeighbor(this,children[3],null);
                        saved_neighbors[11].replaceNeighbor(this,children[3],null);
                    }
                }else if(saved_neighbors[10] != null){
                    children[1].saved_neighbors[8] = saved_neighbors[8];
                    children[3].saved_neighbors[8] = saved_neighbors[10];
                    if(saved_neighbors[10].Side == Side){
                        saved_neighbors[8].saved_neighbors[0] = children[1];
                        saved_neighbors[10].saved_neighbors[0] = children[3];
                    }else{                        
                        saved_neighbors[8].replaceNeighbor(this,children[1],null);
                        saved_neighbors[10].replaceNeighbor(this,children[3],null);
                    }
                }else{
                    children[1].saved_neighbors[8] = saved_neighbors[8];
                    children[3].saved_neighbors[8] = saved_neighbors[8];
                    if(saved_neighbors[8].Side == Side){
                        if(saved_neighbors[8].level == level){
                            saved_neighbors[8].saved_neighbors[0] = children[3];
                            saved_neighbors[8].saved_neighbors[2] = children[1];
                        }else if(childNum == 1){
                            saved_neighbors[8].saved_neighbors[2] = children[3];
                            saved_neighbors[8].saved_neighbors[3] = children[1];
                        }else{
                            saved_neighbors[8].saved_neighbors[0] = children[3];
                            saved_neighbors[8].saved_neighbors[1] = children[1];
                        }
                    }else{
                        if(GeoUtils.isInverse(Side,saved_neighbors[8].Side)){
                            saved_neighbors[8].replaceNeighbor(this,children[3],children[1]);
                        }else{
                            saved_neighbors[8].replaceNeighbor(this,children[1],children[3]);
                        }
                    }
                }

                if(saved_neighbors[13] != null){
                    children[3].saved_neighbors[12] = saved_neighbors[12];
                    children[3].saved_neighbors[14] = saved_neighbors[13];
                    children[2].saved_neighbors[12] = saved_neighbors[14];
                    if(saved_neighbors[12].Side == Side){
                        saved_neighbors[12].saved_neighbors[4] = children[3];
                        saved_neighbors[13].saved_neighbors[4] = children[3];
                        saved_neighbors[14].saved_neighbors[4] = children[2];
                    }else{
                        saved_neighbors[12].replaceNeighbor(this,children[3],null);
                        saved_neighbors[13].replaceNeighbor(this,children[3],null);
                        saved_neighbors[14].replaceNeighbor(this,children[2],null);
                    }
                }else if(saved_neighbors[15] != null){
                    children[3].saved_neighbors[12] = saved_neighbors[12];
                    children[2].saved_neighbors[12] = saved_neighbors[14];
                    children[2].saved_neighbors[14] = saved_neighbors[15];
                    if(saved_neighbors[12].Side == Side){
                        saved_neighbors[12].saved_neighbors[4] = children[3];
                        saved_neighbors[14].saved_neighbors[4] = children[2];
                        saved_neighbors[15].saved_neighbors[4] = children[2];
                    }else{
                        saved_neighbors[12].replaceNeighbor(this,children[3],null);
                        saved_neighbors[14].replaceNeighbor(this,children[2],null);
                        saved_neighbors[15].replaceNeighbor(this,children[2],null);
                    }
                }else if(saved_neighbors[14] != null){
                    children[3].saved_neighbors[12] = saved_neighbors[12];
                    children[2].saved_neighbors[12] = saved_neighbors[14];
                    if(saved_neighbors[12].Side == Side){
                        saved_neighbors[12].saved_neighbors[4] = children[3];
                        saved_neighbors[14].saved_neighbors[4] = children[2];
                    }else{
                        saved_neighbors[12].replaceNeighbor(this,children[3],null);
                        saved_neighbors[14].replaceNeighbor(this,children[2],null);
                    }
                }else{
                    children[3].saved_neighbors[12] = saved_neighbors[12];
                    children[2].saved_neighbors[12] = saved_neighbors[12];
                    if(saved_neighbors[12].Side == Side){
                        if(saved_neighbors[12].level == level){
                            saved_neighbors[12].saved_neighbors[4] = children[2];
                            saved_neighbors[12].saved_neighbors[6] = children[3];
                        }else if(childNum == 2){
                            saved_neighbors[12].saved_neighbors[4] = children[2];
                            saved_neighbors[12].saved_neighbors[5] = children[3];
                        }else{
                            saved_neighbors[12].saved_neighbors[6] = children[2];
                            saved_neighbors[12].saved_neighbors[7] = children[3];
                        }                     
                    }else{
                        if(GeoUtils.isInverse(Side,saved_neighbors[12].Side)){
                            saved_neighbors[12].replaceNeighbor(this,children[2],children[3]);
                        }else{
                            saved_neighbors[12].replaceNeighbor(this,children[3],children[2]);
                        }
                    }
                }
                master.divideList = combineList(master.divideList,children);
                master.reindexList = combineList(master.reindexList,saved_neighbors);
                master.reindexList = combineList(master.reindexList,corners());

                for(int z = 0; z < 16; z++){
                    saved_neighbors[z] = null;
                }
		for(int i = 0; i < 4; i++){
                    children[i].build_level(ignore_Chunk,false);
		}
		g.removeFromParent();
                if(initial){
                    for(int i = 0; i < master.divideList.size(); i++){
                        master.reindexList.remove(master.divideList.get(i));
                        master.reindexList.remove(master.divideList.get(i).parent);
                        master.divideList.get(i).computeNormals(false,false,name);
                    }
                    for(int i = 0; i < master.reindexList.size(); i++){
                        if(master.reindexList.get(i)!=null){
                            master.reindexList.get(i).computeNormals(false,false,name);
                        }
                    }
                    master.divideList.clear();
                    master.reindexList.clear();
                }
		//for(int i = 0; i < 4; i++){
                //    children[i].computeNormals(true,false,"Parent");
		///}
		for(int i = 0; i < 4; i++){
			children[i].finished_building = true;
		}
		this.updateGeometricState();
		//if(verbose){System.out.println("Divide: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addDivide(((System.nanoTime() - start)/1000000f));};
	}

        

        public void prep_unite(){
            boolean found = (!children[0].has_child && !children[1].has_child && !children[2].has_child && children[3].has_child && !children[3].children[0].has_child && !children[3].children[1].has_child && !children[3].children[2].has_child && !children[3].children[3].has_child) ||
                    (!children[0].has_child && !children[1].has_child && children[2].has_child && !children[3].has_child && !children[2].children[0].has_child && !children[2].children[1].has_child && !children[2].children[2].has_child && !children[2].children[3].has_child) ||
                    (!children[0].has_child && children[1].has_child && !children[2].has_child && !children[3].has_child && !children[1].children[0].has_child && !children[1].children[1].has_child && !children[1].children[2].has_child && !children[1].children[3].has_child) ||
                    (children[0].has_child && !children[1].has_child && !children[2].has_child && !children[3].has_child && !children[0].children[0].has_child && !children[0].children[1].has_child && !children[0].children[2].has_child && !children[0].children[3].has_child);
            if(found){
                for(int i = 0; i < 4; i++){
                    if(children[i].has_child){
                        System.out.println("Uniting Child: "+children[i].getName());
                        children[i].unite("P: "+name);
                    }
                }
            }else{
                boolean next = children[0].has_child || children[1].has_child || children[2].has_child || children[3].has_child;
                if(next){
                    for(int i = 0; i < 4; i++){
                        if(children[i].has_child){
                            children[i].prep_unite();
                        }
                    }
                }
            }
        }

	public void unite(String whereFrom){
		long start = System.nanoTime();
                opt.countObject(1); //remove
                System.out.println("uniting: "+getName());
                opt.write("U: "+name+" "+whereFrom);
		has_child = false;
                saved_neighbors[0] = children[2].saved_neighbors[0];
                if(saved_neighbors[0] != children[0].saved_neighbors[0] && children[0].saved_neighbors[0] != null){
                    saved_neighbors[2] = children[0].saved_neighbors[0];
                    if(saved_neighbors[0].Side == Side){
                        saved_neighbors[0].saved_neighbors[8] = this;
                        saved_neighbors[2].saved_neighbors[8] = this;
                    }else{
                        saved_neighbors[0].replaceNeighbor(children[2], this, null);
                        saved_neighbors[2].replaceNeighbor(children[0], this, null);
                    }
                }else{
                    if(saved_neighbors[0].Side == Side){
                        saved_neighbors[0].saved_neighbors[8] = this;
                        saved_neighbors[0].saved_neighbors[10] = null;
                    }else{
                        saved_neighbors[0].replaceNeighbor(children[2], this, this);
                    }
                }

                saved_neighbors[4] = children[0].saved_neighbors[4];
                if(saved_neighbors[4] != children[1].saved_neighbors[4] && children[1].saved_neighbors[4] != null){
                    saved_neighbors[6] = children[1].saved_neighbors[4];
                    if(saved_neighbors[4].Side == Side){
                        saved_neighbors[4].saved_neighbors[12] = this;
                        saved_neighbors[6].saved_neighbors[12] = this;
                    }else{
                        saved_neighbors[4].replaceNeighbor(children[0], this, null);
                        saved_neighbors[6].replaceNeighbor(children[1], this, null);
                    }
                }else{
                    if(saved_neighbors[4].Side == Side){
                        saved_neighbors[4].saved_neighbors[12] = this;
                        saved_neighbors[4].saved_neighbors[14] = null;
                    }else{                        
                        saved_neighbors[4].replaceNeighbor(children[1], this, this);                        
                    }
                }

                saved_neighbors[8] = children[1].saved_neighbors[8];
                if(saved_neighbors[8] != children[3].saved_neighbors[8] && children[3].saved_neighbors[8] != null){
                    saved_neighbors[10] = children[3].saved_neighbors[8];
                    if(saved_neighbors[8].Side == Side){
                        saved_neighbors[8].saved_neighbors[0] = this;
                        saved_neighbors[10].saved_neighbors[0] = this;
                    }else{
                        saved_neighbors[8].replaceNeighbor(children[1], this, null);
                        saved_neighbors[10].replaceNeighbor(children[3], this, null);
                    }
                }else{
                    if(saved_neighbors[8].Side == Side){
                        saved_neighbors[8].saved_neighbors[0] = this;
                        saved_neighbors[8].saved_neighbors[2] = null;
                    }else{
                        saved_neighbors[8].replaceNeighbor(children[1], this, this);
                    }
                }

                saved_neighbors[12] = children[3].saved_neighbors[12];                
                if(saved_neighbors[12] != children[2].saved_neighbors[12] && children[2].saved_neighbors[12] != null){
                    saved_neighbors[14] = children[2].saved_neighbors[12];
                    if(saved_neighbors[12].Side == Side){
                        saved_neighbors[12].saved_neighbors[4] = this;
                        saved_neighbors[14].saved_neighbors[4] = this;
                    }else{                        
                        saved_neighbors[12].replaceNeighbor(children[3], this, null);
                        saved_neighbors[14].replaceNeighbor(children[2], this, null);
                    }
                }else{
                    if(saved_neighbors[12].Side == Side){
                        saved_neighbors[12].saved_neighbors[4] = this;
                        saved_neighbors[12].saved_neighbors[6] = null;
                    }else{
                        saved_neighbors[12].replaceNeighbor(children[3], this, this);
                    }
                }
                
		for(int i = 0; i < 4; i++){
			children[i].removeFromParent();
			children[i] = null;
		}
		reindex(q,true,true);
		this.attachChild(g);
		//if(verbose){System.out.println("Unite: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addUnite(((System.nanoTime() - start)/1000000f));};
	}



        /**
         * Replaces the "find" chunk with the "replace" chunk. If a replace2 is specified it is put at location replace+2
         * @param find
         * @param replace
         * @param replace2
         */
        public void replaceNeighbor(Chunk_Spherical find, Chunk_Spherical replace, Chunk_Spherical replace2){
            for(int i = 0; i < 16; i++){
                if(saved_neighbors[i] != null){
                    if(saved_neighbors[i]==find){

                        //dumpNeighbor();
                        //System.out.println(replace+" "+i+" "+(int)(replace.level-level));

                        saved_neighbors[i] = replace;
                        int s = 2;
                        if(Math.abs(replace.level-level) == 2){
                            s = 1;
                        }                        
                        if(s == 2){
                            if(i == 2 || i == 6 || i == 10 || i == 14){                                
                                if(replace2 != null){
                                    saved_neighbors[i-s] = replace2;
                                }
                                if(replace == replace2){
                                    saved_neighbors[i] = null; //used to be i-s
                                }                                
                            }else{
                                if(replace2 != null){
                                    saved_neighbors[i+s] = replace2;
                                }
                                if(replace == replace2){
                                    saved_neighbors[i+s] = null;
                                }
                            }
                        }
                        else{
                           if(i == 1 || i == 3 || i == 5 || i == 7 || i == 9 || i == 11 || i == 13 || i == 15){
                                if(replace2 != null){
                                    saved_neighbors[i-s] = replace2;
                                }
                                if(replace == replace2){
                                    saved_neighbors[i] = null; //used to be i-s
                                }
                            }else{
                                if(replace2 != null){
                                    saved_neighbors[i+s] = replace2;
                                }
                                if(replace == replace2){
                                    saved_neighbors[i+s] = null;
                                }
                            }
                        }
                    }
                }
            }
        }

	private void build_level(ArrayList<Chunk_Spherical> ignore_Chunk, boolean calcNormal){
		compute_walls(true,ignore_Chunk,calcNormal);
		q.clearBuffer(Type.Index);
		q.setBuffer(Type.Index,3,produce_index());
		g = new Geometry("",q);
		g.setMaterial(mat);
		this.attachChild(g);
		g.setModelBound(new BoundingBox());
		g.updateModelBound();
		g.updateGeometricState();
		q.updateBound();
		g.updateModelBound();
                this.updateGeometricState();
	}
	
	private IntBuffer produce_index(){
		//assign the new index buffers
                opt.write("I: "+name);
                long start = System.nanoTime();
                for(int i = 0; i < GeoUtils.LW_MLW; i ++){
			build_normals[i].zero();
			build_normals_count[i] = 0;
		}

                IntBuffer index = GeoUtils.getIndex(Side,(GeoUtils.form_to_west(this) < level),(GeoUtils.form_to_south(this) < level),(GeoUtils.form_to_east(this) < level),(GeoUtils.form_to_north(this) < level));
		index.rewind();
                float[] vbArr = new float[GeoUtils.TC_M3];
                int[] indexArr = new int[GeoUtils.TC_M3];
                ((FloatBuffer)(q.getBuffer(Type.Position).getData()).rewind()).get(vbArr,0,GeoUtils.TC_M3);
                index.get(indexArr,0,GeoUtils.TC_M3);
                int v1 = 0;
                int v2 = 0;
                int v3 = 0;
                int v1loc = 0;
                int v2loc = 0;
                int v3loc = 0;
                Vector3f vector1 = new Vector3f();
                Vector3f vector2 = new Vector3f();
                Vector3f vector3 = new Vector3f();
                Vector3f normal = new Vector3f();
                for(int i = 0; i < GeoUtils.TC_M3; i+=3){
                    v1 = indexArr[i];
                    v2 = indexArr[i+1];
                    v3 = indexArr[i+2];
                    v1loc = v1 * 3;
                    v2loc = v2 * 3;
		    v3loc = v3 * 3;
                    vector1.set(vbArr[v1loc],vbArr[v1loc+1],vbArr[v1loc+2]);
                    vector2.set(vbArr[v2loc],vbArr[v2loc+1],vbArr[v2loc+2]);
                    vector3.set(vbArr[v3loc],vbArr[v3loc+1],vbArr[v3loc+2]);
                    vector1.subtract(vector3,vector1);
                    normal = vector1.cross(vector3.subtract(vector2)).normalize();

                    build_normals[v1] = normal.add(build_normals[v1]);
                    build_normals_count[v1]++;

                    build_normals[v2] = normal.add(build_normals[v2]);
                    build_normals_count[v2]++;

                    build_normals[v3] = normal.add(build_normals[v3]);
                    build_normals_count[v3]++;
                }
                //if(verbose){System.out.println("Index: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addIndex(((System.nanoTime() - start)/1000000f));};
                
                return index;
                   
	}
	
	private void produce_vertex(){
                opt.write("V: "+name);
		long start = System.nanoTime();
		//build new vertex buffer
		FloatBuffer fb_new = BufferUtils.createVector3Buffer(GeoUtils.TC_M3);
		double increase = ((detail/ Math.pow(2,level))/(level_width-1));
		//assign the new vertexbuffer
		if(level == 0){
			double current_y = posY;
			for(double y = 0;y < level_width; y++){
				double current_x = posX;
				for(double x = 0;x < level_width; x++){	
					double[] loc2 = GeoUtils.orient_location(((size*(x))/(level_width-1)+minX),0,((size*(y))/(level_width-1)+minY),Side);
					double c_x = loc2[0];
					double c_y = loc2[1];
					double c_z = loc2[2];
					double new_x = c_x * Math.sqrt(  1 - ((c_y*c_y)/2) -  ((c_z*c_z)/2)  + ((c_y*c_y*c_z*c_z)/3)    );
					double new_y = c_y * Math.sqrt(  1 - ((c_z*c_z)/2) -  ((c_x*c_x)/2)  + ((c_z*c_z*c_x*c_x)/3)    );
					double new_z = c_z * Math.sqrt(  1 - ((c_x*c_x)/2) -  ((c_y*c_y)/2)  + ((c_x*c_x*c_y*c_y)/3)    );
					double scalar = n.fancy_noise(new_x,new_y,new_z)*noise_scale;
					
					double new_x_1 = new_x * radius;
					double new_x_2 = new_x * scalar;
					double new_y_1 = new_y * radius;
					double new_y_2 = new_y * scalar;
					double new_z_1 = new_z * radius;
					double new_z_2 = new_z * scalar;
					
//					Vector3f loc = new Vector3f((float)new_x,(float)new_y,(float)new_z);
//					loc = loc.mult(radius).add(loc.mult((float) scalar));
					fb_new.put((float) (new_x_1+new_x_2)).put((float) (new_y_1+new_y_2)).put((float) (new_z_1+new_z_2));					
					current_x += increase;
				}
				current_y += increase;			
			}
		}else{
			FloatBuffer p_vert = (FloatBuffer) parent.q.getBuffer(Type.Position).getData();
			boolean xodd = false;
			boolean yodd = false;
			int vertNum = 0;
			switch(childNum){
				case(0): vertNum = 0;break;
				case(1): vertNum = GeoUtils.level_width/2;break;
				case(2): vertNum = (GeoUtils.level_width/2)*GeoUtils.level_width;break;
				case(3): vertNum = ((GeoUtils.level_width/2)*GeoUtils.level_width)+(GeoUtils.level_width/2);break;
			}
			int buffer_offset = 0;
			int side_difference = 0;
			int level_invert = 0;
			switch(Side){
				case(0): buffer_offset = 1; side_difference = -1; level_invert = 1; break;
				case(1): buffer_offset = 2; side_difference = -1; level_invert = 1; break;
				case(2): buffer_offset = 0; side_difference = 1; level_invert = -1; break;
				case(3): buffer_offset = 0; side_difference = -1; level_invert = 1; break;
				case(4): buffer_offset = 2; side_difference = 1; level_invert = -1; break;
				case(5): buffer_offset = 1; side_difference = 1; level_invert = -1; break;
			}
			int tmpvert = vertNum;
			double current_y = posY;
			for(int y = 0;y < level_width; y++){
				double current_x = posX;
				xodd=false;
				for(int x = 0;x < level_width; x++){
					float y_value = 0;
					if(!yodd && !xodd){
						y_value = (p_vert.get(tmpvert*3+buffer_offset)+side_difference)*level_invert;
						tmpvert++;
					}else{
			//			y_value = (float) n.fancy_noise(current_x,current_y)*noise_scale;
					}
					
					double[] loc2 = GeoUtils.orient_location(((size*(x))/(level_width-1)+minX),0,((size*(y))/(level_width-1)+minY),Side);
					double c_x = loc2[0];
					double c_y = loc2[1];
					double c_z = loc2[2];
					double new_x = c_x * Math.sqrt(  1 - ((c_y*c_y)/2) -  ((c_z*c_z)/2)  + ((c_y*c_y*c_z*c_z)/3)    );
					double new_y = c_y * Math.sqrt(  1 - ((c_z*c_z)/2) -  ((c_x*c_x)/2)  + ((c_z*c_z*c_x*c_x)/3)    );
					double new_z = c_z * Math.sqrt(  1 - ((c_x*c_x)/2) -  ((c_y*c_y)/2)  + ((c_x*c_x*c_y*c_y)/3)    );
					double scalar = n.fancy_noise(new_x,new_y,new_z)*noise_scale;
					
					double new_x_1 = new_x * radius;
					double new_x_2 = new_x * scalar;
					double new_y_1 = new_y * radius;
					double new_y_2 = new_y * scalar;
					double new_z_1 = new_z * radius;
					double new_z_2 = new_z * scalar;
					
					fb_new.put((float) (new_x_1+new_x_2)).put((float) (new_y_1+new_y_2)).put((float) (new_z_1+new_z_2));
					current_x += increase;
					xodd=!xodd;
				}
				yodd=!yodd;
				if(yodd){
					vertNum+=level_width;
					tmpvert = vertNum;
				}
				current_y += increase;			
			}
		}
		q.clearBuffer(Type.Position);
		q.setBuffer(Type.Position,3,fb_new);
		int position = ((int) (((int)(level_width/2)*level_width)+((int)(level_width/2))))*3;
		midpoint = new Vector3f(fb_new.get(position),fb_new.get(position+1),fb_new.get(position+2));
		//if(verbose){System.out.println("Vertex: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addVertex(((System.nanoTime() - start)/1000000f));};
	}
	
	
	
	private void compute_walls(boolean inform, ArrayList<Chunk_Spherical> ignore_Chunk, boolean calcNormal){
                opt.write("W: "+name);
            	long start = System.nanoTime();
		if(saved_neighbors[0] == null){
			//form_to_west = level;
		}else{
			if(saved_neighbors[0].Side != Side){
                            int[] t = GeoUtils.normal_targets(Side,0);
                            if(saved_neighbors[0] != null){
                                west_wall(saved_neighbors[0],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                if(saved_neighbors[2] != null){
                                    west_wall(saved_neighbors[2],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    if(saved_neighbors[1] != null){
                                      west_wall(saved_neighbors[1],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    }
                                    if(saved_neighbors[3] != null){
                                      west_wall(saved_neighbors[3],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    }
                                }
                            }
			}else{
                            if(saved_neighbors[0] != null){
                                west_wall(saved_neighbors[0],inform,1,3,ignore_Chunk,calcNormal);
                                if(saved_neighbors[2] != null){
                                    west_wall(saved_neighbors[2],inform,1,3,ignore_Chunk,calcNormal);
                                    if(saved_neighbors[1] != null){
                                      west_wall(saved_neighbors[1],inform,1,3,ignore_Chunk,calcNormal);
                                    }
                                    if(saved_neighbors[3] != null){
                                      west_wall(saved_neighbors[3],inform,1,3,ignore_Chunk,calcNormal);
                                    }
                                }
                            }
			}             
		}
		
		if(saved_neighbors[4] == null){
			//form_to_south = level;
		}else{
			if(saved_neighbors[4].Side != Side){
				int[] t = GeoUtils.normal_targets(Side,1);
                                if(saved_neighbors[4] != null){
                                    south_wall(saved_neighbors[4],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    if(saved_neighbors[6] != null){
                                        south_wall(saved_neighbors[6],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        if(saved_neighbors[5] != null){
                                            south_wall(saved_neighbors[5],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[7] != null){
                                            south_wall(saved_neighbors[7],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}else{
                            if(saved_neighbors[4] != null){
                                    south_wall(saved_neighbors[4],inform,2,3,ignore_Chunk,calcNormal);
                                    if(saved_neighbors[6] != null){
                                        south_wall(saved_neighbors[6],inform,2,3,ignore_Chunk,calcNormal);
                                        if(saved_neighbors[5] != null){
                                            south_wall(saved_neighbors[5],inform,2,3,ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[7] != null){
                                            south_wall(saved_neighbors[7],inform,2,3,ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}              
		}
		
		if(saved_neighbors[8] == null){
			//form_to_east = level;
		}else{
			if(saved_neighbors[8].Side != Side){
				int[] t = GeoUtils.normal_targets(Side,2);
                                if(saved_neighbors[8] != null){
                                    east_wall(saved_neighbors[8],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    if(saved_neighbors[10] != null){
                                        east_wall(saved_neighbors[10],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        if(saved_neighbors[9] != null){
                                           east_wall(saved_neighbors[9],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[11] != null){
                                           east_wall(saved_neighbors[11],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}else{
                            for(int i = 8; i < 12; i++){
                                if(saved_neighbors[i] !=  null){
                                    east_wall(saved_neighbors[i],inform,0,2,ignore_Chunk,calcNormal);
                                }
                            }
                            if(saved_neighbors[8] != null){
                                    east_wall(saved_neighbors[8],inform,0,2,ignore_Chunk,calcNormal);
                                    if(saved_neighbors[10] != null){
                                        east_wall(saved_neighbors[10],inform,0,2,ignore_Chunk,calcNormal);
                                        if(saved_neighbors[9] != null){
                                           east_wall(saved_neighbors[9],inform,0,2,ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[11] != null){
                                           east_wall(saved_neighbors[11],inform,0,2,ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}          
		}
		
		if(saved_neighbors[12] == null){
			//form_to_north = level;
		}else{

			if(saved_neighbors[12].Side != Side){
				int[] t = GeoUtils.normal_targets(Side,3);
                                if(saved_neighbors[12] != null){
                                    north_wall(saved_neighbors[12],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                    if(saved_neighbors[14] != null){
                                        north_wall(saved_neighbors[14],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        if(saved_neighbors[13] != null){
                                            north_wall(saved_neighbors[13],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[15] != null){
                                            north_wall(saved_neighbors[15],inform,t[0],t[1],ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}else{
                            if(saved_neighbors[12] != null){
                                    north_wall(saved_neighbors[12],inform,0,1,ignore_Chunk,calcNormal);
                                    if(saved_neighbors[14] != null){
                                        north_wall(saved_neighbors[14],inform,0,1,ignore_Chunk,calcNormal);
                                        if(saved_neighbors[13] != null){
                                            north_wall(saved_neighbors[13],inform,0,1,ignore_Chunk,calcNormal);
                                        }
                                        if(saved_neighbors[15] != null){
                                            north_wall(saved_neighbors[15],inform,0,1,ignore_Chunk,calcNormal);
                                        }
                                    }
                                }
			}        
		}
                

                //if(verbose){System.out.println("Walls: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addWall(((System.nanoTime() - start)/1000000f));};
	}
	
	private void west_wall(Chunk_Spherical c, boolean inform, int t1, int t2, ArrayList<Chunk_Spherical> ignore_Chunk, boolean calcNormal){
		//the purpose here is to go get the level of the west wall, and also let any children of c below this.level
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(0,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					west_wall(masterBlock.children[t1],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(0,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					west_wall(masterBlock.children[t2],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
		}else{
			if(masterBlock.level >= level){
				//form_to_west = level;
				if(inform){
					//warn them about the division and let them change!
					//System.out.println(masterBlock+" "+parent+" "+masterBlock.Side+" "+Side+" "+masterBlock.level+" "+level);
					if(masterBlock.level == 0 || level == 0){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}else if(masterBlock != parent.children[0] && masterBlock != parent.children[2]){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}
				}
			}else{
				if(level - masterBlock.level > 1){
                                        ArrayList<Chunk_Spherical> arr = new ArrayList<Chunk_Spherical>(4);
                                        arr.add(parent.children[0]);
                                        arr.add(parent.children[1]);
                                        arr.add(parent.children[2]);
                                        arr.add(parent.children[3]);
					masterBlock.divide("west wall "+name,arr,false);
					west_wall(masterBlock,inform,t1,t2,ignore_Chunk,calcNormal);
				}else{
					//form_to_west = masterBlock.level;
				}
			}
		}
	}
	
	private void east_wall(Chunk_Spherical c, boolean inform, int t1, int t2, ArrayList<Chunk_Spherical> ignore_Chunk,boolean calcNormal){
		//the purpose here is to go get the level of the west wall, and also let any children of c below this.level
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(2,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					east_wall(masterBlock.children[t1],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(2,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					east_wall(masterBlock.children[t2],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
		}else{
			if(masterBlock.level >= level){
				//form_to_east = level;
				if(inform){
					//warn them about the division and let them change!
					//System.out.println(masterBlock+" "+parent+" "+Side+" "+level);
					if(masterBlock.level == 0 || level == 0){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}else if(masterBlock != parent.children[1] && masterBlock != parent.children[3]){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}				
				}
			}else{
				if(level - masterBlock.level > 1){
                                        ArrayList<Chunk_Spherical> arr = new ArrayList<Chunk_Spherical>(4);
                                        arr.add(parent.children[0]);
                                        arr.add(parent.children[1]);
                                        arr.add(parent.children[2]);
                                        arr.add(parent.children[3]);
					masterBlock.divide("east wall "+name,arr,false);
					east_wall(masterBlock,inform,t1,t2,ignore_Chunk,calcNormal);
				}else{
					//form_to_east = masterBlock.level;
				}
			}
		}
	}
	
	private void south_wall(Chunk_Spherical c, boolean inform, int t1, int t2, ArrayList<Chunk_Spherical> ignore_Chunk, boolean calcNormal){
		//the purpose here is to go get the level of the west wall, and also let any children of c below this.level
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(1,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					south_wall(masterBlock.children[t1],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(1,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					south_wall(masterBlock.children[t2],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
		}else{
			if(masterBlock.level >= level){
				//form_to_south = level;
				if(inform){
					//warn them about the division and let them change!
					if(masterBlock.level == 0 || level == 0){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}else if((masterBlock != parent.children[0] && masterBlock != parent.children[1] ) || masterBlock.Side != Side){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}	
				}
			}else{
				if(level - masterBlock.level > 1){
                                    ArrayList<Chunk_Spherical> arr = new ArrayList<Chunk_Spherical>(4);
                                        arr.add(parent.children[0]);
                                        arr.add(parent.children[1]);
                                        arr.add(parent.children[2]);
                                        arr.add(parent.children[3]);
					masterBlock.divide("south wall "+name,arr,false);
					south_wall(masterBlock,inform,t1,t2,ignore_Chunk,calcNormal);
				}else{
					//form_to_south = masterBlock.level;
				}
			}
		}
	}
	
	private void north_wall(Chunk_Spherical c, boolean inform, int t1, int t2, ArrayList<Chunk_Spherical> ignore_Chunk, boolean calcNormal){
		//the purpose here is to go get the level of the west wall, and also let any children of c below this.level
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(3,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					north_wall(masterBlock.children[t1],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(3,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					north_wall(masterBlock.children[t2],inform,t1,t2,ignore_Chunk,calcNormal);
				}
			}
		}else{
			if(masterBlock.level >= level){
				//form_to_north = level;
				if(inform){
					//warn them about the division and let them change!
					if(masterBlock.level == 0 || level == 0){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}else if((masterBlock != parent.children[2] && masterBlock != parent.children[3]) || masterBlock.Side != Side){
                                            if(ignore_Chunk!=null){
                                                if(!ignore_Chunk.contains(masterBlock)){
                                                    if(!calcNormal){
                                                        master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                        master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                    }
                                                    masterBlock.reindex(masterBlock.q,false,calcNormal);
                                                }
                                            }else{
                                                if(!calcNormal){
                                                    master.reindexList = combineList(master.reindexList,masterBlock.parent.children);
                                                    master.reindexList = combineList(master.reindexList,masterBlock.corners());
                                                }
                                                masterBlock.reindex(masterBlock.q,false,calcNormal);
                                            }
					}
				}
			}else{
				if(level - masterBlock.level > 1){
                                    ArrayList<Chunk_Spherical> arr = new ArrayList<Chunk_Spherical>(4);
                                        arr.add(parent.children[0]);
                                        arr.add(parent.children[1]);
                                        arr.add(parent.children[2]);
                                        arr.add(parent.children[3]);
					masterBlock.divide("north wall "+name,arr,false);
					north_wall(masterBlock,inform,t1,t2,ignore_Chunk,calcNormal);
				}else{
					//form_to_north = masterBlock.level;
				}
			}
		}
	}
	
	


	private void reindex(Mesh q, boolean unite, boolean calcNormal){
                opt.write("R: "+name);
		if(unite){
			compute_walls(true,null,calcNormal);
		}else{
			compute_walls(false,null,calcNormal);
		}

		//reassign the index buffer to q
		q.clearBuffer(Type.Index);
		q.setBuffer(Type.Index,3,produce_index());
                if(calcNormal){
                    computeNormals(true,true,"reindex");
                }
	}
	
	void computeNormals(boolean recursive, boolean reindex, String executor) {
                opt.write("N: "+name+" "+executor);
		long start = System.nanoTime();                
		adjust_normals(recursive,reindex);
		int bottom_normals_count = 0;
		int top_normals_count = 0;
		int left_normals_count = 0;
		int right_normals_count = 0;		
		Vector3f[] normals = new Vector3f[q.getVertexCount()];

		for(int y = 0; y < GeoUtils.level_width; y++){
                        int xVar = (y*GeoUtils.level_width)+(GeoUtils.level_width);
			for(int x = y*GeoUtils.level_width; x < xVar; x++){
				if(y==0){
					normals[x] =  (build_normals[x].add(buffer_normal_bottom[bottom_normals_count])).divide((-(build_normals_count[x]+buffer_normal_bottom_count[bottom_normals_count]))).normalize();
					bottom_normals_count++;
				}else if(y==GeoUtils.LW_S1){
					normals[x] =  (build_normals[x].add(buffer_normal_top[top_normals_count])).divide((-(build_normals_count[x]+buffer_normal_top_count[top_normals_count]))).normalize();
					top_normals_count++;
				}else{
					if(x == y*GeoUtils.level_width){
						normals[x] =  (build_normals[x].add(buffer_normal_left[left_normals_count])).divide((-(build_normals_count[x]+buffer_normal_left_count[left_normals_count]))).normalize();
						left_normals_count++;
					}else if(x==(y*GeoUtils.level_width)+GeoUtils.LW_S1){
						normals[x] =  (build_normals[x].add(buffer_normal_right[right_normals_count])).divide((-(build_normals_count[x]+buffer_normal_right_count[right_normals_count]))).normalize();
						right_normals_count++;
					}else{
						normals[x] =  build_normals[x].divide((-build_normals_count[x])).normalize();
					}
				}
			}
		}		
		q.clearBuffer(Type.Normal);
		q.setBuffer(Type.Normal,3,BufferUtils.createFloatBuffer(normals));
		//if(verbose){System.out.println("Normals: "+((System.nanoTime() - start)/1000000f)+" Milliseconds");};
                if(verbose){opt.addNormal(((System.nanoTime() - start)/1000000f));};
	}
	
	
	
	private void adjust_normals(boolean recursize, boolean reindex){
		if(saved_neighbors[12]!=null){
			for(int i = 0; i < GeoUtils.level_width; i++){
				buffer_normal_top[i].zero();
				buffer_normal_top_count[i] = 0;
			}
                        int[] t = new int[2];
                        t[0] = 0; t[1] = 1;
			if(saved_neighbors[12].Side != Side){
                            t=GeoUtils.normal_targets(Side,3);
                        }
			neighbor_normals_north(saved_neighbors[12], recursize, reindex, t[0], t[1]);
                        if(saved_neighbors[14] != null){
                             neighbor_normals_north(saved_neighbors[14], recursize, reindex, t[0], t[1]);
                             if(saved_neighbors[13] != null){
                                 neighbor_normals_north(saved_neighbors[13], recursize, reindex, t[0], t[1]);
                             }
                             if(saved_neighbors[15] != null){
                                 neighbor_normals_north(saved_neighbors[15], recursize, reindex, t[0], t[1]);
                             }
                        }
			
		}
		if(saved_neighbors[4]!=null){
			for(int i = 0; i < GeoUtils.level_width; i++){
				buffer_normal_bottom[i].zero();
				buffer_normal_bottom_count[i] = 0;
			}
                        int[] t = new int[2];
                        t[0] = 2; t[1] = 3;
			if(saved_neighbors[4].Side != Side){
				t = GeoUtils.normal_targets(Side,1);
                        }
			neighbor_normals_south(saved_neighbors[4], recursize, reindex, t[0], t[1]);
                        if(saved_neighbors[6] != null){
                            neighbor_normals_south(saved_neighbors[6], recursize, reindex, t[0], t[1]);
                            if(saved_neighbors[5] != null){
                                neighbor_normals_south(saved_neighbors[5], recursize, reindex, t[0], t[1]);
                            }
                            if(saved_neighbors[7] != null){
                                neighbor_normals_south(saved_neighbors[7], recursize, reindex, t[0], t[1]);
                            }
                        }
		}
		if(saved_neighbors[0]!=null){
			for(int i = 0; i < GeoUtils.LW_S2; i++){
				buffer_normal_left[i].zero();
				buffer_normal_left_count[i] = 0;
			}
                        int[] t = new int[2];
                        t[0] = 1; t[1] = 3;
			if(saved_neighbors[0].Side != Side){
				t = GeoUtils.normal_targets(Side,0);
                        }
			neighbor_normals_west(saved_neighbors[0], recursize, reindex, t[0], t[1]);
                        if(saved_neighbors[2] != null){
                            neighbor_normals_west(saved_neighbors[2], recursize, reindex, t[0], t[1]);
                            if(saved_neighbors[1] != null){
                               neighbor_normals_west(saved_neighbors[1], recursize, reindex, t[0], t[1]);
                            }
                            if(saved_neighbors[3] != null){
                               neighbor_normals_west(saved_neighbors[3], recursize, reindex, t[0], t[1]);
                            }
                        }
			
		}		
		if(saved_neighbors[8]!=null){
			for(int i = 0; i < GeoUtils.LW_S2; i++){
				buffer_normal_right[i].zero();
				buffer_normal_right_count[i] = 0;
			}
                        int[] t = new int[2];
                        t[0] = 0; t[1] = 2;
			if(saved_neighbors[8].Side != Side){
				t = GeoUtils.normal_targets(Side,2);
                        }
			neighbor_normals_east(saved_neighbors[8], recursize, reindex, t[0], t[1]);
			if(saved_neighbors[10] != null){
                            neighbor_normals_east(saved_neighbors[10], recursize, reindex, t[0], t[1]);
                            if(saved_neighbors[9] != null){
                               neighbor_normals_east(saved_neighbors[9], recursize, reindex, t[0], t[1]);
                            }
                            if(saved_neighbors[11] != null){
                               neighbor_normals_east(saved_neighbors[11], recursize, reindex, t[0], t[1]);
                            }
                        }
		}
	}
	

	private void neighbor_normals_northeast_corner(Chunk_Spherical master, Chunk_Spherical c, boolean recursive, int stage, int t1, int t2){
		if(c != null){

			if(stage == 0){
				if(c.has_child && master.level > c.level){
					//TODO:
					if(c.children[t1]!=null){
						double[] targets = GeoUtils.gather_target(3,c.children[t1],master.Side,Side);
                                                double barrier[] = GeoUtils.gather_limit(1,1,c.children[t1],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && maxY == barrier[0]){
							neighbor_normals_northeast_corner(master,c.children[t1],recursive,0,t1,t2);
						}
					}
					if(c.children[t2]!=null){
						double[] targets = GeoUtils.gather_target(3,c.children[t2],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(1,1,c.children[t2],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && maxY == barrier[0]){
							neighbor_normals_northeast_corner(master,c.children[t2],recursive,0,t1,t2);
						}
					}
				}else{
					neighbor_normals_northeast_corner(master,c,recursive,1,t1,t2);
				}			
			}else if(stage == 1){
				if(c.has_child){
					if(c.children[t1]!=null){
                                            double[] barrier = GeoUtils.gather_limit(1,1,c.children[t1],master,Side);
                                            if(maxY == barrier[0] && maxX == barrier[1]){
						neighbor_normals_northeast_corner(master,c.children[t1],recursive,1,t1,t2);
                                            }
					}
                                        if(c.children[t2]!=null && c.Side != Side){
                                            double[] barrier = GeoUtils.gather_limit(1,1,c.children[t2],master,Side);
                                            if(maxY == barrier[0] && maxX == barrier[1]){
						neighbor_normals_northeast_corner(master,c.children[t2],recursive,1,t1,t2);
                                            }
					}
				}else{
					neighbor_normals_northeast_corner(master,c,recursive,2,t1,t2);
				}
			}else{
				if(c.level != 0 && !( (childNum == 2 || childNum == 1) && c.level < level) && GeoUtils.compare_sides(Side,saved_neighbors[8].Side,saved_neighbors[12].Side)){
                                    int i = GeoUtils.normal_corner(c.Side,3,Side);
					buffer_normal_top[GeoUtils.LW_S1]= buffer_normal_top[GeoUtils.LW_S1].add(c.build_normals[i]);
					buffer_normal_top_count[GeoUtils.LW_S1]+=c.build_normals_count[i];
				}
				if(recursive){
					if(c.parent != parent){
						c.computeNormals(false,false,this.getName()+" NEC");
					}
				}
			}	
		}
	}
	
	private void neighbor_normals_northwest_corner(Chunk_Spherical master, Chunk_Spherical c, boolean recursive, int stage, int t1, int t2){
		if(c != null){                               
			if(stage == 0){
				if(c.has_child && master.level > c.level){
					if(c.children[t1]!=null){
						double[] targets = GeoUtils.gather_target(3,c.children[t1],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(1,0,c.children[t1],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && maxY == barrier[0]){
							neighbor_normals_northwest_corner(master,c.children[t1],recursive,0,t1,t2);
						}
					}
					if(c.children[t2]!=null){
						double[] targets = GeoUtils.gather_target(3,c.children[t2],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(1,0,c.children[t2],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && maxY == barrier[0]){
							neighbor_normals_northwest_corner(master,c.children[t2],recursive,0,t1,t2);
						}
					}
				}else{
					neighbor_normals_northwest_corner(master,c,recursive,1,t1,t2);
				}			
			}else if(stage == 1){
				if(c.has_child){
					if(c.children[t1]!=null && c.Side != Side){
                                                double[] barrier = GeoUtils.gather_limit(1,0,c.children[t1],master,Side);
                                                if(maxY == barrier[0] && minX == barrier[1]){
                                                    neighbor_normals_northwest_corner(master,c.children[t1],recursive,1,t1,t2);
                                                }
					}
                                        if(c.children[t2]!=null){
                                                double[] barrier = GeoUtils.gather_limit(1,0,c.children[t2],master,Side);
                                                if(maxY == barrier[0] && minX == barrier[1]){
                                                    neighbor_normals_northwest_corner(master,c.children[t2],recursive,1,t1,t2);
                                                }
					}
				}else{
					neighbor_normals_northwest_corner(master,c,recursive,2,t1,t2);
				}
			}else{
				if(c.level != 0 && !( (childNum == 3 || childNum == 0) && c.level < level) && GeoUtils.compare_sides(Side,saved_neighbors[0].Side,saved_neighbors[12].Side)){
					int i = GeoUtils.normal_corner(c.Side,2,Side);
					buffer_normal_top[0]= buffer_normal_top[0].add(c.build_normals[i]);
					buffer_normal_top_count[0]+=c.build_normals_count[i];				
				}
				if(recursive){
					if(c.parent != parent){
						c.computeNormals(false,false,this.getName()+" NWC");
					}
				}	
			}
		}
	}
	
	private void neighbor_normals_southwest_corner(Chunk_Spherical master, Chunk_Spherical c, boolean recursive, int stage, int t1, int t2){
		if(c != null){
			if(stage == 0){
				if(c.has_child && master.level > c.level){
					if(c.children[t1]!=null){
						double[] targets = GeoUtils.gather_target(1,c.children[t1],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(0,0,c.children[t1],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && minY == barrier[0]){
							neighbor_normals_southwest_corner(master,c.children[t1],recursive,0,t1,t2);
						}
					}
					if(c.children[t2]!=null){
						double[] targets = GeoUtils.gather_target(1,c.children[t2],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(0,0,c.children[t2],master,Side);

                                                    if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && minY == barrier[0]){
							neighbor_normals_southwest_corner(master,c.children[t2],recursive,0,t1,t2);
                                                    }
					}
				}else{
					neighbor_normals_southwest_corner(master,c,recursive,1,t1,t2);
				}			
			}else if(stage == 1){
				if(c.has_child){
                                    if(c.children[t1]!=null && c.Side != Side){
                                            double[] barrier = GeoUtils.gather_limit(0,0,c.children[t1],master,Side);
                                            if(minY == barrier[0] && minX == barrier[1]){
						neighbor_normals_southwest_corner(master,c.children[t1],recursive,1,t1,t2);
                                            }
					}
					if(c.children[t2]!=null){
                                            double[] barrier = GeoUtils.gather_limit(0,0,c.children[t2],master,Side);
                                            if(minY == barrier[0] && minX == barrier[1]){
						neighbor_normals_southwest_corner(master,c.children[t2],recursive,1,t1,t2);
                                            }
					}
				}else{
					neighbor_normals_southwest_corner(master,c,recursive,2,t1,t2);
				}
			}else{
				if(c.level != 0 && !( (childNum == 1 || childNum == 2) && c.level < level) && GeoUtils.compare_sides(Side,saved_neighbors[0].Side,saved_neighbors[4].Side)){
                                        int i = GeoUtils.normal_corner(c.Side,0,Side);
					buffer_normal_bottom[0]= buffer_normal_bottom[0].add(c.build_normals[i]);
					buffer_normal_bottom_count[0]+=c.build_normals_count[i];				
				}
				if(recursive){
					if(c.parent != parent){
						c.computeNormals(false,false,this.getName()+" SWC");
					}
				}	
			}
		}
	}
	
	private void neighbor_normals_southeast_corner(Chunk_Spherical master, Chunk_Spherical c, boolean recursive, int stage, int t1, int t2){
		if(c != null){
			if(stage == 0){
				if(c.has_child && master.level > c.level){
					if(c.children[t1]!=null){
						double[] targets = GeoUtils.gather_target(1,c.children[t1],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(0,1,c.children[t1],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && minY == barrier[0]){
							neighbor_normals_southeast_corner(master,c.children[t1],recursive,0,t1,t2);
						}
					}
					if(c.children[t2]!=null){
						double[] targets = GeoUtils.gather_target(1,c.children[t2],master.Side,Side);
                                                double[] barrier = GeoUtils.gather_limit(0,1,c.children[t2],master,Side);
						if(targets[1] >= barrier[3] && targets[0] <= barrier[2] && minY == barrier[0]){
							neighbor_normals_southeast_corner(master,c.children[t2],recursive,0,t1,t2);
						}
					}
				}else{
					neighbor_normals_southeast_corner(master,c,recursive,1,t1,t2);
				}			
			}else if(stage == 1){
				if(c.has_child){
					if(c.children[t1]!=null){
                                            double[] barrier = GeoUtils.gather_limit(0,1,c.children[t1],master,Side);
                                            if(minY == barrier[0] && maxX == barrier[1]){
                                                neighbor_normals_southeast_corner(master,c.children[t1],recursive,1,t1,t2);
                                            }
					}
                                        if(c.children[t2]!=null && c.Side != Side){
                                            double[] barrier = GeoUtils.gather_limit(0,1,c.children[t2],master,Side);
                                            if(minY == barrier[0] && maxX == barrier[1]){
                                                neighbor_normals_southeast_corner(master,c.children[t2],recursive,1,t1,t2);
                                            }
					}
				}else{
					neighbor_normals_southeast_corner(master,c,recursive,2,t1,t2);
				}
			}else{
				if(c.level != 0 && !( (childNum == 0 || childNum == 3) && c.level < level) && GeoUtils.compare_sides(Side,saved_neighbors[4].Side,saved_neighbors[8].Side)){
					int i = GeoUtils.normal_corner(c.Side,1,Side);
					buffer_normal_bottom[GeoUtils.LW_S1]= buffer_normal_bottom[GeoUtils.LW_S1].add(c.build_normals[i]);
					buffer_normal_bottom_count[GeoUtils.LW_S1]+=c.build_normals_count[i];
				}
				if(recursive){
					if(c.parent != parent){
						c.computeNormals(false,false,this.getName()+" SEC");
					}
				}
			}
		}
	}
	
	

        
	
	
	private void neighbor_normals_east(Chunk_Spherical masterBlock, boolean recursive, boolean reindex, int t1, int t2){
                if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(2,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					neighbor_normals_east(masterBlock.children[t1],recursive,reindex,t1,t2);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(2,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					neighbor_normals_east(masterBlock.children[t2],recursive,reindex,t1,t2);
				}
			}
		}else{
			if(masterBlock.level == level){
				int top_loc = 0;
				int bottom_loc = 0;
				int increase = 0;
				int neighbor_loc = 0;
				if( t1 == 0 && t2 == 1){
					top_loc = GeoUtils.LW_S1;
					bottom_loc = 0;
					increase = 1;
				}else if( t1 == 1 && t2 == 3){
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_S1;
					increase = GeoUtils.level_width;
				}else if( t1 == 2 && t2 == 3){					
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_MCLWS1;
					increase = 1;
				}else if( t1 == 0 && t2 == 2){
					top_loc = GeoUtils.LW_MCLWS1;
					bottom_loc = 0;
					increase = GeoUtils.level_width;
				}
				neighbor_loc = bottom_loc + increase;
				
				buffer_normal_bottom[GeoUtils.LW_S1]= buffer_normal_bottom[GeoUtils.LW_S1].add(masterBlock.build_normals[bottom_loc]);
				buffer_normal_bottom_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[bottom_loc];
				buffer_normal_top[GeoUtils.LW_S1]= buffer_normal_top[GeoUtils.LW_S1].add(masterBlock.build_normals[top_loc]);
				buffer_normal_top_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[top_loc];
				for(int i = 0; i < GeoUtils.LW_S2; i++){
					buffer_normal_right[i]= buffer_normal_right[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_right_count[i]+=masterBlock.build_normals_count[neighbor_loc];	
					neighbor_loc += increase;
				}
				if(level != 0){
					//Here we take care of the corner normals
					int down =  GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != /**masterBlock**/Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,t[0],t[1]);
						}else{

							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,2,3);
						}
					}
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,0,1);
						}
					}
				}
			}else if(masterBlock.level < level){
				//we need to skip every other vertex
				int top_loc = 0; //location of top normal buffer on target
				int bottom_loc = 0; //location of bottom normal buffer on target
				int increase = 0; //amount to increase neighbor_loc per incriment
				int neighbor_loc = 0; //starting location for this sides buffers
				if(childNum==1){
					if( t1 == 0 && t2 == 1){
						top_loc = GeoUtils.level_width/2;
						bottom_loc = 0;
						increase = 1;
					}else if( t1 == 1 && t2 == 3){
						top_loc =  (((GeoUtils.level_width)/2)*(GeoUtils.level_width)) + GeoUtils.LW_S1;
						bottom_loc = GeoUtils.LW_S1;
						increase = GeoUtils.level_width;
					}else if( t1 == 2 && t2 == 3){					
						top_loc = GeoUtils.LW_MLW_S1-(GeoUtils.level_width/2);
						bottom_loc = GeoUtils.LW_MCLWS1;
						increase = 1;
					}else if( t1 == 0 && t2 == 2){
						top_loc = (GeoUtils.level_width/2) * GeoUtils.level_width;
						bottom_loc = 0;
						increase = GeoUtils.level_width;
					}
				}else{ //childNum must be 3, because 0 and 2 with east normals are inside the parent, so neighbor cannot be greater
					if( t1 == 0 && t2 == 1){
						top_loc = GeoUtils.LW_S1;
						bottom_loc = GeoUtils.level_width/2;
						increase = 1;
					}else if( t1 == 1 && t2 == 3){
						top_loc = GeoUtils.LW_MLW_S1;
						bottom_loc = ((GeoUtils.level_width/2)*GeoUtils.level_width) + GeoUtils.LW_S1;
						increase = GeoUtils.level_width;
					}else if( t1 == 2 && t2 == 3){					
						top_loc = GeoUtils.LW_MLW_S1;
						bottom_loc = GeoUtils.LW_MLW_S1-(GeoUtils.level_width/2);
						increase = 1;
					}else if( t1 == 0 && t2 == 2){
						top_loc = GeoUtils.LW_MCLWS1;
						bottom_loc = (GeoUtils.level_width/2) * GeoUtils.level_width;
						increase = GeoUtils.level_width;
					}
				}	
				neighbor_loc = bottom_loc + increase;
				
				buffer_normal_bottom[GeoUtils.LW_S1]= buffer_normal_bottom[GeoUtils.LW_S1].add(masterBlock.build_normals[bottom_loc]);
				buffer_normal_bottom_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[bottom_loc];
				buffer_normal_top[GeoUtils.LW_S1]= buffer_normal_top[GeoUtils.LW_S1].add(masterBlock.build_normals[top_loc]);
				buffer_normal_top_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[top_loc];
				
				for(int i = 1; i < GeoUtils.LW_S2; i+=2){
					buffer_normal_right[i]= buffer_normal_right[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_right_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc+=increase;
				}

				//Again, corner normals
				if(childNum == 3){
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,0,1);
						}
					}
				}
				if(childNum == 1){
					int down = GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,2,3);
						}
					}
				}	
			}else{
				//masterBlocks level is greater than (higher detail than) our level
				int vertexNum=0;
				int local_pos;
				int local_start;
				int increase = 0;
				int top_loc = 0;
				int bottom_loc = 0;
				
				if( t1 == 0 && t2 == 1){
					top_loc = GeoUtils.LW_S1;
					bottom_loc = 0;
					increase = 1;
				}else if( t1 == 1 && t2 == 3){
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_S1;
					increase = GeoUtils.level_width;
				}else if( t1 == 2 && t2 == 3){					
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_MCLWS1;
					increase = 1;
				}else if( t1 == 0 && t2 == 2){
					top_loc = GeoUtils.LW_MCLWS1;
					bottom_loc = 0;
					increase = GeoUtils.level_width;
				}
				
				if(masterBlock.childNum == t1){
					local_pos = (GeoUtils.LW_S2/2)+1;
					local_start = 0;
					buffer_normal_bottom[GeoUtils.LW_S1]= buffer_normal_bottom[GeoUtils.LW_S1].add(masterBlock.build_normals[bottom_loc]);
					buffer_normal_bottom_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[bottom_loc];
				}else{ //childNum == t2
					local_pos = GeoUtils.LW_S2;
					local_start = GeoUtils.LW_S2/2;
					buffer_normal_top[GeoUtils.LW_S1]= buffer_normal_top[GeoUtils.LW_S1].add(masterBlock.build_normals[top_loc]);
					buffer_normal_top_count[GeoUtils.LW_S1]+=masterBlock.build_normals_count[top_loc];
				}
				
				vertexNum = (masterBlock.childNum == t1) ? bottom_loc + (increase*2) : bottom_loc;
				for(int i = local_start; i < local_pos; i++){
					buffer_normal_right[i]= buffer_normal_right[i].add(masterBlock.build_normals[vertexNum]);
					buffer_normal_right_count[i]+=masterBlock.build_normals_count[vertexNum];
					vertexNum+=increase*2;
				}

				if(masterBlock.childNum == t1){
					int down = GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_southeast_corner(masterBlock,corners()[2],recursive,0,2,3);
						}
					}
				}	
				if(masterBlock.childNum == t2){
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northeast_corner(masterBlock,corners()[3],recursive,0,0,1);
						}
					}
				}
			}
			if(recursive){
				if(masterBlock.parent != parent){
					if(masterBlock.level < level){
						if(childNum == 3){
							masterBlock.computeNormals(false,reindex,this.getName()+" EW1");
						}
					}else{
						masterBlock.computeNormals(false,reindex,this.getName()+" EW2");
					}
				}else if(reindex){
					masterBlock.computeNormals(false,reindex,"sibling-EW");
				}
			}
		}
	}
	
	private void neighbor_normals_west(Chunk_Spherical c, boolean recursive, boolean reindex, int t1, int t2){
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(0,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					neighbor_normals_west(masterBlock.children[t1],recursive,reindex,t1,t2);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(0,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxY && targets[0] <= minY) || (maxY >= targets[1] && minY <= targets[0])){
					neighbor_normals_west(masterBlock.children[t2],recursive,reindex,t1,t2);
				}
			}
		}else{
			if(masterBlock.level == level){
				int top_loc = 0;
				int bottom_loc = 0;
				int increase = 0;
				int neighbor_loc = 0;
				if( t1 == 0 && t2 == 1){
					top_loc = GeoUtils.LW_S1;
					bottom_loc = 0;
					increase = 1;
				}else if( t1 == 1 && t2 == 3){
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_S1;
					increase = GeoUtils.level_width;
				}else if( t1 == 2 && t2 == 3){					
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_MCLWS1;
					increase = 1;
				}else if( t1 == 0 && t2 == 2){
					top_loc = GeoUtils.LW_MCLWS1;
					bottom_loc = 0;
					increase = GeoUtils.level_width;
				}
				neighbor_loc = bottom_loc + increase;
				buffer_normal_bottom[0]= buffer_normal_bottom[0].add(masterBlock.build_normals[bottom_loc]);
				buffer_normal_bottom_count[0]+=masterBlock.build_normals_count[bottom_loc];				
				buffer_normal_top[0]= buffer_normal_top[0].add(masterBlock.build_normals[top_loc]);
				buffer_normal_top_count[0]+=masterBlock.build_normals_count[top_loc];	
				for(int i = 0; i < GeoUtils.LW_S2; i++){
					buffer_normal_left[i]= buffer_normal_left[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_left_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc+=increase;
				}
				if(level != 0){
					int down = GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,2,3);
						}
					}
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,0,1);
						}	
					}
				}
			}else if(masterBlock.level < level){
				//we need to skip every other vertex
				int top_loc = 0; //location of top normal buffer on target
				int bottom_loc = 0; //location of bottom normal buffer on target
				int increase = 0; //amount to increase neighbor_loc per incriment
				int neighbor_loc = 0; //starting location for this sides buffers
				if(childNum==0){
					if( t1 == 0 && t2 == 1){
						top_loc = GeoUtils.level_width/2;
						bottom_loc = 0;
						increase = 1;
					}else if( t1 == 1 && t2 == 3){
						top_loc = GeoUtils.LW_S1 + (GeoUtils.level_width*(GeoUtils.level_width/2));
						bottom_loc = GeoUtils.LW_S1;
						increase = GeoUtils.level_width;
					}else if( t1 == 2 && t2 == 3){					
						top_loc = GeoUtils.LW_MCLWS1+(GeoUtils.level_width/2);
						bottom_loc = GeoUtils.LW_MCLWS1;
						increase = 1;
					}else if( t1 == 0 && t2 == 2){
						top_loc = GeoUtils.level_width * (GeoUtils.level_width/2);
						bottom_loc = 0;
						increase = GeoUtils.level_width;
					}
				}else{
					if( t1 == 0 && t2 == 1){
						top_loc = GeoUtils.LW_S1;
						bottom_loc = GeoUtils.level_width/2;
						increase = 1;
					}else if( t1 == 1 && t2 == 3){
						top_loc = GeoUtils.LW_MLW_S1;
						bottom_loc =  GeoUtils.LW_S1 + (GeoUtils.level_width*(GeoUtils.level_width/2));
						increase = GeoUtils.level_width;
					}else if( t1 == 2 && t2 == 3){					
						top_loc = GeoUtils.LW_MLW_S1;
						bottom_loc = GeoUtils.LW_MCLWS1+(GeoUtils.level_width/2);
						increase = 1;
					}else if( t1 == 0 && t2 == 2){
						top_loc = GeoUtils.LW_MCLWS1;
						bottom_loc = GeoUtils.level_width * (GeoUtils.level_width/2);
						increase = GeoUtils.level_width;
					}
				}	
				neighbor_loc = bottom_loc + increase;
				
				buffer_normal_bottom[0]= buffer_normal_bottom[0].add(masterBlock.build_normals[bottom_loc]);
				buffer_normal_bottom_count[0]+=masterBlock.build_normals_count[bottom_loc];
				buffer_normal_top[0]= buffer_normal_top[0].add(masterBlock.build_normals[top_loc]);
				buffer_normal_top_count[0]+=masterBlock.build_normals_count[top_loc];
				
				for(int i = 1; i < GeoUtils.LW_S2; i+=2){
					buffer_normal_left[i]= buffer_normal_left[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_left_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc+=increase;
				}

				if(childNum == 2){
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,0,1);
						}
					}
				}
				if(childNum == 0){
					int down = GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,2,3);
						}
					}
				}
			}else{
				//masterBlocks level is greater than (higher detail than) our level		
				int vertexNum=0;
				int local_pos;
				int local_start;
				int increase = 0;
				int top_loc = 0;
				int bottom_loc = 0;
				
				if( t1 == 0 && t2 == 1){
					top_loc = GeoUtils.LW_S1;
					bottom_loc = 0;
					increase = 1;
				}else if( t1 == 1 && t2 == 3){
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_S1;
					increase = GeoUtils.level_width;
				}else if( t1 == 2 && t2 == 3){					
					top_loc = GeoUtils.LW_MLW_S1;
					bottom_loc = GeoUtils.LW_MCLWS1;
					increase = 1;
				}else if( t1 == 0 && t2 == 2){
					top_loc = GeoUtils.LW_MCLWS1;
					bottom_loc = 0;
					increase = GeoUtils.level_width;
				}

				if(masterBlock.childNum == t1){
					local_pos = GeoUtils.level_width/2;
					local_start = 0;
					buffer_normal_bottom[0]= buffer_normal_bottom[0].add(masterBlock.build_normals[bottom_loc]);
					buffer_normal_bottom_count[0]+=masterBlock.build_normals_count[bottom_loc];
				}else{ //childNum == t2
					local_pos = GeoUtils.LW_S2;
					local_start = (GeoUtils.level_width/2)-1;
					buffer_normal_top[0]= buffer_normal_top[0].add(masterBlock.build_normals[top_loc]);
					buffer_normal_top_count[0]+=masterBlock.build_normals_count[top_loc];
				}
				
				vertexNum = (masterBlock.childNum == t1) ? bottom_loc + (increase*2) : bottom_loc;
				
				for(int i = local_start; i < local_pos; i++){
					buffer_normal_left[i]= buffer_normal_left[i].add(masterBlock.build_normals[vertexNum]);
					buffer_normal_left_count[i]+=masterBlock.build_normals_count[vertexNum];
					vertexNum+=increase*2;
				}
				
				if(masterBlock.childNum == t1){
					int down = GeoUtils.normal_neighbor_select(masterBlock.Side,0,Side)*4;
					if(masterBlock.saved_neighbors[down] != null){
						if(masterBlock.saved_neighbors[down].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[down].Side,Side);
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_southwest_corner(masterBlock,corners()[1],recursive,0,2,3);
						}
					}
				}	
				if(masterBlock.childNum == t2){
					int up = GeoUtils.normal_neighbor_select(masterBlock.Side,1,Side)*4;
					if(masterBlock.saved_neighbors[up] != null){
						if(masterBlock.saved_neighbors[up].Side != Side){
							int[] t = GeoUtils.neighbor_normal_wall(masterBlock.saved_neighbors[up].Side,Side);
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,t[0],t[1]);
						}else{
							neighbor_normals_northwest_corner(masterBlock,corners()[0],recursive,0,0,1);
						}
					}
				}
			}
			if(recursive){
				if(masterBlock.parent != parent){
					if(masterBlock.level < level){
						if(childNum == 2){
							masterBlock.computeNormals(false,reindex,this.getName()+" WW1");
						}
					}else{
						masterBlock.computeNormals(false,reindex,this.getName()+" WW2");
					}
				}else if(reindex){
					masterBlock.computeNormals(false,reindex,"sibling-WW");
				}
			}
		}
	}
	
	private void neighbor_normals_north(Chunk_Spherical masterBlock, boolean recursive, boolean reindex, int t1, int t2){
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(3,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					neighbor_normals_north(masterBlock.children[t1],recursive,reindex,t1,t2);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(3,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					neighbor_normals_north(masterBlock.children[t2],recursive,reindex,t1,t2);
				}
			}
		}else{
			if(masterBlock.level == level){
				int neighbor_loc = 0;
				int increase = 0;
				if( t1 == 0 && t2 == 1){
					increase = 1;
					neighbor_loc = 0;
				}else if( t1 == 1 && t2 == 3){
					increase = GeoUtils.level_width;
					neighbor_loc = GeoUtils.LW_S1;
				}else if( t1 == 2 && t2 == 3){					
					increase = 1;
					neighbor_loc = GeoUtils.LW_MCLWS1;
				}else if( t1 == 0 && t2 == 2){
					increase = GeoUtils.level_width;
					neighbor_loc = 0;
				}
				
				
				for(int i = 0; i < GeoUtils.level_width; i++){
					buffer_normal_top[i]= buffer_normal_top[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_top_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc += increase;
				}
			}else if(masterBlock.level < level){
				int neighbor_loc = 0;
				int increase = 0;
				if(childNum==2){
					if( t1 == 0 && t2 == 1){
						increase = 1;
						neighbor_loc = 0;
					}else if( t1 == 1 && t2 == 3){
						increase = GeoUtils.level_width;
						neighbor_loc = GeoUtils.LW_S1;
					}else if( t1 == 2 && t2 == 3){					
						increase = 1;
						neighbor_loc = GeoUtils.LW_MCLWS1;
					}else if( t1 == 0 && t2 == 2){
						increase = GeoUtils.level_width;
						neighbor_loc = 0;
					}
				}else{//childNum == 3
					if( t1 == 0 && t2 == 1){
						increase = 1;
						neighbor_loc = GeoUtils.level_width/2;
					}else if( t1 == 1 && t2 == 3){
						increase = GeoUtils.level_width;
						neighbor_loc = ((GeoUtils.level_width/2) * GeoUtils.level_width) + GeoUtils.LW_S1;
					}else if( t1 == 2 && t2 == 3){					
						increase = 1;
						neighbor_loc = GeoUtils.LW_MCLWS1 + (GeoUtils.level_width/2);
					}else if( t1 == 0 && t2 == 2){
						increase = GeoUtils.level_width;
						neighbor_loc = (GeoUtils.level_width/2) * GeoUtils.level_width;
					}
				}				
				for(int i = 0; i < GeoUtils.level_width; i+=2){
					buffer_normal_top[i]= buffer_normal_top[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_top_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc+=increase;
				}
			}else{
				//masterBlocks level is greater than (higher detail than) our level
				int vertexNum = (masterBlock.childNum == t1)? 0: GeoUtils.level_width/2;
				int neighbor_loc = 0;
				int increase = 0;
				int count = 0;
				if( t1 == 0 && t2 == 1){
					increase = 1;
					neighbor_loc = 0;
				}else if( t1 == 1 && t2 == 3){
					increase = GeoUtils.level_width;
					neighbor_loc = GeoUtils.LW_S1;
				}else if( t1 == 2 && t2 == 3){					
					increase = 1;
					neighbor_loc = GeoUtils.LW_MCLWS1;
				}else if( t1 == 0 && t2 == 2){
					increase = GeoUtils.level_width;
					neighbor_loc = 0;
				}
				for(int i = neighbor_loc; count < GeoUtils.level_width; i+=(increase*2)){
					buffer_normal_top[vertexNum]= buffer_normal_top[vertexNum].add(masterBlock.build_normals[i]);
					buffer_normal_top_count[vertexNum]+=masterBlock.build_normals_count[i];
					vertexNum++;
					count+=2;
				}
			}
			if(recursive){
				if(masterBlock.parent != parent){
					if(masterBlock.level < level){
						if(childNum == 3){
							masterBlock.computeNormals(false,reindex,this.getName()+" NW1");
						}
					}else{
                                                //remove -- for search purpose only ,this.getName());
						masterBlock.computeNormals(false,reindex,this.getName()+" NW2");
					}
				}else if(reindex){
					masterBlock.computeNormals(false,reindex,"sibling-NW");
				}
			}
		}
	}
	
	private void neighbor_normals_south(Chunk_Spherical c, boolean recursive, boolean reindex, int t1, int t2){
		Chunk_Spherical masterBlock = c;
		if(masterBlock.has_child){
			if(masterBlock.children[t1]!=null){
				double[] targets = GeoUtils.gather_target(1,masterBlock.children[t1],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					neighbor_normals_south(masterBlock.children[t1],recursive,reindex,t1,t2);
				}
			}
			if(masterBlock.children[t2]!=null){
				double[] targets = GeoUtils.gather_target(1,masterBlock.children[t2],Side,Side);
				if( (targets[1] >= maxX && targets[0] <= minX) || (maxX >= targets[1] && minX <= targets[0])){
					neighbor_normals_south(masterBlock.children[t2],recursive,reindex,t1,t2);
				}
			}
		}else{
			if(masterBlock.level == level){                                      
				int neighbor_loc = 0;
				int increase = 0;
				if( t1 == 0 && t2 == 1){
					increase = 1;
					neighbor_loc = 0;
				}else if( t1 == 1 && t2 == 3){
					increase = GeoUtils.level_width;
					neighbor_loc = GeoUtils.LW_S1;
				}else if( t1 == 2 && t2 == 3){					
					increase = 1;
					neighbor_loc = GeoUtils.LW_MCLWS1;
				}else if( t1 == 0 && t2 == 2){
					increase = GeoUtils.level_width;
					neighbor_loc = 0;
				}
				
				for(int i = 0; i < GeoUtils.level_width; i++){
					buffer_normal_bottom[i]= buffer_normal_bottom[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_bottom_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc += increase;
				}
			}else if(masterBlock.level < level){
				int neighbor_loc = 0;
				int increase = 0;
				
				if(childNum==0){
					//vertexNum = (int) (level_width * (level_width -1));
					if( t1 == 0 && t2 == 1){
						increase = 1;
						neighbor_loc = 0;
					}else if( t1 == 1 && t2 == 3){
						increase = GeoUtils.level_width;
						neighbor_loc = GeoUtils.LW_S1;
					}else if( t1 == 2 && t2 == 3){					
						increase = 1;
						neighbor_loc = GeoUtils.LW_MCLWS1;
					}else if( t1 == 0 && t2 == 2){
						increase = GeoUtils.level_width;
						neighbor_loc = 0;
					}
				}else{//childNum == 1
					if( t1 == 0 && t2 == 1){
						increase = 1;
						neighbor_loc = GeoUtils.level_width/2;
					}else if( t1 == 1 && t2 == 3){
						increase = GeoUtils.level_width;
						neighbor_loc = ((GeoUtils.level_width/2) * GeoUtils.level_width) + GeoUtils.LW_S1;
					}else if( t1 == 2 && t2 == 3){					
						increase = 1;
						neighbor_loc = GeoUtils.LW_MCLWS1+ (GeoUtils.level_width/2);
					}else if( t1 == 0 && t2 == 2){
						increase = GeoUtils.level_width;
						neighbor_loc = (GeoUtils.level_width/2)*GeoUtils.level_width;
					}
				}
				
				for(int i = 0; i < GeoUtils.level_width; i+=2){
					buffer_normal_bottom[i]= buffer_normal_bottom[i].add(masterBlock.build_normals[neighbor_loc]);
					buffer_normal_bottom_count[i]+=masterBlock.build_normals_count[neighbor_loc];
					neighbor_loc+=increase;
				}
				
			}else{
				//masterBlocks level is greater than (higher detail than) our level
				int vertexNum = (masterBlock.childNum == t1)? 0 : GeoUtils.level_width/2;
				int count = 0;
				int neighbor_loc = 0;
				int increase = 0;
				if( t1 == 0 && t2 == 1){
					increase = 1;
					neighbor_loc = 0;
				}else if( t1 == 1 && t2 == 3){
					increase = GeoUtils.level_width;
					neighbor_loc = GeoUtils.LW_S1;
				}else if( t1 == 2 && t2 == 3){					
					increase = 1;
					neighbor_loc = GeoUtils.LW_MCLWS1;
				}else if( t1 == 0 && t2 == 2){
					increase = GeoUtils.level_width;
					neighbor_loc = 0;
				}
		
				for(int i = neighbor_loc; count < GeoUtils.level_width; i+=(increase*2)){
					buffer_normal_bottom[vertexNum]= buffer_normal_bottom[vertexNum].add(masterBlock.build_normals[i]);
					buffer_normal_bottom_count[vertexNum]+=masterBlock.build_normals_count[i];
					vertexNum++;
					count +=2;
				}
			}
			if(recursive){
				if(masterBlock.parent != parent){
					if(masterBlock.level < level){
						if(childNum == 1){
							masterBlock.computeNormals(false,reindex,this.getName()+" SW1");
						}
					}else{
						masterBlock.computeNormals(false,reindex,this.getName()+" SW2");
					}
				}else if(reindex){
					masterBlock.computeNormals(false,reindex,"sibling-SW");
				}
			}
		}
	}
	
	
	private void catalogue_normal(FloatBuffer vb,int v1, int v2, int v3){
		int v1loc = v1 * 3;
		int v2loc = v2 * 3;
		int v3loc = v3 * 3;
                vb.position(v1loc);
		Vector3f vector1 = new Vector3f(vb.get(),vb.get(),vb.get());
                vb.position(v2loc);
		Vector3f vector2 = new Vector3f(vb.get(),vb.get(),vb.get());
                vb.position(v3loc);
		Vector3f vector3 = new Vector3f(vb.get(),vb.get(),vb.get());

		vector1.subtractLocal(vector3);
		Vector3f normal = vector1.cross(vector3.subtract(vector2)).normalizeLocal();

		build_normals[v1] = normal.add(build_normals[v1]);
		build_normals_count[v1]++;
		
		build_normals[v2] = normal.add(build_normals[v2]);
		build_normals_count[v2]++;
		
		build_normals[v3] = normal.add(build_normals[v3]);
		build_normals_count[v3]++;
	}

        

        public ArrayList<Chunk_Spherical> combineList(ArrayList<Chunk_Spherical> a, ArrayList<Chunk_Spherical> b){
            int j = b.size();
            Chunk_Spherical bi;
            for(int i = 0; i < j; i++){
                bi = b.get(i);
                a.remove(bi);
                a.add(bi);                
            }
            return a;
        }

        public ArrayList<Chunk_Spherical> combineList(ArrayList<Chunk_Spherical> a, Chunk_Spherical b[]){
            int j = b.length;
            for(int i = 0; i < j; i++){
                if(b!=null){
                    a.remove(b[i]);
                    a.add(b[i]);
                }
            }
            return a;
        }

        public ArrayList<Chunk_Spherical> combineList(ArrayList<Chunk_Spherical> a, Chunk_Spherical b){
            if(b!=null){
                a.remove(b);
                a.add(b);
            }
            return a;
        }

        public Chunk_Spherical[] corners(){
            Chunk_Spherical[] a = new Chunk_Spherical[4];
            int max = 0;
            if(saved_neighbors[15] != null){
                max = 15;
            }else if(saved_neighbors[14] != null){
                max = 14;
            }else if(saved_neighbors[13] != null){
                max = 13;
            }else{
                max = 12;
            }
            a[0] = findSame(saved_neighbors[0],saved_neighbors[max],children[2]);
            if(saved_neighbors[3] != null){
                max = 3;
            }else if(saved_neighbors[2] != null){
                max = 2;
            }else if(saved_neighbors[1] != null){
                max = 1;
            }else{
                max = 0;
            }
            a[1] = findSame(saved_neighbors[4],saved_neighbors[max],children[0]);
            if(saved_neighbors[7] != null){
                max = 7;
            }else if(saved_neighbors[6] != null){
                max = 6;
            }else if(saved_neighbors[5] != null){
                max = 5;
            }else{
                max = 4;
            }
            a[2] = findSame(saved_neighbors[8], saved_neighbors[max],children[1]);
            if(saved_neighbors[11] != null){
                max = 11;
            }else if(saved_neighbors[10] != null){
                max = 10;
            }else if(saved_neighbors[9] != null){
                max = 9;
            }else{
                max = 8;
            }
            a[3] = findSame(saved_neighbors[12], saved_neighbors[max],children[3]);
            return a;
        }

        public Chunk_Spherical findSame(Chunk_Spherical a, Chunk_Spherical b, Chunk_Spherical exclude){
            Chunk_Spherical c = null;
            if(a.level != 0 && b.level != 0){
                for(int i = 0; i < 16; i++){
                    for(int j = 0; j < 16; j++){
                        if(a.saved_neighbors[i] != null){
                            if(b.saved_neighbors[j] != null){
                                if(a.saved_neighbors[i] == b.saved_neighbors[j] && b.saved_neighbors[j] != this && b.saved_neighbors[j] != exclude){
                                    if((minX == b.saved_neighbors[j].maxX && minY == b.saved_neighbors[j].maxY)||(minY == b.saved_neighbors[j].maxY && maxX == b.saved_neighbors[j].minX)||(maxX == b.saved_neighbors[j].minX && maxY == b.saved_neighbors[j].minY)||(maxY == b.saved_neighbors[j].minY && minX == b.saved_neighbors[j].maxX)){
                                        c = b.saved_neighbors[j];
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return c;
        }        

        public void dumpNeighbor(){
            for(int i = 0; i < 16; i++){
                System.out.println(i+" "+name+" "+saved_neighbors[i]);
            }
        }
}
