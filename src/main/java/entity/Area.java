package entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Area {

    //所属机器人
    public Robot robot;
    //覆盖区域
    public List<Cell> inners;
    //覆盖区域的邻域
    public List<Cell> neighbors;
    //覆盖区域的中心（像素表示）
    public Point center;
    //覆盖区域的覆盖时间
    public double coverageTime;
    //区域的生成树
    public Tree tree;
    //区域的覆盖路径
    public List<Segment> path;

    public Area(Robot robot) {
        //初始化
        this.robot = robot;
        this.inners = new ArrayList<Cell>();
        this.inners.add(robot.initialCell);
        robot.initialCell.robot = robot;
        this.tree = new Tree(robot.initialCell);
        this.path = new ArrayList<Segment>();
        this.neighbors = new ArrayList<Cell>();
        this.neighbors.addAll(robot.initialCell.neighbors);
        this.center = robot.initialCell.getCenter();
        this.coverageTime = robot.initialCell.sidePixelCount * 0.8333 / robot.initialCell.coverage.speed * 2;
    }

    public void andCell(Cell cell) {

        int cellIndexInOriginalNeighbors = this.findCellIndexInNeighbors(cell);
        //判断当前的点是不是在邻域
        if (cellIndexInOriginalNeighbors == -1)
            return;

        //加入到区域
        this.inners.add(cell);
        cell.robot = this.robot;
        //更新邻域
        this.neighbors.remove(cellIndexInOriginalNeighbors);
        List<Cell> tempCellNeighbors = cell.neighbors;
        for (Cell neighborCell : tempCellNeighbors) {
            if (!this.isCellInArea(neighborCell) && this.findCellIndexInNeighbors(neighborCell) == -1)
                this.neighbors.add(neighborCell);
        }
        //计算中心
        this.center = getCenter();
        //计算覆盖时间
        this.coverageTime += (cell.sidePixelCount * 0.8333 / cell.coverage.speed) * 2;
    }

    public void deleteCell(Cell cell) {

        int cellIndexInArea = findCellIndexInArea(cell);
        //判断当前的点是不是在区域
        if (cellIndexInArea == -1)
            return;
        //当前点是起始点或者唯一连接点不能删除
        if (!isAbleToDeleteCell(cell))
            return;

        //从区域中删除
        this.inners.remove(cellIndexInArea);
        cell.robot = null;
        cell.deletedRobots.add(robot);

        //更新邻域
        // 对被删点的邻域点中属于区域邻域的点进行判断，如果该点有邻域在区域内，保留，否则删除
        List<Cell> tempCellNeighbors = cell.neighbors;  //被删点的邻域点
        for (Cell tempCellNeighborCell : tempCellNeighbors) {
            int tempCellNeighborCellIndexInNeighbors = this.findCellIndexInNeighbors(tempCellNeighborCell);
            //被删点的邻域点中属于区域邻域的点
            if (tempCellNeighborCellIndexInNeighbors != -1) {
                //该点的邻域
                List<Cell> tempCellNeighborCellNeighbors = tempCellNeighborCell.neighbors;
                //是否有邻域在区域内
                boolean hasNeighborCellInArea = false;
                for (Cell neighborCell : tempCellNeighborCellNeighbors) {
                    if (this.isCellInArea(neighborCell)) {
                        hasNeighborCellInArea = true;
                        break;
                    }
                }
                if (!hasNeighborCellInArea)
                    this.neighbors.remove(tempCellNeighborCellIndexInNeighbors);
            }
        }
        //再将该点加入到邻域内
        this.neighbors.add(cell);

        //计算中心
        this.center = getCenter();
        //计算覆盖时间
        this.coverageTime -= (cell.sidePixelCount * 0.8333 / cell.coverage.speed) * 2;
    }

    private Point getCenter() {
        int totalPixelCount = 0;
        int totalCenterX = 0;
        int totalCenterY = 0;
        for (Cell cell : this.inners) {
            int pixelCount = cell.sidePixelCount * cell.sidePixelCount;
            totalCenterX += cell.getCenter().x * pixelCount;
            totalCenterY += cell.getCenter().y * pixelCount;
            totalPixelCount += pixelCount;
        }
        return new Point(totalCenterX / totalPixelCount,
                totalCenterY / totalPixelCount);
    }

    public boolean isAbleToDeleteCell(Cell cell) {
        //起点不能被删除
        if (this.robot.initialCell.isTheSameAs(cell))
            return false;
            //唯一连接点不能被删除
        else
            return !isUniqueLinkCell(cell);
    }

    private boolean isUniqueLinkCell(Cell cell) {
        //连接点的条件
        //假装先删除该点
        cell.robot = null;
        //连接生成树
        checkLinkOfArea();

        for (Cell neighborCell : cell.neighbors) {
            //如果该点的在区域中的邻域点无法连接到生成树，说明这是唯一连接点
            if (this.isCellInArea(neighborCell)
                    && !this.robot.initialCell.isTheSameAs(neighborCell)
                    && neighborCell.preCell == null) {
                cell.robot = this.robot;
                return true;
            }
        }

        cell.robot = this.robot;
        return false;
    }

    public int findCellIndexInArea(Cell cell) {
        for (int i = 0; i < this.inners.size(); i++) {
            if (this.inners.get(i).isTheSameAs(cell))
                return i;
        }
        return -1;
    }

    private int findCellIndexInNeighbors(Cell cell) {
        for (int i = 0; i < this.neighbors.size(); i++) {
            if (this.neighbors.get(i).isTheSameAs(cell))
                return i;
        }
        return -1;
    }

    public boolean isCellInArea(Cell cell) {
        if (cell.robot == null)
            return false;
        else
            return cell.robot.id == this.robot.id;
    }

    public void checkLinkOfArea() {
        for (Cell cell : this.inners) {
            cell.preCell = null;
            cell.nextCells = new ArrayList<Cell>();
        }
        robot.area.tree = new Tree(robot.initialCell);
        linkNode(robot.area.tree.rootCell);
    }

    private void linkNode(Cell preCell) {
        List<Cell> cells = preCell.neighbors;
        for (Cell cell : cells) {
            if (this.isCellInArea(cell) && !cell.isTheSameAs(robot.area.tree.rootCell)
                    && cell.preCell == null) {
                cell.preCell = preCell;
                preCell.nextCells.add(cell);
                linkNode(cell);
            }
        }
    }

    //Prim
    private double minDis;

    public void buildSpanningTree() {
        for (Cell cell : this.inners) {
            cell.preCell = null;
            cell.nextCells = new ArrayList<Cell>();
        }

        robot.area.tree = new Tree(robot.initialCell);
        for (int i = 0; i < this.inners.size(); i++) {
            minDis = Double.MAX_VALUE;
            Cell treeCell = robot.area.tree.rootCell;
            Cell linkCell = null;
            Cell[] cells = new Cell[2];
            cells[0] = robot.area.tree.rootCell;
            cells[1] = linkCell;
            findLinkCell(treeCell, cells);
            if (cells[1] != null) {
                cells[0].nextCells.add(cells[1]);
                cells[1].preCell = cells[0];
            }
        }
    }

    private void findLinkCell(Cell treeCell, Cell[] cells) {
        for (Cell neighbor : treeCell.neighbors) {
            double newDis = getDis(treeCell, neighbor);
            if (this.isCellInArea(neighbor)
                    && neighbor.preCell == null
                    && neighbor.nextCells.size() == 0
                    && newDis < minDis) {
                minDis = newDis;
                cells[0] = treeCell;
                cells[1] = neighbor;
            }
        }
        for (Cell newTreeCell : treeCell.nextCells)
            findLinkCell(newTreeCell, cells);
    }

    private double getDis(Cell cell1, Cell cell2) {
        double x1 = cell1.index.xIndex * Math.pow(2, 10 - cell1.index.layerIndex);
        double y1 = cell1.index.yIndex * Math.pow(2, 10 - cell1.index.layerIndex);
        double x2 = cell2.index.xIndex * Math.pow(2, 10 - cell2.index.layerIndex);
        double y2 = cell2.index.yIndex * Math.pow(2, 10 - cell2.index.layerIndex);
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private void linkPathBetweenNodes(Cell fatherCell, Cell cell) {

        //节点之间的连接关系
        int direct = getDirect(cell, fatherCell);
        Segment segment1 = null;
        Segment segment2 = null;
        switch (direct) {
            case 1: //从右往左
                segment1 = new Segment(
                        cell.childCells[0].getCenter(),
                        fatherCell.childCells[1].getCenter()
                );
                segment2 = new Segment(
                        cell.childCells[2].getCenter(),
                        fatherCell.childCells[3].getCenter()
                );
                path.add(segment1);
                path.add(segment2);
                break;
            case 2: //从上往下
                segment1 = new Segment(
                        cell.childCells[0].getCenter(),
                        fatherCell.childCells[2].getCenter()
                );
                segment2 = new Segment(
                        cell.childCells[1].getCenter(),
                        fatherCell.childCells[3].getCenter()
                );
                path.add(segment1);
                path.add(segment2);
                break;
            case 4: //从左往右
                segment1 = new Segment(
                        cell.childCells[1].getCenter(),
                        fatherCell.childCells[0].getCenter()
                );
                segment2 = new Segment(
                        cell.childCells[3].getCenter(),
                        fatherCell.childCells[2].getCenter()
                );
                path.add(segment1);
                path.add(segment2);
                break;
            case 8: //从下往上
                segment1 = new Segment(
                        cell.childCells[2].getCenter(),
                        fatherCell.childCells[0].getCenter()
                );
                segment2 = new Segment(
                        cell.childCells[3].getCenter(),
                        fatherCell.childCells[1].getCenter()
                );
                path.add(segment1);
                path.add(segment2);
                break;
        }

    }

    private void linkPathInNode(Cell fatherCell, Cell cell) {

        int direct;
        if (fatherCell == null)
            direct = 0;
        else
            direct = getDirect(cell, fatherCell);

        //节点内部的连接关系
        Segment segment;
        int totalDirect = direct;
        for (Cell cell1 : cell.nextCells) {
            totalDirect = totalDirect | getDirect(cell, cell1);
        }
        if ((totalDirect & 1) == 0) {
            segment = new Segment(
                    cell.childCells[0].getCenter(),
                    cell.childCells[2].getCenter()
            );
            path.add(segment);
        }
        if ((totalDirect & 2) == 0) {
            segment = new Segment(
                    cell.childCells[0].getCenter(),
                    cell.childCells[1].getCenter()
            );
            path.add(segment);
        }
        if ((totalDirect & 4) == 0) {
            segment = new Segment(
                    cell.childCells[3].getCenter(),
                    cell.childCells[1].getCenter()
            );
            path.add(segment);
        }
        if ((totalDirect & 8) == 0) {
            segment = new Segment(
                    cell.childCells[2].getCenter(),
                    cell.childCells[3].getCenter()
            );
            path.add(segment);
        }
    }

    public int getDirect(Cell cell1, Cell cell2) {
        int direct = 0;
        int deltaX = cell2.getCenter().x - cell1.getCenter().x;
        int deltaY = cell2.getCenter().y - cell1.getCenter().y;
        int singleDirectDis = cell2.sidePixelCount / 2 + cell1.sidePixelCount / 2;
        if (Math.abs(deltaX - singleDirectDis) <= 1)
            direct = 4; //从左往右
        else if (Math.abs(deltaX + singleDirectDis) <= 1)
            direct = 1; //从右往左
        else if (Math.abs(deltaY - singleDirectDis) <= 1)
            direct = 8; //从下往上
        else if (Math.abs(deltaY + singleDirectDis) <= 1)
            direct = 2; //从上往下
        //System.out.println(direct);
        return direct;
    }

    public void spiralSpanningTree() {
        this.linkPathInNode(null, this.tree.rootCell);
        for (Cell cell : this.inners) {
            for (Cell nextCell : cell.nextCells) {
                this.linkPathBetweenNodes(cell, nextCell);
                this.linkPathInNode(cell, nextCell);
            }
        }
    }

}
