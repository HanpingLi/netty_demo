package cn.haniel.netty.c1;

import java.util.function.Consumer;

/**
 * @author hanping
 * @date 2023-12-28
 */
public class Test {

    public static void main(String[] args) {
        System.out.println(23232);
        method1();
        System.out.println(34444);
    }

    public static void method1() {
        System.out.println(1222);
        method2("xxx");
        System.out.println(3333);
    }

    public static void method2(String s) {
        int a = 12;
        System.out.println(1111);

        new Thread(() -> System.out.println("")).start();
    }

}
