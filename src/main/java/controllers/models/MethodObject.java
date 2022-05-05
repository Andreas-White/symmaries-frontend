package controllers.models;

import java.util.Objects;

public class MethodObject {

    private String name;
    private boolean isSecure;
    private String secsumContent;
    private String javaFileContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public String getSecsumContent() {
        return secsumContent;
    }

    public void setSecsumContent(String secsumContent) {
        this.secsumContent = secsumContent;
    }

    public String getJavaFileContent() {
        return javaFileContent;
    }

    public void setJavaFileContent(String javaFileContent) {
        this.javaFileContent = javaFileContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodObject that)) return false;
        return isSecure() == that.isSecure() && Objects.equals(getName(),
                that.getName()) && Objects.equals(getSecsumContent(),
                that.getSecsumContent()) && Objects.equals(getJavaFileContent(),
                that.getJavaFileContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isSecure(), getSecsumContent(), getJavaFileContent());
    }
}
