/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import java.awt.Color;
import eu.jangos.extractor.file.adt.chunk.MCIN;
import eu.jangos.extractor.file.adt.chunk.MCLQ;
import static eu.jangos.extractor.file.adt.chunk.MCLQ.LIQUID_FLAG_LENGTH;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Warkdev
 */
public class ADT extends FileReader {

    private static final String HEADER_VERSION = "MVER";
    private static final String HEADER_MHDR = "MHDR";
    private static final String HEADER_MCIN = "MCIN";
    private static final String HEADER_MTEX = "MTEX";
    private static final String HEADER_MMDX = "MMDX";
    private static final String HEADER_MMID = "MMID";
    private static final String HEADER_MWMO = "MWMO";
    private static final String HEADER_MWID = "MWID";
    private static final String HEADER_MDDF = "MDDF";
    private static final String HEADER_MODF = "MODF";
    private static final String HEADER_MCNK = "MCNK";    

    // Size as from which the offset calculation is made.
    private static final int GLOBAL_OFFSET = 0x14;

    public static final float TILE_SIZE = 533.33333f;
    public static final float CHUNK_SIZE = TILE_SIZE / 16.0f;
    public static final float UNIT_SIZE = CHUNK_SIZE / 8.0f;
    public static final float ZERO_POINT = 32.0f * TILE_SIZE;

    private static final int SIZE_LIQUID_MAP = 128;        

    private int version;
    private int mfboEnum;
    private int headerFlags;
    private int offsetMCIN;
    private int offsetMTEX;
    private int offsetMMDX;
    private int offsetMMID;
    private int offsetMWMO;
    private int offsetMWID;
    private int offsetMDDF;
    private int offsetMODF;
    private int offsetMFBO;
    private int offsetMH2O;
    private int offsetMTFX;

    @Override
    public void init(byte[] data, String filename) throws IOException, FileReaderException {
        init = false;        
        super.data = ByteBuffer.wrap(data);
        super.data.order(ByteOrder.LITTLE_ENDIAN);    
        super.filename = filename;
        
        // This is all what we need to read our file. Initialize the offset and check the version.
        readVersion(super.data);
        readHeader(super.data);
        init = true;
    }

