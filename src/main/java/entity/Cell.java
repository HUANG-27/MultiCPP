package entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Cell {

    //在金字塔中的位置
    public Index index;
    //覆盖类型
    public Coverage coverage;
    //大小
    public int sidePixelCount;
    public int size;
    //所属机器人
    public Robot robot;
    //邻域
    public List<Cell> neighbors;
    //到区域中心的距离
    public double distanceToArea;
    // 到其他区域中心的距离之和
    public double averageDistanceToOtherAreas;
    //到未分配cell的中心的距离
    public double distanceToUnassignedCellCenter;

    //在前期的整个准备过程中，主要是建立下面索引，方便查找邻域
    //同层索引
    public Cell prevXCell;
    public Cell nextXCell;
    public Cell prevYCell;
    public Cell nextYCell;
    //上层索引
    public Cell fatherCell;
    //下层索引
    public Cell[] childCells;
    //位运算（0001，0010，0100，1000）表示四个方向
    public int neighborDirections; //位运算（0001，0010，0100，1000）表示

    //被删除过的robot，防止两个区域争抢同一个cell
    public List<Robot> deletedRobots;

    //生成树的节点
    public Cell preCell;
    public List<Cell> nextCells;

    public Cell(Index index, Coverage coverage, int sidePixelCount) {
        this.index = index;
        this.coverage = coverage;
        this.sidePixelCount = sidePixelCount;
        this.neighbors = new ArrayList<Cell>();
        this.deletedRobots = new ArrayList<Robot>();
        this.nextCells = new ArrayList<Cell>();
    }

    public boolean isTheSameAs(Cell cell) {
        return cell.index.xIndex == this.index.xIndex
                && cell.index.yIndex == this.index.yIndex
                && cell.index.layerIndex == this.index.layerIndex;
    }

    public boolean isDeletedRobot(Robot robot) {
        for (Robot robot1 : deletedRobots) {
            if (robot1.id == robot.id)
                return true;
        }
        return false;
    }

    public Point getCenter() {
        int x = this.index.xIndex * this.sidePixelCount + this.sidePixelCount / 2;
        int y = this.index.yIndex * this.sidePixelCount + this.sidePixelCount / 2;
        return new Point(x, y);
    }
}
