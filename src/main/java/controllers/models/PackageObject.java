package controllers.models;

import java.util.List;
import java.util.Objects;

public class PackageObject {

    private String name;
    private boolean isSecure;
    private List<ClassObject> classObjectList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public List<ClassObject> getClassesList() {
        return classObjectList;
    }

    public void setClassesList(List<ClassObject> classObjectList) {
        this.classObjectList = classObjectList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageObject packageObject)) return false;
        return isSecure() == packageObject.isSecure() && getName().equals(packageObject.getName())
                && getClassesList().equals(packageObject.getClassesList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isSecure(), getClassesList());
    }

    public void checkSecurityOfClasses() {
        for (ClassObject c: classObjectList) {
            if (!c.isSecure())
                setSecure(false);
        }
    }
}
