package org.walkgis.learngis.lesson12.basicclasses;


import javafx.scene.paint.Color;

public class GISConst {
    public static double minScreenDistance = 5;

    //点的颜色和半径
    public static Color pointColor = Color.RED;
    public static int pointSize = 3;
    //线的颜色和宽度
    public static Color lineColor = Color.BLUE;
    public static int lineWidth = 1;
    //面的边框颜色，填充颜色及边框宽度
    public static Color polygonBoundaryColor = Color.BLACK;
    public static Color polygonFillColor = Color.GRAY;
    public static int polygonBoundaryWidth = 1;
    //被选中的点的颜色
    public static Color selectedPointColor = Color.AQUA;
    //被选中的线颜色
    public static Color selectedLineColor = Color.AQUA;
    //被选中的面的线颜色
    public static Color selectedPolygonBoundaryColor = Color.AQUA;
}
