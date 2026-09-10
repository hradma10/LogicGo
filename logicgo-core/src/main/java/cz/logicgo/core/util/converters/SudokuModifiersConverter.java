package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.RossiniType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.*;

@Converter(autoApply = false)
public class SudokuModifiersConverter implements AttributeConverter<SudokuModifiers, byte[]> {

    private static final byte ID_EVEN_ODD = 1;
    private static final byte ID_CONSECUTIVE = 2;
    private static final byte ID_XV = 3;
    private static final byte ID_VUDOKU = 4;
    private static final byte ID_BETWEEN = 5;
    private static final byte ID_GREATER_HORIZ = 6;
    private static final byte ID_GREATER_VERT = 7;
    private static final byte ID_SKYSCRAPER = 8;
    private static final byte ID_SANDWICH = 9;
    private static final byte ID_XSUMS = 10;
    private static final byte ID_QUADRUPLES = 11;
    private static final byte ID_KILLER = 12;
    private static final byte ID_GROUP_SUMS = 13;
    private static final byte ID_KROPKI = 14;
    private static final byte EOF = 0;

    @Override
    public byte[] convertToDatabaseColumn(SudokuModifiers attribute) {
        if (attribute == null || !attribute.atLeastOneActive()) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {

            if (attribute.hasEvenOdd()) {
                writeChunk(dos, ID_EVEN_ODD, paritiesToBytes(attribute.getEvenOdd().parityTypes()));
            }
            if (attribute.hasConsecutive()) {
                writeChunk(dos, ID_CONSECUTIVE, consecutivePairsToBytes(attribute.getConsecutive().cells()));
            }
            if (attribute.hasXv()) {
                writeChunk(dos, ID_XV, xvPairsToBytes(attribute.getXv().marks()));
            }
            if (attribute.hasVudoku()) {
                writeChunk(dos, ID_VUDOKU, vudokuMarksToBytes(attribute.getVudoku().marks()));
            }
            if (attribute.hasBetween()) {
                writeChunk(dos, ID_BETWEEN, serializeBetweenLines(attribute.getBetween().lines()));
            }

            if (attribute.hasGreaterThan()) {
                GreaterThanModifier gt = attribute.getGreaterThan();
                if (gt.horizontal() != null) {
                    writeChunk(dos, ID_GREATER_HORIZ, compTypesToBytes(Arrays.asList(gt.horizontal())));
                }
                if (gt.vertical() != null) {
                    writeChunk(dos, ID_GREATER_VERT, compTypesToBytes(Arrays.asList(gt.vertical())));
                }
            }

            if (attribute.hasSkyscraper()) {
                writeChunk(dos, ID_SKYSCRAPER, serializeSkyscraper(attribute.getSkyscraper()));
            }

            if (attribute.hasSandwich()) {
                writeChunk(dos, ID_SANDWICH, serializeSandwich(attribute.getSandwich()));
            }

            if (attribute.hasXSums()) {
                writeChunk(dos, ID_XSUMS, serializeXSums(attribute.getXSums()));
            }

            if (attribute.hasQuadruples()) {
                writeChunk(dos, ID_QUADRUPLES, serializeQuadruples(attribute.getQuadruples()));
            }
            if (attribute.hasGroupSums()) {
                writeChunk(dos, ID_GROUP_SUMS, serializeGroupSums(attribute.getGroupSums()));
            }
            if (attribute.hasKropki()) {
                writeChunk(dos, ID_KROPKI, serializeKropki(attribute.getKropki()));
            }
            if (attribute.hasKiller()) {
                writeChunk(dos, ID_KILLER, serializeKiller(attribute.getKiller()));
            }

            dos.writeByte(EOF);
        } catch (IOException e) {

            return null;
        }

        return compress(baos.toByteArray());
    }

