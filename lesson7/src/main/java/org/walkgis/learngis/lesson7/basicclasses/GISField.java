package org.walkgis.learngis.lesson7.basicclasses;

import java.lang.reflect.Type;

public class GISField {
    public Type dataType;
    public String fileName;

    public GISField(Type dataType, String fileName) {
        this.dataType = dataType;
        this.fileName = fileName;
    }
}
