package utils;

import entity.*;
import entity.Robot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawTools {

    public static BufferedImage drawLandCoverType(Algorithm algorithm) {
        List<Layer> layers = algorithm.pyramid.layers;
        int w = layers.get(0).cellSidePixelCount * layers.get(0).cells.length;
        int h = layers.get(0).cellSidePixelCount * layers.get(0).cells[0].length;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        for (Layer layer : layers) {
            for (int i = 0; i < layer.cells.length; i++) {
                for (int j = 0; j < layer.cells[0].length; j++) {
                    if (layer.cells[i][j].coverage == null)
                        continue;
                    graphics.setColor(layer.cells[i][j].coverage.color);
                    graphics.fillRect(
                            layer.cellSidePixelCount * i, layer.cellSidePixelCount * j,
                            layer.cellSidePixelCount, layer.cellSidePixelCount);
                }
            }

            graphics.setColor(new Color(255, 255, 255));
            for (int i = 0; i < layer.cells.length; i++) {
                for (int j = 0; j < layer.cells[0].length; j++) {
                    if (layer.cells[i][j].coverage == null)
                        continue;
                    graphics.drawRect(
                            layer.cellSidePixelCount * i, layer.cellSidePixelCount * j,
                            layer.cellSidePixelCount, layer.cellSidePixelCount);
                }
            }
        }

        return image;
    }

    public static BufferedImage drawCellAndItsNeighbors(Algorithm algorithm, Cell cell) {
        BufferedImage image = drawLandCoverType(algorithm);
        Graphics graphics = image.getGraphics();
        graphics.setColor(new Color(255, 0, 0));
        int cellCoveragePixelCount = algorithm.pyramid.layers.get(cell.index.layerIndex).cellSidePixelCount;
        graphics.drawRect(cell.index.xIndex * cellCoveragePixelCount,
                cell.index.yIndex * cellCoveragePixelCount, cellCoveragePixelCount, cellCoveragePixelCount);
        List<Cell> cells = cell.neighbors;
        for (Cell cell1 : cells) {
            cellCoveragePixelCount = algorithm.pyramid.layers.get(cell1.index.layerIndex).cellSidePixelCount;
            graphics.drawRect(cell1.index.xIndex * cellCoveragePixelCount,
                    cell1.index.yIndex * cellCoveragePixelCount, cellCoveragePixelCount, cellCoveragePixelCount);
        }
        return image;
    }

    public static BufferedImage drawTempAssignment(Algorithm algorithm) {

        List<Robot> robots = algorithm.robots;
        BufferedImage image = drawLandCoverType(algorithm);
        Graphics graphics = image.getGraphics();

        //分配到的区域
        for (Robot robot : robots) {
            //区域填充
            graphics.setColor(robot.areaColor);
            for (Cell cell : robot.area.inners) {
                graphics.setColor(cell.robot.areaColor);
                graphics.fillRect(cell.index.xIndex * cell.sidePixelCount, cell.index.yIndex * cell.sidePixelCount,
                        cell.sidePixelCount, cell.sidePixelCount);
            }
            //区域边界
            graphics.setColor(new Color(255, 255, 255));
            for (Cell cell : robot.area.inners) {
                graphics.drawRect(cell.index.xIndex * cell.sidePixelCount, cell.index.yIndex * cell.sidePixelCount,
                        cell.sidePixelCount, cell.sidePixelCount);
            }
        }

        //机器人的起点
        for (Robot robot : robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        //区域边界
//        for (Robot robot : robots) {
//            graphics.setColor(new Color(0,0,0));
//            for (Cell cell : robot.area.neighbors) {
//                graphics.drawRect(cell.index.xIndex * cell.sidePixelCount, cell.index.yIndex * cell.sidePixelCount,
//                        cell.sidePixelCount, cell.sidePixelCount);
//            }
//        }

        //区域的中心
//        for (Robot robot : robots) {
//            graphics.setColor(new Color(0, 0, 0));
//            graphics.fillRect(robot.area.center.x, robot.area.center.y, 10, 10);
//        }
//        graphics.fillRect(algorithm.unAssignedCellCenter.x, algorithm.unAssignedCellCenter.y, 10, 10);

        return image;
    }

    public static BufferedImage drawAssignmentResultAndSpanningTree(Algorithm algorithm) {

        List<Robot> robots = algorithm.robots;
        BufferedImage image = drawTempAssignment(algorithm);
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        for (Robot robot : robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        graphics.setColor(new Color(255, 0, 0));
        graphics.setStroke(new BasicStroke(2));
        //画树
        for (Robot robot : robots) {
            drawSpanningTree(robot.area.tree.rootCell, graphics);
        }

        return image;
    }

    public static BufferedImage drawLandCoverTypeAndSpanningTree(Algorithm algorithm) {

        List<Robot> robots = algorithm.robots;
        BufferedImage image = drawLandCoverType(algorithm);
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        for (Robot robot : robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        //画树
        graphics.setColor(new Color(255, 0, 0));
        graphics.setStroke(new BasicStroke(2));
        for (Robot robot : robots) {
            drawSpanningTree(robot.area.tree.rootCell, graphics);
        }

        return image;
    }
    private static void drawSpanningTree(Cell cell, Graphics graphics) {
        if (cell.nextCells.size() > 0) {
            for (Cell cell1 : cell.nextCells) {
                graphics.drawLine(cell.index.xIndex * cell.sidePixelCount + cell.sidePixelCount / 2,
                        cell.index.yIndex * cell.sidePixelCount + cell.sidePixelCount / 2,
                        cell1.index.xIndex * cell1.sidePixelCount + cell1.sidePixelCount / 2,
                        cell1.index.yIndex * cell1.sidePixelCount + cell1.sidePixelCount / 2);
                drawSpanningTree(cell1, graphics);
            }
        }
    }

    public static BufferedImage drawAssignmentResultAndCoveragePath(Algorithm algorithm) {

        List<Robot> robots = algorithm.robots;
        BufferedImage image = drawAssignmentResultAndSpanningTree(algorithm);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setStroke(new BasicStroke(3));

        //画路
        for (Robot robot : robots) {
            graphics.setColor(robot.pathColor);
            for (Segment segment : robot.area.path) {
                graphics.drawLine(segment.points[0].x, segment.points[0].y, segment.points[1].x, segment.points[1].y);
            }
        }

        //机器人的起点
        for (Robot robot : robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        return image;
    }

    public static BufferedImage drawCoveragePath(Algorithm algorithm) {

        List<Layer> layers = algorithm.pyramid.layers;
        int w = layers.get(0).cellSidePixelCount * layers.get(0).cells.length;
        int h = layers.get(0).cellSidePixelCount * layers.get(0).cells[0].length;

        BufferedImage image = new BufferedImage(w, h, Transparency.TRANSLUCENT);

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setStroke(new BasicStroke(3));

        for (Layer layer : layers) {
            for (int i = 0; i < layer.cells.length; i++) {
                for (int j = 0; j < layer.cells[0].length; j++) {
                    if (layer.cells[i][j].coverage == null)
                        continue;
                    graphics.setColor(Color.white);
                    graphics.fillRect(
                            layer.cellSidePixelCount * i, layer.cellSidePixelCount * j,
                            layer.cellSidePixelCount, layer.cellSidePixelCount);
                }
            }

            graphics.setColor(new Color(255, 255, 255));
            for (int i = 0; i < layer.cells.length; i++) {
                for (int j = 0; j < layer.cells[0].length; j++) {
                    if (layer.cells[i][j].coverage == null)
                        continue;
                    graphics.drawRect(
                            layer.cellSidePixelCount * i, layer.cellSidePixelCount * j,
                            layer.cellSidePixelCount, layer.cellSidePixelCount);
                }
            }
        }

        //画路
        for (Robot robot : algorithm.robots) {
            graphics.setColor(robot.pathColor);
            for (Segment segment : robot.area.path) {
                graphics.drawLine(segment.points[0].x, segment.points[0].y, segment.points[1].x, segment.points[1].y);
            }
        }

        //机器人的起点
        for (Robot robot : algorithm.robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        return image;
    }

    public static BufferedImage drawLandCoverTypeCoveragePath(Algorithm algorithm) {

        List<Robot> robots = algorithm.robots;
        BufferedImage image = drawLandCoverType(algorithm);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setStroke(new BasicStroke(3));

        //画路
        for (Robot robot : robots) {
            graphics.setColor(robot.pathColor);
            for (Segment segment : robot.area.path) {
                graphics.drawLine(segment.points[0].x, segment.points[0].y, segment.points[1].x, segment.points[1].y);
            }
        }

        //机器人的起点
        for (Robot robot : robots) {
            //起点
            graphics.setColor(new Color(255, 0, 0));
            int x = robot.initialCell.index.xIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            int y = robot.initialCell.index.yIndex * robot.initialCell.sidePixelCount + robot.initialCell.sidePixelCount / 2;
            graphics.fillRect(x - 5, y - 5, 10, 10);
        }

        return image;
    }

    public static Color getRandomColorLight() {
        Random random = new Random();
        return new Color(random.nextInt(192) + 64,
                random.nextInt(192) + 64,
                random.nextInt(192) + 64);
    }

    public static Color getRandomColorDark() {
        Random random = new Random();
        return new Color(random.nextInt(64) + 64,
                random.nextInt(64) + 64,
                random.nextInt(64) + 64);
    }

}
