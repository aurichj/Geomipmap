package Procedural_Planet.GeoMipMap;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author Josh
 */
public class Optimizer {
    double normal = 0;
    float normal_count = 0;

    double wall = 0;
    float wall_count = 0;

    double index = 0;
    float index_count = 0;

    double divide = 0;
    float divide_count = 0;

    double unite = 0;
    float unite_count = 0;

    double vertex = 0;
    float vertex_count = 0;

    double total_time = 0;
    int count = 0;

    String debug_file_name = "debug.txt";
    BufferedWriter out;
    int lineNumber = 0;

    public Optimizer(){
        try{
            /**BufferedWriter**/ out = new BufferedWriter(new FileWriter(debug_file_name,true));
            out.write("Start Debug");
            out.newLine();
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void write(String text){
        try{
            if(lineNumber <=5000){
                out = new BufferedWriter(new FileWriter(debug_file_name,true));
                out.write(lineNumber+". "+text);
                out.newLine();
                out.close();
                lineNumber++;
            }
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void addNormal(double value){
        normal += value;
        normal_count++;
    }

    public void addWall(double value){
        wall += value;
        wall_count++;
    }

    public void addIndex(double value){
        index += value;
        index_count++;
    }

    public void addDivide(double value){
        divide += value;
        divide_count++;
    }

    public void addUnite(double value){
        unite += value;
        unite_count++;
    }

    public void addVertex(double value){
        vertex += value;
        vertex_count++;
    }

    public void total(double total){
        total_time = total;
    }

    public void countObject(int value){
        count += value;
    }

    public void dumpInfo(){
        System.out.println("Begin Value Dump");
        System.out.println("Obj. Count | Vertex Count | Vertex Total | Vertex Avg. | Index Count | Index Total | Index Avg | Normal Count | Normal Total | Normal Avg. | Wall Count | Wall Total | Wall Avg. | Divide Count | Divide Total | Divide Avg. | Unite Count | Unite Total | Unite Avg. | Total Time");
        System.out.println(count+"|"+vertex_count+"|"+vertex+"|"+vertex/vertex_count+"|"+index_count+"|"+index+"|"+index/index_count+"|"+normal_count+"|"+normal+"|"+normal/normal_count+"|"+wall_count+"|"+wall+"|"+wall/wall_count+"|"+divide_count+"|"+divide+"|"+divide/divide_count+"|"+unite_count+"|"+unite+"|"+unite/unite_count+"|"+total_time);
        System.out.println("End Value Dump");
    }

    public void clear(){
       normal = 0;
       normal_count = 0;
       wall = 0;
       wall_count = 0;
       index = 0;
       index_count = 0;
       divide = 0;
       divide_count = 0;
       unite = 0;
       unite_count = 0;
       vertex = 0;
       vertex_count = 0;
       total_time = 0;
       count = 0;
    }
}