    @Override
    public SudokuModifiers convertToEntityAttribute(byte[] dbData) {
        SudokuModifiers mods = new SudokuModifiers();
        if (dbData == null || dbData.length == 0) return mods;

        dbData = decompress(dbData);

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dbData))) {

            CompType[][] gtHoriz = null, gtVert = null;
            RossiniType[] rosHoriz = null, rosVert = null;

            while (dis.available() > 0) {
                byte id = dis.readByte();
                if (id == EOF) break;

                int length = dis.readInt();
                byte[] data = dis.readNBytes(length);

                switch (id) {
                    case ID_EVEN_ODD -> mods.setEvenOdd(new EvenOddModifier(bytesToParities(data)));
                    case ID_CONSECUTIVE -> mods.setConsecutive(new ConsecutiveModifier(bytesToConsecutivePairs(data)));
                    case ID_XV -> mods.setXv(new XvModifier(bytesToXVPairs(data)));
                    case ID_VUDOKU -> mods.setVudoku(new VudokuModifier(bytesToVudokuMarks(data)));
                    case ID_BETWEEN -> mods.setBetween(new BetweenModifier(deserializeBetweenLines(data)));

                    case ID_GREATER_HORIZ -> gtHoriz = bytesToCompTypeArray(data).toArray(new CompType[0][]);
                    case ID_GREATER_VERT -> gtVert = bytesToCompTypeArray(data).toArray(new CompType[0][]);

                    case ID_SKYSCRAPER -> mods.setSkyscraper(deserializeSkyscraper(data));

                    case ID_SANDWICH -> mods.setSandwich(deserializeSandwich(data));

                    case ID_XSUMS -> mods.setXSums(deserializeXSums(data));

                    case ID_QUADRUPLES -> mods.setQuadruples(deserializeQuadruples(data));
                    case ID_GROUP_SUMS -> mods.setGroupSums(deserializeGroupSums(data));
                    case ID_KILLER -> mods.setKiller(deserializeKiller(data));
                    case ID_KROPKI -> mods.setKropki(deserializeKropki(data));
                }
            }

            if (gtHoriz != null || gtVert != null) {
                mods.setGreaterThan(new GreaterThanModifier(gtHoriz, gtVert));
            }

        } catch (IOException e) {

        }

        return mods;
    }


    private void writeChunk(DataOutputStream dos, byte id, byte[] data) throws IOException {
        if (data == null || data.length == 0) return;
        dos.writeByte(id);
        dos.writeInt(data.length);
        dos.write(data);
    }

    private byte[] serializeSkyscraper(SkyscraperModifier mod) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        writeByteArray(dos, mod.top());
        writeByteArray(dos, mod.bottom());
        writeByteArray(dos, mod.left());
        writeByteArray(dos, mod.right());
        return baos.toByteArray();
    }

    private byte[] serializeSandwich(SandwichModifier mod) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        writeShortArray(dos, mod.top());
        writeShortArray(dos, mod.left());
        return baos.toByteArray();
    }

    private byte[] serializeXSums(XSumsModifier mod) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        writeShortArray(dos, mod.top());
        writeShortArray(dos, mod.bottom());
        writeShortArray(dos, mod.left());
        writeShortArray(dos, mod.right());
        return baos.toByteArray();
    }


    private byte[] serializeKiller(KillerModifier mod) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            var cages = mod.cages();
            dos.writeByte(cages.size());
            for (var cage : cages) {
                var cells = cage.cells();
                dos.writeByte(cells.size());
                for (var cell : cells) {
                    dos.writeByte(cell.row());
                    dos.writeByte(cell.col());
                }
                dos.writeByte(cage.targetSum());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private KillerModifier deserializeKiller(byte[] data) {
        if (data == null || data.length == 0) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readByte();

            List<KillerCage> groups = new ArrayList<>();

            while (count > 0) {
                int countCageSize = dis.readByte();
                List<GridCell> cells = new ArrayList<>();
                while (countCageSize > 0) {
                    int row = dis.readByte();
                    int col = dis.readByte();
                    cells.add(new GridCell(row, col));
                    countCageSize--;
                }
                int num = dis.readByte();
                groups.add(new KillerCage(num, cells));
                count--;
            }

            return new KillerModifier(groups);

        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }


    private byte[] serializeQuadruples(QuadruplesModifier mod) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(mod.marks().size());

            for (var mark : mod.marks()) {
                dos.writeByte(mark.topLeft().row());
                dos.writeByte(mark.topLeft().col());

                dos.writeByte(mark.values().size());
                for (int val : mark.values()) {
                    dos.writeByte(val);
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {

            return new byte[0];
        }
    }

    private QuadruplesModifier deserializeQuadruples(byte[] data) {
        if (data == null || data.length == 0) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readByte();
            List<QuadrupleMark> marks = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                int row = dis.readByte();
                int col = dis.readByte();

                int valCount = dis.readByte();
                List<Integer> values = new ArrayList<>();
                for (int j = 0; j < valCount; j++) {
                    values.add((int) dis.readByte());
                }

                marks.add(new QuadrupleMark(new GridCell(row, col), values));
            }

            return new QuadruplesModifier(marks);

        } catch (IOException | IllegalArgumentException e) {

            return null;
        }
    }

    private byte[] serializeGroupSums(GroupSumsModifier mod) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(mod.marks().size());

            for (var mark : mod.marks()) {
                dos.writeByte(mark.topLeft().row());
                dos.writeByte(mark.topLeft().col());
                dos.writeByte(mark.targetSum());
            }

            return baos.toByteArray();
        } catch (IOException e) {

            return new byte[0];
        }
    }

    private GroupSumsModifier deserializeGroupSums(byte[] data) {
        if (data == null || data.length == 0) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readByte();
            List<GroupSumMark> marks = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                int row = dis.readByte();
                int col = dis.readByte();
                int targetSum = dis.readByte();

                marks.add(new GroupSumMark(new GridCell(row, col), targetSum));
            }

            return new GroupSumsModifier(marks);

        } catch (IOException | IllegalArgumentException e) {

            return null;
        }
    }

    private XSumsModifier deserializeXSums(byte[] data) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int[] top = readShortArray(dis);
            int[] bottom = readShortArray(dis);
            int[] left = readShortArray(dis);
            int[] right = readShortArray(dis);
            return new XSumsModifier(top, bottom, left, right);
        }
    }

    private SkyscraperModifier deserializeSkyscraper(byte[] data) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int[] top = readByteArray(dis);
            int[] bottom = readByteArray(dis);
            int[] left = readByteArray(dis);
            int[] right = readByteArray(dis);
            return new SkyscraperModifier(top, bottom, left, right);
        }
    }

    private SandwichModifier deserializeSandwich(byte[] data) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int[] top = readShortArray(dis);
            int[] left = readShortArray(dis);
            return new SandwichModifier(top, left);
        }
    }

    private void writeByteArray(DataOutputStream dos, int[] arr) throws IOException {
        if (arr == null) {
            dos.writeByte(0);
        } else {
            dos.writeByte(arr.length);
            for (int v : arr) dos.writeByte(v);
        }
    }

    private byte[] serializeKropki(KropkiModifier kropkiModifier) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeShort(kropkiModifier.dots().size());
            for (KropkiDot dot : kropkiModifier.dots()) {
                dos.writeByte(dot.color().getId());
                dos.writeByte(dot.first().row());
                dos.writeByte(dot.first().col());
                dos.writeByte(dot.second().row());
                dos.writeByte(dot.second().col());
            }

            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }

    }

    private KropkiModifier deserializeKropki(byte[] data) throws IOException {
        List<KropkiDot> dots = new ArrayList<>();

        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

            int size = dis.readShort();
            for (int i = 0; i < size; i++) {
                DotColor dotColor = DotColor.fromId(dis.readByte());
                int rowFirst = dis.readByte();
                int colFirst = dis.readByte();
                GridCell first = new GridCell(rowFirst, colFirst);
                int rowSecond = dis.readByte();
                int colSecond = dis.readByte();
                GridCell second = new GridCell(rowSecond, colSecond);
                KropkiDot dot = new KropkiDot(first, second, dotColor);
                dots.add(dot);
            }

            return new KropkiModifier(dots);
        } catch (Exception e) {
            return new KropkiModifier(new ArrayList<>());
        }

    }

    private int[] readByteArray(DataInputStream dis) throws IOException {
        int len = dis.readByte();
        if (len == 0) return null;
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) arr[i] = dis.readByte();
        return arr;
    }

    private void writeShortArray(DataOutputStream dos, int[] arr) throws IOException {
        if (arr == null) {
            dos.writeByte(0);
        } else {
            dos.writeByte(arr.length);
            for (int v : arr) dos.writeShort(v);
        }
    }

    private int[] readShortArray(DataInputStream dis) throws IOException {
        int len = dis.readByte();
        if (len == 0) return null;
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) arr[i] = dis.readShort();
        return arr;
    }
}
