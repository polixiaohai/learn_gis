package org.walkgis.learngis.lesson13.basicclasses;

public class GISField {
    public Class dataType;
    public String fileName;

    public GISField(Class dataType, String fileName) {
        this.dataType = dataType;
        this.fileName = fileName;
    }
}