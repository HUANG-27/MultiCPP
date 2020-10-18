package entity;

import javafx.geometry.Point2D;

import java.util.*;
import java.util.List;

public class Pyramid {

    /**
     * 需要覆盖区域的分类结果图，分辨率，左上坐标，地物类型
     * 地物类型包含机器人在该种地物中的覆盖范围和移动速度
     */
    private ClassicImage classicImage;
    private List<Coverage> coverages;

    public List<Layer> layers;

    public Pyramid(ClassicImage classicImage, List<Coverage> coverages) {
        this.classicImage = classicImage;
        this.coverages = coverages;
    }

    /**
     * 建立层状结构，确定层数和每层的格子大小
     */
    public void buildPyramid() {

        //统计不同地物类型的覆盖范围的最值
        double maxCoverageRange = Double.MIN_VALUE;
        double minCoverageRange = Double.MAX_VALUE;
        for (Coverage coverage : coverages) {
            maxCoverageRange = Math.max(coverage.range, maxCoverageRange);
            minCoverageRange = Math.min(coverage.range, minCoverageRange);
        }

        //覆盖范围转换为覆盖行列数
        double maxCoveragePixelCount = Math.floor(maxCoverageRange / classicImage.resolution);
        double minCoveragePixelCount = Math.floor(minCoverageRange / classicImage.resolution);
        //计算不同分辨率的层数，考虑生成树建立的方法，需要多一层（+1）
        int layerCount = (int) Math.ceil(Math.log(maxCoveragePixelCount / minCoveragePixelCount) / Math.log(2)) + 1;
        //为了划分方便，取 maxCount = (2^n) * minCount
        maxCoveragePixelCount = minCoveragePixelCount * Math.pow(2, layerCount - 1);
        //计算最大分辨率的行列数
        int widthOfMaxCoveragePixelCount = (int) Math.ceil(classicImage.image.getWidth() / maxCoveragePixelCount);
        int heightOfMaxCoveragePixelCount = (int) Math.ceil(classicImage.image.getHeight() / maxCoveragePixelCount);

        //先按照最大的覆盖范围划分
        //然后按照四叉树的方式划分
        //直到格子大小小于最小覆盖范围
        layers = new ArrayList<Layer>();
        int w = widthOfMaxCoveragePixelCount;
        int h = heightOfMaxCoveragePixelCount;
        int pixelCount = (int) maxCoveragePixelCount;
        for (int i = 0; i < layerCount; i++) {
            Layer layer = new Layer(i, pixelCount, w, h);
            layers.add(layer);
            //下一层
            w *= 2;
            h *= 2;
            pixelCount /= 2;
        }


        System.out.println("----- image info -----");
        System.out.println("width: " + classicImage.image.getWidth());
        System.out.println("height: " + classicImage.image.getHeight());
        for (int i = 0; i < layerCount; i++) {
            Layer layer = layers.get(i);
            System.out.println(i + " " + layer.cells[0][0].sidePixelCount + " w:" + layer.cells.length + " h:" + layer.cells[0].length);
        }
    }

