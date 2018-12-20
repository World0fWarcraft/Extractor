/* 
 * Copyright 2018 Warkdev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.jangos.extractorfx.obj;

import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2SkinProfile;
import eu.jangos.extractor.file.m2.M2SkinSection;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Warkdev
 */
public class M2Converter extends ModelConverter {

    private static final Logger logger = LoggerFactory.getLogger(M2Converter.class);
    
    private M2 reader;

    public M2Converter(M2 reader) {
        super();
        this.reader = reader;
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @param view There are 4 views in the M2 file of the same model. Use
     * number 1 to 4 to extract the view you want.
     * @throws ConverterException
     */
    public void convert(int view, int maxHeight) throws ConverterException {
        if (this.reader == null) {
            throw new ConverterException("M2FileReader is null");
        }

        if (view < 1 || view > 4) {
            throw new ConverterException("View number must be between 1 and 4");
        }
        view--;

        clearMesh();

        try {
            
            // Converting M2Vertex to Vertex.
            float minX = 0;
            float maxX = 0;
            float minY = 0;
            float maxY = 0;
            for (M2Vertex v : reader.getVertices()) {
                if (v.getPosition().z <= maxHeight && v.getPosition().z > maxHeight / 3 * 2) {
                    if (v.getPosition().x > maxX) {
                        maxX = v.getPosition().x;
                    }
                    if (v.getPosition().x < minX) {
                        minX = v.getPosition().x;
                    }
                    if (v.getPosition().y > maxY) {
                        maxY = v.getPosition().y;
                    }
                    if (v.getPosition().y < minY) {
                        minY = v.getPosition().y;
                    }
                }                                
            }

            for (M2Vertex v : reader.getVertices()) {
                if (v.getPosition().z <= maxHeight) {
                    mesh.getPoints().addAll(v.getPosition().x, v.getPosition().y, v.getPosition().z);
                    mesh.getNormals().addAll(v.getNormal().x, v.getNormal().y, v.getNormal().z);
                    mesh.getTexCoords().addAll(v.getTexCoords()[0].x, v.getTexCoords()[0].y);
                } else {                    
                    mesh.getPoints().addAll(v.getPosition().x < minX ? minX : v.getPosition().x > maxX ? maxX : v.getPosition().x,
                            v.getPosition().y < minY ? minY : v.getPosition().y > maxY ? maxY : v.getPosition().y,
                            maxHeight);
                    mesh.getNormals().addAll(1, 1, 1);
                    mesh.getTexCoords().addAll(v.getTexCoords()[0].x, v.getTexCoords()[0].y);
                }                
            }

            List<M2SkinProfile> listSkins = reader.getSkins();
            for (int i = 0; i < listSkins.get(view).getSubMeshes().size(); i++) {
                List<M2SkinSection> listSkinSections = listSkins.get(view).getSubMeshes();
                List<Short> listIndices = listSkins.get(view).getIndices();

                int face = listSkins.get(view).getSubMeshes().get(i).getIndexStart();
                                
                while (face < listSkinSections.get(i).getIndexStart() + listSkinSections.get(i).getIndexCount()) {
                    this.mesh.getFaces().addAll(listIndices.get(face + 2), listIndices.get(face + 2), listIndices.get(face + 2),
                            listIndices.get(face + 1), listIndices.get(face + 1), listIndices.get(face + 1),
                            listIndices.get(face), listIndices.get(face), listIndices.get(face));
                    face += 3;
                }
            }
        } catch (M2Exception ex) {
            logger.error("Error while reading the M2 content "+ex.getMessage());
            throw new ConverterException("Error while reading the M2 content " + ex.getMessage());
        }
    }

    public M2 getReader() {
        return reader;
    }

    public void setReader(M2 reader) {
        this.reader = reader;
    }

}