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
package eu.jangos.extractor.file.wmo.group;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBox;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class MOGP {        
    private int groupName;
    private int descriptiveGroupName;
    private int flags;
    private CAaBox boundingBox = new CAaBox();
    private short portalStart;
    private short portalCount;
    private short transBatchCount;
    private short intBatchCount;
    private short extBatchCount;
    private short padding;
    private byte[] fogsIds = new byte[4];
    private int groupLiquid;
    private int wmoAreaTableRecId;
    private int flagEnum;
    private int flag2;    
    
    public void read(ByteBuffer data) {
        this.groupName = data.getInt();
        this.descriptiveGroupName = data.getInt();
        this.flags = data.getInt();
        this.boundingBox.setMin(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        this.boundingBox.setMax(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        this.portalStart = data.getShort();
        this.portalCount = data.getShort();
        this.transBatchCount = data.getShort();
        this.intBatchCount = data.getShort();
        this.extBatchCount = data.getShort();
        this.padding = data.getShort();
        data.get(fogsIds);
        this.groupLiquid = data.getInt();
        this.wmoAreaTableRecId = data.getInt();
        this.flagEnum = data.getInt();
        this.flag2 = data.getInt();          
    }
    
    public int getGroupName() {
        return groupName;
    }

    public void setGroupName(int groupName) {
        this.groupName = groupName;
    }

    public int getDescriptiveGroupName() {
        return descriptiveGroupName;
    }

    public void setDescriptiveGroupName(int descriptiveGroupName) {
        this.descriptiveGroupName = descriptiveGroupName;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public CAaBox getBoundingBox() {
        return boundingBox;
    }

    public short getPortalStart() {
        return portalStart;
    }

    public void setPortalStart(short portalStart) {
        this.portalStart = portalStart;
    }

    public short getPortalCount() {
        return portalCount;
    }

    public void setPortalCount(short portalCount) {
        this.portalCount = portalCount;
    }
        
    public void setBoundingBox(CAaBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public short getTransBatchCount() {
        return transBatchCount;
    }

    public void setTransBatchCount(short transBatchCount) {
        this.transBatchCount = transBatchCount;
    }

    public short getIntBatchCount() {
        return intBatchCount;
    }

    public void setIntBatchCount(short intBatchCount) {
        this.intBatchCount = intBatchCount;
    }

    public short getExtBatchCount() {
        return extBatchCount;
    }

    public void setExtBatchCount(short extBatchCount) {
        this.extBatchCount = extBatchCount;
    }

    public short getPadding() {
        return padding;
    }

    public void setPadding(short padding) {
        this.padding = padding;
    }

    public byte[] getFogsIds() {
        return fogsIds;
    }

    public void setFogsIds(byte[] fogsIds) {
        this.fogsIds = fogsIds;
    }

    public int getGroupLiquid() {
        return groupLiquid;
    }

    public void setGroupLiquid(int groupLiquid) {
        this.groupLiquid = groupLiquid;
    }

    public int getWmoAreaTableRecId() {
        return wmoAreaTableRecId;
    }

    public void setWmoAreaTableRecId(int wmoAreaTableRecId) {
        this.wmoAreaTableRecId = wmoAreaTableRecId;
    }

    public int getFlagEnum() {
        return flagEnum;
    }

    public void setFlagEnum(int flagEnum) {
        this.flagEnum = flagEnum;
    }

    public int getFlag2() {
        return flag2;
    }

    public void setFlag2(int flag2) {
        this.flag2 = flag2;
    }    
}