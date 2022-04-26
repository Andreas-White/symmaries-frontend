package controllers.entities;

import java.util.Objects;

public class MethodObject {

    private String name;
    private boolean isSecure;
    private String content;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodObject methodObject)) return false;
        return isSecure() == methodObject.isSecure() && Objects.equals(getName(), methodObject.getName())
                && Objects.equals(getContent(), methodObject.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isSecure(), getContent());
    }
}
