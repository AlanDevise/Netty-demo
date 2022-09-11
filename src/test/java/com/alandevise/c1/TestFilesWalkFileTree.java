package com.alandevise.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Filename: TestFilesWalkFileTree.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月11日 14:44
 */

public class TestFilesWalkFileTree {
    public static void main(String[] args) throws IOException {
    }

    private static void CopyFolder() throws IOException {
        String source = "/Users/alanzhang/Desktop/Jupyter Notebook";
        String target = "/Users/alanzhang/Desktop/Jupyter Notebook_COPY";

        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String targetName = path.toString().replace(source, target);
                // 是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                } else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }

    // [WARNING] 极为危险的操作，逐级删除路径下所有的文件及文件夹，不可恢复
    private static void DeleteVariousFiles() throws IOException {
        Files.walkFileTree(Paths.get("/Users/alanzhang/Desktop/Jupyter Notebook_COPY"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                System.out.println(file + " 已删除");
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                System.out.println(dir + " 已删除");
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static void m2() throws IOException {
        AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar")) {
                    System.out.println(file);
                    fileCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("Jars count: " + fileCount);
    }

    private static void m1() throws IOException {
        // 匿名类内部不能使用局部变脸进行累加计数，所以这里使用"累加器"
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("====> " + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("dir count " + dirCount);
        System.out.println("file count " + fileCount);
    }
}