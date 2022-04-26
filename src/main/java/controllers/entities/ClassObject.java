package controllers.entities;

import java.util.List;
import java.util.Objects;

public class ClassObject {

    private String name;
    private boolean isSecure;
    private List<MethodObject> methodObjectList;
    private String content;

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

    public List<MethodObject> getMethodsList() {
        return methodObjectList;
    }

    public void setMethodsList(List<MethodObject> methodObjectList) {
        this.methodObjectList = methodObjectList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassObject classObject)) return false;
        return isSecure() == classObject.isSecure() && Objects.equals(getName(), classObject.getName())
                && Objects.equals(getMethodsList(), classObject.getMethodsList()) && Objects.equals(getContent(), classObject.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isSecure(), getMethodsList(), getContent());
    }

    public void checkSecurityOfMethods() {
        for (MethodObject m: methodObjectList) {
            if (!m.isSecure())
                setSecure(false);
        }
    }
}
