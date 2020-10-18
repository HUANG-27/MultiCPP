package utils;

import entity.*;
import entity.Robot;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Algorithm {

    //一种机器人
    //不同地物覆盖类型
    //考虑机器人覆盖能力和行进速度

    //机器人
    public ClassicImage classicImage;
    public List<Robot> robots;
    public List<Coverage> coverages;
    public Pyramid pyramid;

    public Point unAssignedCellCenter = new Point();

    public void readConfigurationAndInit(String imageFilePath) throws IOException {

        //分类图片及其信息
        classicImage = FileTools.readClassicImage(imageFilePath);

        //覆盖信息
        coverages = FileTools.readCoverages(imageFilePath.split("\\.")[0] + "_coverages.txt");

        //机器人信息
        robots = FileTools.readRobotInitialConfig(imageFilePath.split("\\.")[0] + "_robots.txt");

    }

    public void Init() {

        //建立树结构
        pyramid = new Pyramid(classicImage, coverages);
        pyramid.buildPyramid();
        pyramid.setIndexes();
        pyramid.setCoverages();
        pyramid.setCellNeighbors();

        //初始化每个机器人的覆盖区域
        for (Robot robot : robots) {
            //初始化每个机器人的覆盖区域
            robot.initialCell = pyramid.getCellInPyramid(robot.initialPosition);
            robot.area = new Area(robot);
        }
    }

    public void assignArea(int cycleTimes) {

        if (pyramid == null)
            return;
        if (robots == null)
            return;

        //循环次数
        int i = 0;
        while (i++ < cycleTimes) {
            //（1）确定选点的机器人
            Robot tempRobot = getTempRobot();
            //（2）选出合适的点
            List<Cell> cells = orderNeighborCells(tempRobot);
            //（3）把点加入到area，更新neighbors
            for (Cell cell : cells) {
                //未分配点直接加入
                if (cell.robot == null) {
                    tempRobot.area.andCell(cell);
                    break;
                }
                //已分配点
                else {
                    if (cell.isDeletedRobot(tempRobot))
                        continue;
                    if (cell.robot.area.isAbleToDeleteCell(cell)) {
                        //先删除
                        cell.robot.area.deleteCell(cell);
                        //再加入
                        tempRobot.area.andCell(cell);
                        break;
                    }
                }
            }
        }
    }

    private Robot getTempRobot() {
        //根据时间的长短选择时间最短的机器人
        Robot tempRobot = robots.get(0);
        for (Robot robot : robots) {
//            if (robot.area.inners.size() < tempRobot.area.inners.size())
//                tempRobot = robot;
            if (robot.area.coverageTime < tempRobot.area.coverageTime)
                tempRobot = robot;
        }
        return tempRobot;
    }

    private List<Cell> orderNeighborCells(Robot robot) {

        //排好序的邻域
        List<Cell> orderNeighborCells = new ArrayList<Cell>();
        //未分配的邻域
        List<Cell> unAssignedCells = new ArrayList<Cell>();
        //已分配的邻域
        List<Cell> assignedCells = new ArrayList<Cell>();
        for (Cell cell : robot.area.neighbors) {
            if (cell.robot == null)
                unAssignedCells.add(cell);
            else
                assignedCells.add(cell);
        }

        //到区域中心的距离
        for (Cell unAssignedCell : unAssignedCells) {
            double deltaX = robot.area.center.x - unAssignedCell.getCenter().x;
            double deltaY = robot.area.center.y - unAssignedCell.getCenter().y;
            unAssignedCell.distanceToArea = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }

        //计算到除Vs,i之外其他所有的Vs,p的中心的平均距离
        for (Cell unAssignedCell : unAssignedCells) {
            unAssignedCell.averageDistanceToOtherAreas = 0;
            for (Robot tempRobot : robots) {
                if (tempRobot.id != robot.id) {
                    double deltaX = tempRobot.area.center.x - unAssignedCell.getCenter().x;
                    double deltaY = tempRobot.area.center.y - unAssignedCell.getCenter().y;
                    unAssignedCell.averageDistanceToOtherAreas += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                }
            }
            if (robots.size() > 1)
                unAssignedCell.averageDistanceToOtherAreas /= (robots.size() - 1);
        }

        //按照距离排序
        for (int i = 0; i < unAssignedCells.size(); i++) {
            for (int j = i; j < unAssignedCells.size(); j++) {
                if ((2 * unAssignedCells.get(j).distanceToArea - unAssignedCells.get(j).averageDistanceToOtherAreas)
                        < (2 * unAssignedCells.get(i).distanceToArea - unAssignedCells.get(i).averageDistanceToOtherAreas)) {
                    Cell cell = unAssignedCells.get(j);
                    unAssignedCells.set(j, unAssignedCells.get(i));
                    unAssignedCells.set(i, cell);
                }
            }
        }

        orderNeighborCells.addAll(unAssignedCells);
        //如果存在未分配的点直接选择即可
        if (orderNeighborCells.size() > 0)
            return orderNeighborCells;

        //到当前机器人覆盖区域中心的距离
//        for (Cell assignedCell : assignedCells) {
//            double deltaX = robot.area.center.x - assignedCell.getCenter().x;
//            double deltaY = robot.area.center.y - assignedCell.getCenter().y;
////            double deltaX = robot.initialCell.getCenter().x - assignedCell.getCenter().x;
////            double deltaY = robot.initialCell.getCenter().y - assignedCell.getCenter().y;
//            assignedCell.distanceToArea = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//        }
//
//        //计算到除Vs,i之外其他所有的Vs,p的中心的平均距离
//        for (Cell assignedCell : assignedCells) {
//            assignedCell.averageDistanceToOtherAreas = 0;
//            for (Robot tempRobot : robots) {
//                if (tempRobot.id != robot.id) {
//                    double deltaX = tempRobot.area.center.x - assignedCell.getCenter().x;
//                    double deltaY = tempRobot.area.center.y - assignedCell.getCenter().y;
////                    double deltaX = tempRobot.initialCell.getCenter().x - assignedCell.getCenter().x;
////                    double deltaY = tempRobot.initialCell.getCenter().y - assignedCell.getCenter().y;
//                    assignedCell.averageDistanceToOtherAreas += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//                }
//            }
//            if (robots.size() > 1)
//                assignedCell.averageDistanceToOtherAreas /= (robots.size() - 1);
//        }
//
//        //按照距离排序
//        for (int i = 0; i < assignedCells.size(); i++) {
//            for (int j = i; j < assignedCells.size(); j++) {
//                if ((assignedCells.get(j).distanceToArea - assignedCells.get(j).averageDistanceToOtherAreas)
//                        < (assignedCells.get(i).distanceToArea - assignedCells.get(i).averageDistanceToOtherAreas)) {
//                    Cell cell = assignedCells.get(j);
//                    assignedCells.set(j, assignedCells.get(i));
//                    assignedCells.set(i, cell);
//                }
//            }
//        }
//
//        orderNeighborCells.addAll(assignedCells);


        //到未分配区域中心的距离
//        int w = classicImage.image.getRaster().getWidth();
//        int h = classicImage.image.getRaster().getHeight();
//        int totalPixelCount = w * h;
//        int totalCenterX = w * totalPixelCount / 2;
//        int totalCenterY = h * totalPixelCount / 2;
//        for (Robot roboti : robots) {
//            int totalPixelCounti = 0;
//            int totalCenterXi = 0;
//            int totalCenterYi = 0;
//            for (Cell cell : roboti.area.inners) {
//                int pixelCount = cell.sidePixelCount * cell.sidePixelCount;
//                totalCenterXi += cell.getCenter().x * pixelCount;
//                totalCenterYi += cell.getCenter().y * pixelCount;
//                totalPixelCounti += pixelCount;
//            }
//            totalCenterX -= totalCenterXi;
//            totalCenterY -= totalCenterYi;
//            totalPixelCount -= totalPixelCounti;
//        }
//        if (totalPixelCount <= 0)
//            totalPixelCount = 1;
//        //未分配区域的中心点
//        unAssignedCellCenter = new Point(totalCenterX / totalPixelCount,
//                totalCenterY / totalPixelCount);

        int totalPixelCount = 0;
        int totalCenterX = 0;
        int totalCenterY = 0;
        for (Layer layer : this.pyramid.layers) {
            for(Cell[] cells : layer.cells){
                for(Cell cell : cells){
                    if(cell.coverage != null && cell.robot == null){
                        int pixelCount = cell.sidePixelCount * cell.sidePixelCount;
                        totalCenterX += cell.getCenter().x * pixelCount;
                        totalCenterY += cell.getCenter().y * pixelCount;
                        totalPixelCount += pixelCount;
                    }
                }
            }
        }

        if(totalPixelCount == 0)
            return orderNeighborCells;

        unAssignedCellCenter = new Point(totalCenterX / totalPixelCount,
                totalCenterY / totalPixelCount);

        //靠近未分配区域一侧的邻域点
        List<Cell> directToUnAssignedAreaAssignedCells = new ArrayList<Cell>();
        for (Cell assignedCell : assignedCells) {
            //当前邻域cell指向未分配区域中心的向量
            Point directVector = new Point(unAssignedCellCenter.x - assignedCell.getCenter().x,
                    unAssignedCellCenter.y - assignedCell.getCenter().y);
            //x方向
            if (directVector.x > 0) {
                // direction1 = 4;
                for (Cell neighborCell : assignedCell.neighbors) {
                    if (neighborCell.robot != null && neighborCell.robot.id == robot.id) {
                        if (robot.area.getDirect(assignedCell, neighborCell) == 1
                                && !directToUnAssignedAreaAssignedCells.contains(assignedCell))
                            directToUnAssignedAreaAssignedCells.add(assignedCell);
                        break;
                    }
                }
            } else if (directVector.x < 0) {
                // direction1 = 1;
                for (Cell neighborCell : assignedCell.neighbors) {
                    if (neighborCell.robot != null && neighborCell.robot.id == robot.id
                            && robot.area.getDirect(assignedCell, neighborCell) == 4) {
                        if (!directToUnAssignedAreaAssignedCells.contains(assignedCell))
                            directToUnAssignedAreaAssignedCells.add(assignedCell);
                        break;
                    }
                }
            }
            // y方向
            if (directVector.y > 0) {
                // direction2 = 2;
                //System.out.println(assignedCell.robot.id);
                for (Cell neighborCell : assignedCell.neighbors) {
                    if (neighborCell.robot != null && neighborCell.robot.id == robot.id
                            && robot.area.getDirect(assignedCell, neighborCell) == 2) {
                        if (!directToUnAssignedAreaAssignedCells.contains(assignedCell))
                            directToUnAssignedAreaAssignedCells.add(assignedCell);
                        break;
                    }
                }
            } else if (directVector.y < 0) {
                // direction2 = 8;
                for (Cell neighborCell : assignedCell.neighbors) {
                    if (neighborCell.robot != null && neighborCell.robot.id == robot.id
                            && robot.area.getDirect(assignedCell, neighborCell) == 8) {
                        if (!directToUnAssignedAreaAssignedCells.contains(assignedCell))
                            directToUnAssignedAreaAssignedCells.add(assignedCell);
                        break;
                    }
                }
            }
        }

        for (Cell assignedCell : directToUnAssignedAreaAssignedCells) {
            double deltaX = robot.area.center.x - assignedCell.getCenter().x;
            double deltaY = robot.area.center.y - assignedCell.getCenter().y;
            assignedCell.distanceToArea = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }

        //计算到除Vs,i之外其他所有的Vs,p的中心的平均距离
        for (Cell assignedCell : directToUnAssignedAreaAssignedCells) {
            assignedCell.averageDistanceToOtherAreas = 0;
            for (Robot tempRobot : robots) {
                if (tempRobot.id != robot.id) {
                    double deltaX = tempRobot.area.center.x - assignedCell.getCenter().x;
                    double deltaY = tempRobot.area.center.y - assignedCell.getCenter().y;
                    assignedCell.averageDistanceToOtherAreas += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                }
            }
            if (robots.size() > 1)
                assignedCell.averageDistanceToOtherAreas /= (robots.size() - 1);
        }

        //按照距离排序
        for (int i = 0; i < directToUnAssignedAreaAssignedCells.size(); i++) {
            for (int j = i; j < directToUnAssignedAreaAssignedCells.size(); j++) {
                if ((directToUnAssignedAreaAssignedCells.get(j).distanceToArea)
                        < (directToUnAssignedAreaAssignedCells.get(i).distanceToArea)) {
                    Cell cell = directToUnAssignedAreaAssignedCells.get(j);
                    directToUnAssignedAreaAssignedCells.set(j, directToUnAssignedAreaAssignedCells.get(i));
                    directToUnAssignedAreaAssignedCells.set(i, cell);
                }
            }
        }

//        for (Cell assignedCell : assignedCells) {
//            double deltaX = robot.area.center.x - unAssignedCellCenter.x;
//            double deltaY = robot.area.center.y - unAssignedCellCenter.y;
//            assignedCell.distanceToUnassignedCellCenter = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//        }

        //按照到未分配区域中心的距离排序
//        for (int i = 0; i < assignedCells.size(); i++) {
//            for (int j = i; j < assignedCells.size(); j++) {
//                if (assignedCells.get(j).distanceToUnassignedCellCenter
//                        < assignedCells.get(i).distanceToUnassignedCellCenter) {
//                    Cell cell = assignedCells.get(j);
//                    assignedCells.set(j, assignedCells.get(i));
//                    assignedCells.set(i, cell);
//                }
//            }
//        }

        //按照邻域个数排序
//        int[] inAreaNeighborCount = new int[correctDirectAssignedCells.size()];
//        for (int i = 0; i < correctDirectAssignedCells.size(); i++) {
//            for (Cell cell : correctDirectAssignedCells.get(i).neighbors) {
//                if (cell.robot != null && cell.robot.id == robot.id)
//                    inAreaNeighborCount[i]++;
//            }
//        }
//        for (int i = 0; i < correctDirectAssignedCells.size(); i++) {
//            for (int j = i; j < correctDirectAssignedCells.size(); j++) {
//                if (inAreaNeighborCount[j] > inAreaNeighborCount[i]) {
//                    Cell cell = correctDirectAssignedCells.get(j);
//                    correctDirectAssignedCells.set(j, correctDirectAssignedCells.get(i));
//                    correctDirectAssignedCells.set(i, cell);
//                }
//            }
//        }

        orderNeighborCells.addAll(directToUnAssignedAreaAssignedCells);

        return orderNeighborCells;
    }

    public void buildSpanningTrees() {
        for (Robot robot : robots) {
            robot.area.buildSpanningTree();
        }
    }

    public void spiralSpanningTrees() {
        for (Robot robot : robots) {
            robot.area.spiralSpanningTree();
        }
    }

}
