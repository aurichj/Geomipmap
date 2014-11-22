package Procedural_Planet.GeoMipMap;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.ArrayList;


public class Geomipmap_Cube extends Node{
	public Chunk_Spherical top;
	public Chunk_Spherical bottom;
	public Chunk_Spherical left;
	public Chunk_Spherical right;
	public Chunk_Spherical front;
	public Chunk_Spherical back;
        public Optimizer opt;
        public ArrayList<Chunk_Spherical> reindexList = new ArrayList<Chunk_Spherical>();
        public ArrayList<Chunk_Spherical> divideList = new ArrayList<Chunk_Spherical>();

	public Geomipmap_Cube(float radius, float noise_scale,Node rootNode, Material mat){
		rootNode.attachChild(this);
                setName("Geomipmap_Cube");
                opt = new Optimizer();
		top = new Chunk_Spherical(noise_scale,radius,this,0,mat);
		bottom = new Chunk_Spherical(noise_scale,radius,this,5,mat);
		left = new Chunk_Spherical(noise_scale,radius,this,2,mat);
		right = new Chunk_Spherical(noise_scale,radius,this,3,mat);
		front = new Chunk_Spherical(noise_scale,radius,this,4,mat);
		back = new Chunk_Spherical(noise_scale,radius,this,1,mat);
		

                top.saved_neighbors[0] = left;
		top.saved_neighbors[4] = back;
		top.saved_neighbors[8] = right;
		top.saved_neighbors[12] = front;


                bottom.saved_neighbors[0] = left;
		bottom.saved_neighbors[4] = back;
		bottom.saved_neighbors[8] = right;
		bottom.saved_neighbors[12] = front;


                left.saved_neighbors[0] = back;
		left.saved_neighbors[4] = bottom;
		left.saved_neighbors[8] = front;
		left.saved_neighbors[12] = top;


                right.saved_neighbors[0] = back;
		right.saved_neighbors[4] = bottom;
		right.saved_neighbors[8] = front;
		right.saved_neighbors[12] = top;


                front.saved_neighbors[0] = left;
		front.saved_neighbors[4] = bottom;
		front.saved_neighbors[8] = right;
		front.saved_neighbors[12] = top;


                back.saved_neighbors[0] = left;
		back.saved_neighbors[4] = bottom;
		back.saved_neighbors[8] = right;
		back.saved_neighbors[12] = top;

		front.computeNormals(false,false,"start");
		back.computeNormals(false,false,"start");
		left.computeNormals(false,false,"start");
		right.computeNormals(false,false,"start");
		top.computeNormals(false,false,"start");
		bottom.computeNormals(false,false,"start");

                long start = System.currentTimeMillis();            

               front.divide("declared",null,true);

                opt.total(((System.currentTimeMillis() - start)));
                opt.dumpInfo();
	}
	
	public void update(Vector3f camloc, Camera cam){
		top.update(camloc, cam);
                bottom.update(camloc, cam);
		left.update(camloc, cam);
		right.update(camloc, cam);
		front.update(camloc, cam);
		back.update(camloc, cam);
	}
}
