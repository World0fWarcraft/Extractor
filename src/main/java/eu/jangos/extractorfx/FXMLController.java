package eu.jangos.extractorfx;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.ADTFileReader;
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import eu.mangos.shared.flags.FlagUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableFloatArray;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

public class FXMLController implements Initializable {

    @FXML
    private Group meshGroup;

    private MeshView terrain = new MeshView();
    private PerspectiveCamera camera;
    private double x, y;

    private static final int VIEWPORT_SIZE = 1920;
    private static final double MODEL_SCALE_FACTOR = 15;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE * 21;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String PATH = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
        String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";        
        String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
        String mdx = "world\\azeroth\\elwynn\\passivedoodads\\detail\\elwynnvineyard\\elwynnvineyard01.m2";
        ADTFileReader adt = new ADTFileReader();
        ADT2OBJConverter adtConverter = new ADT2OBJConverter(adt);
        M2FileReader m2 = new M2FileReader();
        M22OBJConverter m2Converter = new M22OBJConverter(m2);
        File mpq = new File(PATH);
        File modelFile = new File(MODEL);

        
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            adt.init(editor.extractFileAsBytes(map));
            adtConverter.convert();
                     
            m2.init(editor.extractFileAsBytes(mdx));
            m2Converter.convert(1);
            
            TriangleMesh mesh = new TriangleMesh();
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
            terrain.setMesh(mesh);

            for (Vertex v : m2Converter.getVerticeList()) {
                mesh.getPoints().addAll(v.getPosition().x, (-1) * v.getPosition().y, v.getPosition().z * (-1));
                mesh.getTexCoords().addAll(v.getTextCoord().x, (-1) * v.getTextCoord().y);
                mesh.getNormals().addAll(v.getNormal().x, v.getNormal().y, v.getNormal().z);                
            }

            for (RenderBatch batch : m2Converter.getRenderBatches()) {
                int i = batch.getFirstFace();
                if (adtConverter.getMaterials().containsKey(batch.getMaterialID())) {                    
                    /**writer.write("usemtl " + adtConverter.getMaterials().get(batch.getMaterialID()) + "\n");
                    writer.write("s 1" + "\n");*/                    
                }

                // There's one iteration too much.
                while (i < batch.getFirstFace() + batch.getNumFaces()) {                          
                    mesh.getFaceSmoothingGroups().addAll(1);
                    mesh.getFaces().addAll(
                            (m2Converter.getIndiceList().get(i + 2)), (m2Converter.getIndiceList().get(i + 2)), (m2Converter.getIndiceList().get(i + 2)),
                            (m2Converter.getIndiceList().get(i + 1)),  (m2Converter.getIndiceList().get(i + 1)), (m2Converter.getIndiceList().get(i + 1)),
                            (m2Converter.getIndiceList().get(i)), (m2Converter.getIndiceList().get(i)), (m2Converter.getIndiceList().get(i)));     
                    i+= 3;
                }
            }            

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (M2Exception ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        initCamera();
        Platform.runLater(() -> showFigure());
    }

    @FXML
    private void onMouseDragged(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Detected: "+ event.getX());      
        terrain.startFullDrag();
        this.x = event.getX();
        this.y = event.getY();
    }
    
    @FXML
    private void onDragDone(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Done: "+event.getX());        
    }
    
    @FXML
    private void onDragDropped(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Dropped: "+event.getX());
    }
    
    @FXML
    private void onDragEntered(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Entered: "+event.getX());
    }
    
    @FXML
    private void onDragExited(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Exited: "+event.getX());
    }
    
    @FXML
    private void onDragOver(Event e) {
        MouseEvent event = (MouseEvent) e;
        // Moving camera.        
        this.terrain.setRotationAxis(Rotate.Z_AXIS);
        this.terrain.setRotate(event.getY());        
        this.terrain.setRotationAxis(Rotate.X_AXIS);
        
        this.terrain.setRotate(event.getX());
    }
    
    private void initCamera() {
        this.camera = new PerspectiveCamera(false);
        this.camera.setNearClip(0.1);
        this.camera.setFarClip(10000.0);
        this.camera.setTranslateZ(-1000);
        
        final Rotate cameraRotateX = new Rotate(0, new Point3D(1, 0, 0));
        final Rotate cameraRotateY = new Rotate(0, new Point3D(0, 1, 0));
        final Translate cameraTranslate = new Translate(0, 0, -1000);
        camera.getTransforms().addAll(cameraRotateX, cameraRotateY, cameraTranslate);
    }

    private void showFigure() {
        // Add MeshView to Group
        Group meshInGroup = buildScene();
        // Create SubScene
        SubScene subScene = createScene3D(meshInGroup);
        // Add subScene to meshGroup
        this.meshGroup.getChildren().add(subScene);

        //RotateTransition rotate = rotate3dGroup(meshInGroup);        
    }

    private Group buildScene() {
        Group group = new Group();        
        //terrain.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
        //terrain.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
        //terrain.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
        terrain.setScaleX(MODEL_SCALE_FACTOR);
        terrain.setScaleY(MODEL_SCALE_FACTOR);
        terrain.setScaleZ(MODEL_SCALE_FACTOR);
        
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(VIEWPORT_SIZE*5);
        pointLight.setTranslateY(VIEWPORT_SIZE*5);
        pointLight.setTranslateX(VIEWPORT_SIZE*5);
        //pointLight.setTrasetTranslateZ(VIEWPORT_SIZE);
        //pointLight.setTranslateY(VIEWPORT_SIZE);

        group.getChildren().addAll(terrain, pointLight);
        return group;
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene3d.setFill(Color.WHITE);
        scene3d.setCamera(this.camera);
        scene3d.setPickOnBounds(true);
        return scene3d;
    }

    private RotateTransition rotate3dGroup(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

    private void validateFaces(TriangleMesh mesh) {
        ObservableFaceArray faces = mesh.getFaces();
        ObservableFloatArray points = mesh.getPoints();
        ObservableFloatArray normals = mesh.getNormals();
        ObservableFloatArray texCoords = mesh.getTexCoords();

        int nVerts = points.size() / mesh.getPointElementSize();
        int nNVerts = normals.size() / mesh.getNormalElementSize();
        int nTVerts = texCoords.size() / mesh.getTexCoordElementSize();
        for (int i = 0; i < faces.size(); i += 3) {
            if ((faces.get(i) >= nVerts || faces.get(i) < 0)) {
                System.out.println("error with nVerts "+nVerts+", "+faces.get(i));
            }
            if (faces.get(i + 1) >= nNVerts || faces.get(i + 1) < 0) {
                System.out.println("error with nNVerts "+nNVerts+", "+faces.get(i+1));
            }
            if (faces.get(i + 2) >= nTVerts || faces.get(i + 2) < 0) {
                System.out.println("error with nTVerts "+ nTVerts+", "+faces.get(i+2));
            }            
        }
    }
}