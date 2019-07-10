import java.nio.charset.Charset;

/**
 * @author Kenny
 * created on 2019/5/21
 */
public class A {

    public static void main(String[] args) {
        byte[] bytes = "a".getBytes(Charset.forName("utf-8"));
        System.out.println(bytes.length);
    }
}
