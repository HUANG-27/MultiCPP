package entity;

import utils.DrawTools;

import java.awt.*;

public class Coverage {

    public int value;       //像素值
    public String type;     //地物类型描述
    public double range;    //可视范围,直径的二倍，生成树的原因
    public double speed;    //搜索速度
    public Color color;

    public Coverage(int value, String type, double range, double speed) {
        this.value = value;
        this.type = type;
        this.range = range;
        this.speed = speed;
        switch (this.value){
            case 1:
                this.color = new Color(60, 230, 32);
                break;
            case 2:
                this.color = new Color(230, 67, 46);
                break;
            case 3:
                this.color = new Color(175, 230, 103);
                break;
            case 4:
                this.color = new Color(8, 87, 14);
                break;
            case 5:
                this.color = new Color(230, 191, 193);
                break;
            case 6:
                this.color = new Color(116, 116, 116);
                break;
            default:
                this.color = DrawTools.getRandomColorLight();
        }

    }
}
