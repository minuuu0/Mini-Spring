package org.example.minispring.scanner;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassPathScanner {

    public Set<Class<?>> scan(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();

        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                return classes;
            }

            File directory = new File(resource.getFile());
            if (!directory.exists()) {
                return classes;
            }

            findClasses(directory, basePackage, classes);
        } catch (Exception e) {

        }

        return classes;
    }

    private void findClasses(File directory, String packageName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {

                }
            }
        }
    }
}
