package entity;

import java.awt.*;

public class Segment {
    public Point[] points;

    public Segment(Point point1, Point point2){
        points = new Point[2];
        points[0] = point1;
        points[1] = point2;
    }

    public boolean isCrossWith(Segment segment){
        if(pointLocOnSegment(segment.points[0]) * pointLocOnSegment(segment.points[1]) == -1
        && segment.pointLocOnSegment(this.points[0]) * segment.pointLocOnSegment(this.points[1]) == -1)
            return true;
        else
            return false;
    }

    private int pointLocOnSegment(Point point){
        double r = (this.points[1].x-this.points[0].x)*(point.y - this.points[0].y)-(this.points[1].y-this.points[0].y)*(point.x - this.points[0].x);
        if(r > 0)
            return 1;
        else if(r < 0)
            return -1;
        else
            return 0;

    }
}
