package org.walkgis.learngis.lesson19.basicclasses;

public enum SelectResult {
    OK,//正常选择状态，选择到一个结果
    EmptySet,//错误选择状态，备选集是空的
    TooFar,//错误选择状态，点击选择时候距离空间对象太远
    UnknownType;//错误选择状态，未知空间对象
}
