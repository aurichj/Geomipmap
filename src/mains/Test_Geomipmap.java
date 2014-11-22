package mains;

import Procedural_Noise.Functions.Noise;
import Procedural_Planet.GeoMipMap.GeoUtils;
import Procedural_Planet.GeoMipMap.Geomipmap_Cube;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * This application runs a visualization of the spherical geomipmapping.
 * @author Joshua Aurich
 */
public class Test_Geomipmap extends SimpleApplication{
	Geomipmap_Cube q;
	int level = 0;
	double size = 1000;
	float y_scale = 20;
	boolean advanced = false;
	int advanced_counter =0;
	Noise n = new Noise(5675,2048);
        Material mat1;
        Boolean wire = true;
        Boolean pause = false;
        double phi =0;
        double theta=0;
        DirectionalLight dl;
	
	public void simpleUpdate(float tpf){
            if(!pause){
                 q.update(cam.getLocation(),cam);
            }
	}
	
    public static void main(String[] args) {
        GeoUtils.prepareIndex();
        Test_Geomipmap app = new Test_Geomipmap();
        app.start();
    }

	@Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.White);
	Node pnode = new Node();
	rootNode.attachChild(pnode);       
        mat1 = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");    
        mat1.setColor("Color", ColorRGBA.Blue);
        mat1.getAdditionalRenderState().setWireframe(true);
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f((float)(Math.sin(phi)*Math.cos(theta)), (float)(Math.sin(phi)*Math.sin(theta)),(float)(Math.cos(phi))).normalizeLocal());
        dl.setColor(ColorRGBA.Green);
        rootNode.addLight(dl);
        q = new Geomipmap_Cube(/**20**/6360f,/**1f**/100f,pnode,mat1);
        pnode.setModelBound(new BoundingBox());
        pnode.updateModelBound();
        pnode.setLocalTranslation(0,0,-60);
        rootNode.updateGeometricState();
        flyCam.setMoveSpeed(/**35f**/2500f);
        cam.setFrustumFar(10000000000f);
        cam.setLocation(new Vector3f(0,0,/**350**/11500));
        cam.update();
        inputManager.addMapping("Wire",  new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, new String[]{"Wire"});
        inputManager.addMapping("Pause",  new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(actionListener, new String[]{"Pause"});
	}

    private ActionListener actionListener = new ActionListener() {

    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Wire") && !keyPressed) {
          if(wire == false){
              mat1.getAdditionalRenderState().setWireframe(true);
              wire = true;
              System.out.println("wire true");
          }else{
              mat1.getAdditionalRenderState().setWireframe(false);
              wire = false;
              System.out.println("wire false");
          }
      }
      if (name.equals("Pause") && !keyPressed) {
          pause = !pause;
          System.out.println("pause "+pause);
      }     
    }
  };

}