    /**
     * 为树的每一个节点赋值，每个cell都有相应的Coverage类型，包含视域范围和搜索速度
     * 对于tree，从下往上赋值，namely采用合并建立四叉树
     */
    public void setCoverages() {

        int layerCount = layers.size();

        //最底层不用赋值，从次底层开始
        Layer subBottomLayer = layers.get(layerCount - 2);
        int cellSidePixelCount = subBottomLayer.cellSidePixelCount;
        for (int i = 0; i < subBottomLayer.cells.length; i++) {
            for (int j = 0; j < subBottomLayer.cells[0].length; j++) {

                //地物类型id和其数量统计的结果
                int[] numbers = new int[coverages.size()];

                for (int p = 0; p < cellSidePixelCount; p++) {
                    for (int q = 0; q < cellSidePixelCount; q++) {

                        int x = cellSidePixelCount * i + p;
                        int y = cellSidePixelCount * j + q;
                        if (x >= classicImage.image.getRaster().getWidth() || y >= classicImage.image.getRaster().getHeight())
                            continue;

                        int value = classicImage.image.getRaster().getSample(x, y, 0);
                        //System.out.println(value);
                        int index = findCoverageIndexByValue(value);
                        //System.out.println(index);
                        if (index != -1)
                            numbers[index]++;
                    }
                }

                double maxNumber = -1;
                int maxNumberIndex = -1;
                for (int k = 0; k < coverages.size(); k++) {
                    double tempWeightedNumber = numbers[k];
                    if (tempWeightedNumber > maxNumber) {
                        maxNumber = tempWeightedNumber;
                        maxNumberIndex = k;
                    }
                }

                //找到最大的那个
                if (maxNumber != 0)  //如果所有的覆盖类型统计都是0，则在给定区域外
                    subBottomLayer.cells[i][j].coverage = coverages.get(maxNumberIndex);
            }
        }

        //再往上重采样即可
        for (int l = layerCount - 3; l >= 0; l--) {

            Layer tempLayer = layers.get(l);
            Layer tempLowerLayer = layers.get(l + 1);
            for (int i = 0; i < tempLayer.cells.length; i++) {
                for (int j = 0; j < tempLayer.cells[0].length; j++) {
                    Cell cellLT = tempLowerLayer.cells[i * 2][j * 2];
                    Cell cellLB = tempLowerLayer.cells[i * 2 + 1][j * 2];
                    Cell cellRT = tempLowerLayer.cells[i * 2][j * 2 + 1];
                    Cell cellRB = tempLowerLayer.cells[i * 2 + 1][j * 2 + 1];

                    //如果四个格子的地物类型一样，并且合并后格子的大小小于该种地物的可视范围，则合并为大的格子
                    if (cellLT.coverage == null
                            || cellLB.coverage == null
                            || cellRT.coverage == null
                            || cellRB.coverage == null)
                        tempLayer.cells[i][j].coverage = null;
                    else if (cellLT.coverage.value == cellLB.coverage.value
                            && cellLT.coverage.value == cellRT.coverage.value
                            && cellLT.coverage.value == cellRB.coverage.value
                            && cellLT.coverage.range * 2 >= tempLayer.cellSidePixelCount * classicImage.resolution) {
                        tempLayer.cells[i][j].coverage = cellLT.coverage;
                        cellLT.coverage = null;
                        cellLB.coverage = null;
                        cellRT.coverage = null;
                        cellRB.coverage = null;
                    } else
                        tempLayer.cells[i][j].coverage = null;
                }
            }
        }

        statistic();
        statistic2();
    }


    public void statistic() {
        int num = coverages.size() + 1;
        int[] N = new int[num];
        int w = classicImage.image.getRaster().getWidth();
        int h = classicImage.image.getRaster().getHeight();
        for (int p = 0; p < w; p++) {
            for (int q = 0; q < h; q++) {
                int value = classicImage.image.getRaster().getSample(p, q, 0);
                N[value]++;
            }
        }
        double S = 0;
        double T = 0;
        for (int i = 1; i < num; i++) {
            S += N[i] * 0.8333 * 0.8333 / 5 * coverages.get(findCoverageIndexByValue(i)).range;
            T += N[i] * 0.8333 * 0.8333 / 5 / coverages.get(findCoverageIndexByValue(i)).speed;
        }
        System.out.println("----- statistic info -----");
        System.out.println("A-STC recover rate: " + (S / (900 * 900) - 1));
        System.out.println("A-STC coverage time: " + T);
        System.out.println("A-STC running time: ");
    }

    public void statistic2() {
        double S = 0;
        double T = 0;
        for (int i = 0; i < layers.size(); i++) {
            for (Cell[] cells : layers.get(i).cells) {
                for (Cell cell : cells) {
                    if (cell.coverage != null) {
                        S += cell.coverage.range * cell.sidePixelCount * 0.8333 * 2;
                        T += cell.sidePixelCount * 0.8333 / cell.coverage.speed * 2;
                    }
                }
            }
        }
        System.out.println("MLCT-MCPP recover rate: " + (S / (900 * 900) - 1));
        System.out.println("MLCT-MCPP coverage time: " + T);
    }

    /**
     * 建立每一层内部、层与层之间的索引关系，从而减少格子大小不同带来的搜索难度
     */
    public void setIndexes() {

        int layerCount = layers.size();
        for (int i = layerCount - 1; i >= 0; i--) {

            Layer layer = layers.get(i);
            int w = layer.cells.length;
            int h = layer.cells[0].length;

            //同层索引
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < h; k++) {

                    if (j != 0) {
                        layer.cells[j][k].prevXCell = layer.cells[j - 1][k];
                    }
                    if (j != w - 1) {
                        layer.cells[j][k].nextXCell = layer.cells[j + 1][k];
                    }
                    if (k != 0) {
                        layer.cells[j][k].prevYCell = layer.cells[j][k - 1];
                    }
                    if (k != h - 1) {
                        layer.cells[j][k].nextYCell = layer.cells[j][k + 1];
                    }
                }
            }

