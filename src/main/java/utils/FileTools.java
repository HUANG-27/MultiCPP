package utils;

import entity.ClassicImage;
import entity.Coverage;
import entity.Robot;
import javafx.geometry.Point2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

    public static BufferedImage linearStretchImage(BufferedImage bufferedImage) {
        BufferedImage bufferedImage1 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        WritableRaster raster = bufferedImage.getRaster();
        int maxValue = 0;
        for (int i = 0; i < raster.getWidth(); i++) {
            for (int j = 0; j < raster.getHeight(); j++) {
                maxValue = Math.max(maxValue, raster.getSample(i, j, 0));
            }
        }
        short ratio = (short)(255 / maxValue);
        for (int i = 0; i < raster.getWidth(); i++) {
            for (int j = 0; j < raster.getHeight(); j++) {
                bufferedImage1.setRGB(i,j,raster.getSample(i, j, 0) * ratio);
                raster.setSample(i, j, 0, raster.getSample(i, j, 0) * ratio);
                //System.out.println(raster.getSample(i, j, 0));
            }
        }
        //bufferedImage.setData(raster);
        System.out.println(bufferedImage.getType());
        System.out.println(bufferedImage.getRaster().getSample(1, 1, 0));

        //Graphics graphics = bufferedImage1.getGraphics();
        //graphics.drawImage(bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
        //bufferedImage1.setData(raster);
        System.out.println(bufferedImage1.getRaster().getSample(1, 1, 0));
        return bufferedImage1;
    }

    public static ClassicImage readClassicImage(String imageFilePath) throws IOException {
        //分类图片及其信息
        ClassicImage classicImage = new ClassicImage();
        classicImage.image = ImageIO.read(new File(imageFilePath));
        String imageConfigFilePath = imageFilePath.split("\\.")[0] + ".txt";
        FileReader fileReader = new FileReader(imageConfigFilePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        bufferedReader.readLine();
        classicImage.projection = bufferedReader.readLine();
        bufferedReader.readLine();
        String[] strOffset = bufferedReader.readLine().split(" ");
        classicImage.offset = new Point2D(Double.parseDouble(strOffset[0]), Double.parseDouble(strOffset[1]));
        bufferedReader.readLine();
        classicImage.resolution = Double.parseDouble(bufferedReader.readLine());
        bufferedReader.close();
        fileReader.close();
        return classicImage;
    }

    public static void saveResult(String path, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(path));
    }

    public static List<Coverage> readCoverages(String coverageConfigPath) throws IOException {
        List<Coverage> coverages = new ArrayList<Coverage>();
        FileReader fileReader = new FileReader(coverageConfigPath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        bufferedReader.readLine();
        String strCoverages = bufferedReader.readLine();
        while (strCoverages != null) {
            String[] strCoverageArray = strCoverages.split(" ");
            Coverage coverage = new Coverage(Integer.parseInt(strCoverageArray[0]), strCoverageArray[1],
                    Double.parseDouble(strCoverageArray[2]), Double.parseDouble(strCoverageArray[3]));
            coverages.add(coverage);
            strCoverages = bufferedReader.readLine();
        }
        return coverages;
    }

    public static List<entity.Robot> readRobotInitialConfig(String robotConfigPath) throws IOException{
        List<entity.Robot> robots = new ArrayList<entity.Robot>();
        FileReader fileReader1 = new FileReader(robotConfigPath);
        BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
        bufferedReader1.readLine();
        String strRobots = bufferedReader1.readLine();
        while (strRobots != null) {
            String[] strRobotArray = strRobots.split(" ");
            entity.Robot robot = new Robot(Integer.parseInt(strRobotArray[0]), new Point2D(
                    Double.parseDouble(strRobotArray[1]), Double.parseDouble(strRobotArray[2])));
            robots.add(robot);
            strRobots = bufferedReader1.readLine();
        }
        bufferedReader1.close();
        fileReader1.close();
        return robots;
    }

}