    /**
     * Reading the ADT version. The ADT version is expected to be located at the
     * beginning of the ADT File under this form: 4 character indicating the
     * MVER chunk in reverse order. 4 bytes indicating the length of the MVER
     * chunk. 4 bytes indicating the version of the ADT File (18 in 1.12.x).
     * e.g.: REVM 04 00 00 00 12 00 00 00
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readVersion(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_VERSION);

        // We skip the size as we know it's 4.
        in.getInt();
        this.version = in.getInt();
    }

    /**
     * Reading the MHDR chunk of the ADT file. The MHDR chunk is expected to be
     * right after the MVER chunk. It contains offset towards the various chunks
     * hold in this ADT. e.g. : RDHM
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readHeader(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_MHDR);

        this.mfboEnum = in.getInt();
        this.headerFlags = in.getInt();
        this.offsetMCIN = in.getInt();
        this.offsetMTEX = in.getInt();
        this.offsetMMDX = in.getInt();
        this.offsetMMID = in.getInt();
        this.offsetMWMO = in.getInt();
        this.offsetMWID = in.getInt();
        this.offsetMDDF = in.getInt();
        this.offsetMODF = in.getInt();
        this.offsetMFBO = in.getInt();
        this.offsetMH2O = in.getInt();
        this.offsetMTFX = in.getInt();
    }

    private MCIN[] readMCIN() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }        

        super.data.position(GLOBAL_OFFSET + this.offsetMCIN);

        checkHeader(HEADER_MCIN);

        int size = super.data.getInt();
        MCIN index;
        MCIN[] chunkIndex = new MCIN[size / MCIN.getOBJECT_SIZE()];
        for (int i = 0; i < chunkIndex.length; i++) {
            index = new MCIN();
            index.read(super.data);
            chunkIndex[i] = index;
        }

        return chunkIndex;
    }

    public List<String> getTextures() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMTEX + GLOBAL_OFFSET, HEADER_MTEX);
    }

    public List<String> getModels() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMMDX + GLOBAL_OFFSET, HEADER_MMDX);
    }

    public List<Integer> getModelOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMMID + GLOBAL_OFFSET, HEADER_MMID);
    }

    public List<String> getWorldObjects() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMWMO + GLOBAL_OFFSET, HEADER_MWMO);
    }

    public List<Integer> getWorldObjectsOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMWID + GLOBAL_OFFSET, HEADER_MWID);
    }

    public List<MDDF> getDoodadPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MDDF> listPlacement = new ArrayList<>();

        super.data.position(this.offsetMDDF + GLOBAL_OFFSET);

        checkHeader(HEADER_MDDF);

        int size = super.data.getInt();
        int start = super.data.position();
        MDDF placement;
        while (super.data.position() - start < size) {
            placement = new MDDF();
            placement.read(super.data);
            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MODF> getWorldObjectsPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MODF> listPlacement = new ArrayList<>();        
        super.data.position(this.offsetMODF + GLOBAL_OFFSET);

        checkHeader(HEADER_MODF);

        int size = super.data.getInt();
        int start = super.data.position();
        MODF placement;
        while (super.data.position() - start < size) {
            placement = new MODF();
            placement.read(super.data);            
            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MCNK> getMapChunks() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MCNK> listMapChunks = new ArrayList<>();
        
        MCIN[] chunks = readMCIN();

        MCNK chunk;
        for (int i = 0; i < chunks.length; i++) {            
            super.data.position(chunks[i].getOffsetMCNK());
            
            checkHeader(HEADER_MCNK);

            // We ignore size.
            super.data.getInt();
            chunk = new MCNK();
            chunk.read(super.data);                        
            listMapChunks.add(chunk);
        }

        return listMapChunks;
    }    

    private String[][] getLiquidMap(boolean displayLiquidType, int layer) throws FileReaderException {
        List<MCNK> mapChunks = getMapChunks();

        String[][] liquids = new String[SIZE_LIQUID_MAP][SIZE_LIQUID_MAP];
        int idx = 0;
        int idy = 0;
        for (MCNK chunk : mapChunks) {
            MCLQ liquid = chunk.getListLiquids().get(layer);
            if (liquid == null) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                    }
                }
            } else {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        if (liquid.hasNoLiquid(i, j)) {
                            liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                        } else if (liquid.hasLiquid(i, j)) {
                            if (liquid.isDark(i, j)) {
                                if (displayLiquidType) {
                                    String letter = "D";
                                    if (chunk.isRiver()) {
                                        letter += "R ";
                                    } else if (chunk.isOcean()) {
                                        letter += "O ";
                                    } else if (chunk.isMagma()) {
                                        letter += "M ";
                                    } else if (chunk.isSlime()) {
                                        letter += "S ";
                                    }
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = letter;
                                } else {
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "D ";
                                }
                            } else {
                                if (displayLiquidType) {
                                    String letter = "L";
                                    if (chunk.isRiver()) {
                                        letter += "R ";
                                    } else if (chunk.isOcean()) {
                                        letter += "O ";
                                    } else if (chunk.isMagma()) {
                                        letter += "M ";
                                    } else if (chunk.isSlime()) {
                                        letter += "S ";
                                    }
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = letter;
                                } else {
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "L ";
                                }
                            }
                        }
                    }
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        return liquids;
    }

    public void saveLiquidMap(String pngPath, int layer, boolean displayLiquidType) throws FileReaderException, IOException {
        BufferedImage img = new BufferedImage(SIZE_LIQUID_MAP, SIZE_LIQUID_MAP, BufferedImage.TYPE_INT_RGB);
        int alpha = 100;
        int idx = 0;
        int idy = 0;
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            MCLQ liquid = chunk.getListLiquids().get(layer);
            if (liquid == null) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        if (liquid.hasNoLiquid(i, j)) {
                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                        } else if (liquid.hasLiquid(i, j)) {
                            if (liquid.isDark(i, j)) {
                                if (displayLiquidType) {                                    
                                    if (chunk.isRiver()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 112, 192, alpha).getRGB());
                                    } else if (chunk.isOcean()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 32, 96, alpha).getRGB());                                        
                                    } else if (chunk.isMagma()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(255, 192, 0, alpha).getRGB());
                                    } else if (chunk.isSlime()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(55, 86, 35, alpha).getRGB());
                                    }                                    
                                } else {
                                    img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 112, 192, alpha).getRGB());
                                }
                            } else {
                                if (displayLiquidType) {                                    
                                    if (chunk.isRiver()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                    } else if (chunk.isOcean()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(112, 48, 160, alpha).getRGB());
                                    } else if (chunk.isMagma()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(244, 176, 132, alpha).getRGB());
                                    } else if (chunk.isSlime()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(169, 208, 142, alpha).getRGB());
                                    }                                    
                                } else {
                                    img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                }
                            }
                        }
                    }
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }
                
        ImageIO.write(img, "PNG", imgFile);
    }
    
    public void printLiquidMap(boolean displayLiquidType, int layer) throws FileReaderException {
        String[][] liquids = getLiquidMap(displayLiquidType, layer);

        for (int i = 0; i < liquids.length; i++) {
            for (int j = 0; j < liquids[i].length; j++) {
                System.out.print(liquids[j][i]);
            }
            System.out.println();
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}