            //上层索引
            if (i != 0) {
                for (int j = 0; j < w; j++) {
                    for (int k = 0; k < h; k++) {
                        layer.cells[j][k].fatherCell = layers.get(i - 1).cells[j / 2][k / 2];
                    }
                }
            }
            //下层索引
            if (i != layerCount - 1) {
                for (int j = 0; j < w; j++) {
                    for (int k = 0; k < h; k++) {
                        /**
                         *      **8**
                         *      1***4
                         * (0,0)**2**
                         *
                         */
                        layer.cells[j][k].childCells = new Cell[4];
                        layer.cells[j][k].childCells[0] = layers.get(i + 1).cells[j * 2][k * 2];
                        layer.cells[j][k].childCells[0].neighborDirections = 3;
                        layer.cells[j][k].childCells[1] = layers.get(i + 1).cells[j * 2 + 1][k * 2];
                        layer.cells[j][k].childCells[1].neighborDirections = 6;
                        layer.cells[j][k].childCells[2] = layers.get(i + 1).cells[j * 2][k * 2 + 1];
                        layer.cells[j][k].childCells[2].neighborDirections = 9;
                        layer.cells[j][k].childCells[3] = layers.get(i + 1).cells[j * 2 + 1][k * 2 + 1];
                        layer.cells[j][k].childCells[3].neighborDirections = 12;
                    }
                }
            }
        }
    }


    /**
     * 寻找cell的邻域点
     *
     * @param cell 中心
     * @return 领域cell
     */
    public List<Cell> findCellNeighbor(Cell cell) {
        List<Cell> cells = new ArrayList<Cell>();

        //同层搜索
        if (cell.prevXCell != null && cell.prevXCell.coverage != null)
            cells.add(cell.prevXCell);
        if (cell.nextXCell != null && cell.nextXCell.coverage != null)
            cells.add(cell.nextXCell);
        if (cell.prevYCell != null && cell.prevYCell.coverage != null)
            cells.add(cell.prevYCell);
        if (cell.nextYCell != null && cell.nextYCell.coverage != null)
            cells.add(cell.nextYCell);

        //向上搜索
        findUpperNeighborCells(cell, 15, cells);

        //向下搜索
        findLowerNeighborCells(cell, 15, cells);

        return cells;
    }

    /**
     * 寻找非空cell的邻域
     */
    public void setCellNeighbors() {
        for (Layer layer : layers) {
            for (Cell[] cells : layer.cells) {
                for (Cell cell : cells) {
                    if (cell.coverage != null) {
                        cell.neighbors = findCellNeighbor(cell);
                    }
                }
            }
        }
    }

    //向上递归搜索
    private void findUpperNeighborCells(Cell cell, int limitation, List<Cell> cells) {

        Cell upperLayerCell = cell.fatherCell;
        if (upperLayerCell == null || limitation == 0)
            return;

        int newLimitation = cell.neighborDirections & limitation;
        switch (newLimitation) {
            case 1:
                if (upperLayerCell.prevXCell != null) {
                    //有覆盖的加进去
                    if (upperLayerCell.prevXCell.coverage != null)
                        cells.add(upperLayerCell.prevXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 2:
                if (upperLayerCell.prevYCell != null) {
                    if (upperLayerCell.prevYCell.coverage != null)
                        cells.add(upperLayerCell.prevYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 4:
                if (upperLayerCell.nextXCell != null) {
                    if (upperLayerCell.nextXCell.coverage != null)
                        cells.add(upperLayerCell.nextXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 8:
                if (upperLayerCell.nextYCell != null) {
                    if (upperLayerCell.nextYCell.coverage != null)
                        cells.add(upperLayerCell.nextYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 3:
                if (upperLayerCell.prevXCell != null) {
                    if (upperLayerCell.prevXCell.coverage != null)
                        cells.add(upperLayerCell.prevXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                if (upperLayerCell.prevYCell != null) {
                    if (upperLayerCell.prevYCell.coverage != null)
                        cells.add(upperLayerCell.prevYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 6:
                if (upperLayerCell.prevYCell != null) {
                    if (upperLayerCell.prevYCell.coverage != null)
                        cells.add(upperLayerCell.prevYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                if (upperLayerCell.nextXCell != null) {
                    if (upperLayerCell.nextXCell.coverage != null)
                        cells.add(upperLayerCell.nextXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 9:
                if (upperLayerCell.nextYCell != null) {
                    if (upperLayerCell.nextYCell.coverage != null)
                        cells.add(upperLayerCell.nextYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                if (upperLayerCell.prevXCell != null) {
                    if (upperLayerCell.prevXCell.coverage != null)
                        cells.add(upperLayerCell.prevXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
            case 12:
                if (upperLayerCell.nextXCell != null) {
                    if (upperLayerCell.nextXCell.coverage != null)
                        cells.add(upperLayerCell.nextXCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                if (upperLayerCell.nextYCell != null) {
                    if (upperLayerCell.nextYCell.coverage != null)
                        cells.add(upperLayerCell.nextYCell);
                    findUpperNeighborCells(upperLayerCell, newLimitation, cells);
                }
                break;
        }
    }

    //向下递归搜索
    private void findLowerNeighborCells(Cell cell, int limitation, List<Cell> cells) {
        Cell[] lowerLayerCells = cell.childCells;
        if (lowerLayerCells == null || limitation == 0)
            return;

        for (Cell tempCell : lowerLayerCells) {
            int newLimitation = limitation & tempCell.neighborDirections;
            switch (newLimitation) {
                case 1:
                    if (tempCell.prevXCell != null) {
                        if (tempCell.prevXCell.coverage != null)
                            cells.add(tempCell.prevXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 2:
                    if (tempCell.prevYCell != null) {
                        if (tempCell.prevYCell.coverage != null)
                            cells.add(tempCell.prevYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 4:
                    if (tempCell.nextXCell != null) {
                        if (tempCell.nextXCell.coverage != null)
                            cells.add(tempCell.nextXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 8:
                    if (tempCell.nextYCell != null) {
                        if (tempCell.nextYCell.coverage != null)
                            cells.add(tempCell.nextYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 3:
                    if (tempCell.prevXCell != null) {
                        if (tempCell.prevXCell.coverage != null)
                            cells.add(tempCell.prevXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    if (tempCell.prevYCell != null) {
                        if (tempCell.prevYCell.coverage != null)
                            cells.add(tempCell.prevYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 6:
                    if (tempCell.prevYCell != null) {
                        if (tempCell.prevYCell.coverage != null)
                            cells.add(tempCell.prevYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    if (tempCell.nextXCell != null) {
                        if (tempCell.nextXCell.coverage != null)
                            cells.add(tempCell.nextXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 9:
                    if (tempCell.nextYCell != null) {
                        if (tempCell.nextYCell.coverage != null)
                            cells.add(tempCell.nextYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    if (tempCell.prevXCell != null) {
                        if (tempCell.prevXCell.coverage != null)
                            cells.add(tempCell.prevXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
                case 12:
                    if (tempCell.nextXCell != null) {
                        if (tempCell.nextXCell.coverage != null)
                            cells.add(tempCell.nextXCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    if (tempCell.nextYCell != null) {
                        if (tempCell.nextYCell.coverage != null)
                            cells.add(tempCell.nextYCell);
                        findLowerNeighborCells(tempCell, newLimitation, cells);
                    }
                    break;
            }
        }
    }

    // 根据value查找其对应的覆盖类型在覆盖类型列表中的位置
    private int findCoverageIndexByValue(int value) {
        for (int i = 0; i < coverages.size(); i++) {
            if (coverages.get(i).value == value)
                return i;
        }
        return -1;
    }

    /**
     * 计算坐标position在pyramid中的位置
     *
     * @param position 坐标
     * @return 在pyramid中的位置
     */
    public Cell getCellInPyramid(Point2D position) {
        int xPixelCount = (int) Math.floor(
                (position.getX() - classicImage.offset.getX()) / classicImage.resolution);
        int yPixelCount = (int) Math.floor(
                (position.getY() - classicImage.offset.getY()) / classicImage.resolution);
        Layer subBottomLayer = layers.get(layers.size() - 2);
        int xCount = xPixelCount / subBottomLayer.cellSidePixelCount;
        int yCount = yPixelCount / subBottomLayer.cellSidePixelCount;
        Cell cell = subBottomLayer.cells[xCount][yCount];
        while (cell.coverage == null) {
            cell = cell.fatherCell;
        }
        return cell;
    }

    public int getCellCount() {
        int cellCount = 0;
        for (Layer layer : layers) {
            for (Cell[] cells : layer.cells) {
                for (Cell cell : cells) {
                    if (cell.coverage != null)
                        cellCount++;
                }
            }
        }
        return cellCount;
    }

}
