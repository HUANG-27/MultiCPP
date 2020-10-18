package entity;

import javafx.geometry.Point2D;
import utils.DrawTools;

import java.awt.*;

public class Robot {

    public int id;
    public Point2D initialPosition;
    public Cell initialCell;

    public Color areaColor;
    public Color pathColor;

    //当前占有的区域
    public Area area;

    public Robot(int id, Point2D initialPosition){
        this.id = id;
        this.initialPosition = initialPosition;

        switch (id){
            case 1:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(2, 103, 13);
                break;
            case 2:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(103, 38, 3);
                break;
            case 3:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(103, 9, 68);
                break;
            case 4:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(65, 64, 103);
                break;
            case 5:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(34, 6, 103);
                break;
            case 6:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(4, 62, 103);
                break;
            case 7:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(4, 103, 78);
                break;
            case 8:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(103, 48, 25);
                break;
            case 9:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(87, 103, 8);
                break;
            case 10:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = new Color(103, 20, 31);
                break;
            default:
                this.areaColor = DrawTools.getRandomColorLight();
                this.pathColor = DrawTools.getRandomColorDark();
        }
    }
}
