package controllers.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassObjectDTO {

    private String className;
    private List<String> methodNames;
    private List<String> methodContext;

    public ClassObjectDTO() {
        this.methodNames = new ArrayList<>();
        this.methodContext = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(List<String> methodNames) {
        this.methodNames = methodNames;
    }

    public List<String> getMethodContext() {
        return methodContext;
    }

    public void setMethodContext(List<String> methodContext) {
        this.methodContext = methodContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassObjectDTO that)) return false;
        return Objects.equals(getClassName(),
                that.getClassName()) && Objects.equals(getMethodNames(),
                that.getMethodNames()) && Objects.equals(getMethodContext(),
                that.getMethodContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getMethodNames(), getMethodContext());
    }
}
