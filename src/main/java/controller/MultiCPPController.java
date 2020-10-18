package controller;

import entity.Cell;
import entity.Robot;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.input.MouseEvent;
import utils.Algorithm;
import utils.DrawTools;
import utils.FileTools;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class MultiCPPController {

    public Algorithm algorithm = new Algorithm();

    public List<BufferedImage> pyramidImages;
    public List<BufferedImage> valuedPyramidImages;

    public BufferedImage valuedPyramidImage;
    public BufferedImage pyramidImage;


    public TextField textField;
    public Button btnOpenImage;
    public Button btnBuildPyramid;

    public TextField textFieldLayer;
    public TextField textFieldX;
    public TextField textFieldY;
    public Button btnFindNeighbors;

    public TextField textFieldCycleNum;
    public Label lblTotalCycleNum;

    public ImageView imageView;

    public long executeTime = 0;

    public void treeViewMouseClick() {
    }

    public void onOpenImageAction() throws IOException {
        long startTime = System.currentTimeMillis();
        imageView.setPreserveRatio(true);
        //Image image = new Image("https://cn.bing.com/th?id=OHR.AiringGrievances_ZH-CN5830208720_UHD.jpg&pid=hp&w=3840&h=2160&rs=1&c=4&r=0");
        String filePath = textField.getText();
        algorithm.readConfigurationAndInit(filePath);
        imageView.setImage(SwingFXUtils.toFXImage(algorithm.classicImage.image, null));

        lblTotalCycleNum.setText("0");
        executeTime += (System.currentTimeMillis() - startTime);
    }

    public void onBuildPyramidAction() throws IOException {
        long startTime = System.currentTimeMillis();
        algorithm.Init();

        valuedPyramidImage = DrawTools.drawLandCoverType(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(valuedPyramidImage, null));

        int cellCount = algorithm.pyramid.getCellCount();
        textFieldCycleNum.setText(String.valueOf(cellCount));
        FileTools.saveResult("D:/TestData/pyramid.png", valuedPyramidImage);
        executeTime += (System.currentTimeMillis() - startTime);
    }

    public void onFindNeighborsAction() {
        int layerIndex = Integer.parseInt(textFieldLayer.getText());
        int xIndex = Integer.parseInt(textFieldX.getText());
        int yIndex = Integer.parseInt(textFieldY.getText());

        Cell cell = algorithm.pyramid.layers.get(layerIndex).cells[xIndex][yIndex];
        if(cell.coverage != null){
            BufferedImage image = DrawTools.drawCellAndItsNeighbors(algorithm, cell);
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
        }
    }

    public void OnAssignAreaAction() throws IOException {
        long startTime = System.currentTimeMillis();
        int cycleNum = Integer.parseInt(textFieldCycleNum.getText());
        algorithm.assignArea(cycleNum);
        lblTotalCycleNum.setText(String.valueOf(Integer.parseInt(lblTotalCycleNum.getText()) + cycleNum));
        System.out.println("Cycle " + lblTotalCycleNum.getText() + " Finished!");
        BufferedImage image = DrawTools.drawTempAssignment(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        FileTools.saveResult("D:/TestData/assignment" + lblTotalCycleNum.getText() + ".png", image);
        executeTime += (System.currentTimeMillis() - startTime);
    }

    public void onNextCycleAction() {
        long startTime = System.currentTimeMillis();
        int cycleNum = 1;
        algorithm.assignArea(cycleNum);
        lblTotalCycleNum.setText(String.valueOf(Integer.parseInt(lblTotalCycleNum.getText()) + cycleNum));
        System.out.println("Cycle " + lblTotalCycleNum.getText() + " Finished!");
        BufferedImage image = DrawTools.drawTempAssignment(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        executeTime += (System.currentTimeMillis() - startTime);
    }

    public void onBuildSTreeAction() throws IOException {
        long startTime = System.currentTimeMillis();
        algorithm.buildSpanningTrees();
        BufferedImage image = DrawTools.drawAssignmentResultAndSpanningTree(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        //FileTools.saveResult("D:/TestData/result.png", image);
        System.out.println("----- coverage time -----");
        for(Robot robot : algorithm.robots){
            System.out.println(robot.id + " " + robot.area.coverageTime);
        }
        executeTime += (System.currentTimeMillis() - startTime);
    }

    public void onFindPathAction() throws IOException {
        long startTime = System.currentTimeMillis();
        algorithm.spiralSpanningTrees();
        BufferedImage image1 = DrawTools.drawAssignmentResultAndCoveragePath(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image1, null));
        FileTools.saveResult("D:/TestData/result1.png", image1);
        BufferedImage image2 = DrawTools.drawCoveragePath(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image2, null));
        FileTools.saveResult("D:/TestData/result2.png", image2);
        BufferedImage image3 = DrawTools.drawLandCoverTypeCoveragePath(algorithm);
        imageView.setImage(SwingFXUtils.toFXImage(image3, null));
        FileTools.saveResult("D:/TestData/result3.png", image3);
        executeTime += (System.currentTimeMillis() - startTime);
        System.out.println("----- execute time -----");
        System.out.println("MLCT-MCPP running time: " + executeTime / 1000.0);
    }

}
