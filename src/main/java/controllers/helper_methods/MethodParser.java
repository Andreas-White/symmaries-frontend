package controllers.helper_methods;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodParser {

    public void getMethods(
            List<String> methodNames,
            List<String> methodContext,
            String javaFile) throws FileNotFoundException {

        CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(javaFile));

        VoidVisitor<Void> methodNameVisitor = new MethodNamePrinter();
        methodNameVisitor.visit(cu, null);
        List<String> methods = new ArrayList<>();
        VoidVisitor<List<String>> methodNameCollector = new MethodNameCollector();
        methodNameCollector.visit(cu, methods);
        AtomicInteger counter = new AtomicInteger();
        methods.forEach(n -> {
            if (counter.get() % 2 == 0)
                methodNames.add(n);
            else
                methodContext.add(n);
            counter.getAndIncrement();
        });
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
        }
    }

    private static class MethodNameCollector extends VoidVisitorAdapter<List<String>> {

        @Override
        public void visit(MethodDeclaration md, List<String> collector) {
            super.visit(md, collector);
            collector.add(md.getNameAsString());
            collector.add(md.toString());
        }
    }
}
