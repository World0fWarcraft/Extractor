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
package eu.jangos.extractor.file.m2;

import eu.jangos.extractor.file.common.CAaBox;

/**
 *
 * @author Warkdev
 */
public class M2Bounds {
    private CAaBox extend;
    private float radius;

    public M2Bounds() {
    }

    public CAaBox getExtend() {
        return extend;
    }

    public void setExtend(CAaBox extend) {
        this.extend = extend;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }        
}
