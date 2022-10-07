package plus.kat.kernel;

import org.junit.jupiter.api.Test;
import plus.kat.chain.Alias;
import plus.kat.chain.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static java.nio.charset.StandardCharsets.*;

public class ChainTest {

    static class Chalk
        extends Chain {
    }

    @Test
    public void test_is() {
        assertTrue(new Value("$").is('$'));
        assertTrue(new Value("$").is((byte) '$'));
        assertTrue(new Value("kat.plus").is("kat.plus"));
        assertTrue(new Value("😀").is("😀"));
        assertTrue(new Value("陆").is('陆'));
        assertTrue(new Value("陆之岇").is("陆之岇"));
        assertTrue(new Value("😀陆之岇😀").is("😀陆之岇😀"));
        assertTrue(new Value("😀陆之岇😀").is(2, '陆'));
        assertTrue(new Value("😀陆之岇😀+").is(7, '+'));

        assertTrue(new Value("+😀+").is(1, '\uD83D'));
        assertTrue(new Value("+😀+").is(2, '\uDE00'));
        assertTrue(new Value("😀陆之岇😀").is(0, '\uD83D'));
        assertTrue(new Value("😀陆之岇😀").is(1, '\uDE00'));
        assertTrue(new Value("😀陆之岇😀").is(2, '陆'));
        assertTrue(new Value("😀陆之岇😀").is(4, '岇'));
        assertTrue(new Value("😀陆之岇😀").is(5, '\uD83D'));
        assertTrue(new Value("😀陆之岇😀").is(6, '\uDE00'));

        assertFalse(new Value("陆之岇").is(null));
        assertFalse(new Value("陆之岇").is("陆之岇+"));
        assertFalse(new Value("陆之岇+").is("陆之岇"));
        assertFalse(new Value("+陆之岇+").is("陆之岇+"));
        assertFalse(new Value("$$").is('$'));
        assertFalse(new Value("//kat.plus").is("kat.plus"));
        assertFalse(new Value("😀陆之岇😀").is("😀陆之岇😀😀陆之岇😀"));
    }

    @Test
    public void test_get() {
        Alias c = new Alias("kat");
        byte def = '$';
        assertEquals((byte) 'k', c.get(0, def));
        assertEquals((byte) 'a', c.get(1, def));
        assertEquals((byte) 't', c.get(2, def));
        assertEquals((byte) '$', c.get(3, def));
        assertEquals((byte) 't', c.get(-1, def));
        assertEquals((byte) 'a', c.get(-2, def));
        assertEquals((byte) 'k', c.get(-3, def));
        assertEquals((byte) '$', c.get(-4, def));

        assertEquals((byte) 'k', c.get(0));
        assertEquals((byte) 'a', c.get(1));
        assertEquals((byte) 't', c.get(2));
        assertEquals((byte) 't', c.get(-1));
        assertEquals((byte) 'a', c.get(-2));
        assertEquals((byte) 'k', c.get(-3));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.get(3));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.get(-4));
    }

    @Test
    public void test_byteAt() {
        Alias c = new Alias("kat");
        assertEquals((byte) 'k', c.byteAt(0));
        assertEquals((byte) 'a', c.byteAt(1));
        assertEquals((byte) 't', c.byteAt(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.byteAt(3));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.byteAt(-1));
    }

    @Test
    public void test_charAt() {
        Alias c = new Alias("kat");
        assertEquals('k', c.charAt(0));
        assertEquals('a', c.charAt(1));
        assertEquals('t', c.charAt(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.charAt(3));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> c.charAt(-1));
    }

    @Test
    public void test_toString() {
        Value v = new Value("kat");
        assertEquals(105950, v.hashCode());
        assertSame(v.toString(), v.toString());

        v.add(".plus");
        assertSame(v.toString(), v.toString());
        assertEquals(1057563562, v.hashCode());

        v.set(3, (byte) '+');
        assertSame(v.toString(), v.toString());
        assertEquals("kat+plus", v.toString());
        assertEquals(1054792999, v.hashCode());
        assertSame(v.toString(), v.toString(0, 8));

        assertEquals("t+p", v.toString(2, 5));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> v.toString(-1, 1));

        v.clear();
        assertSame("", v.toString());
        assertSame("", v.toString(0, 0));
        assertSame(v.toString(), v.toString());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> v.toString(-1, 1));

        String name = "陆之岇";
        byte[] temp = name.getBytes(UTF_8);

        v.add(temp);
        assertEquals(name, v.toString());
        assertEquals(new String(temp, UTF_8), v.toString(UTF_8));
        assertEquals(new String(temp, US_ASCII), v.toString(US_ASCII));
        assertEquals(new String(temp, ISO_8859_1), v.toString(ISO_8859_1));
    }

    @Test
    public void test_digest() {
        String text = "User{i:id(1)s:name(kraity)}";
        Value value = new Value(text);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", new Value(0).digest());
        assertEquals("d04f45fd1805ea7a98821bdad6894cb4", value.digest());
        assertEquals("21707be3777f237901b7edcdd73dc8288a81a4d2", value.digest("SHA1"));
    }

    @Test
    public void test_startWith() {
        Value value = new Value("plus.kat");
        assertTrue(value.startWith("plus"));
        assertTrue(value.startWith("plus."));

        assertFalse(value.startWith("kat"));
        assertFalse(value.startWith(".plus"));
        assertFalse(value.startWith("kat.plus"));
        assertFalse(value.startWith("plus$kat"));
        assertFalse(value.startWith("$plus$kat$"));
    }

    @Test
    public void test_endsWith() {
        Value value = new Value("plus.kat");
        assertTrue(value.endsWith("kat"));
        assertTrue(value.endsWith(".kat"));

        assertFalse(value.endsWith("plus"));
        assertFalse(value.endsWith("kat."));
        assertFalse(value.endsWith("kat.plus"));
        assertFalse(value.endsWith("$plus$kat$"));
    }

    @Test
    public void test_indexOf() {
        Value value = new Value("plus.kat.plus");

        assertEquals("plus.kat.plus".indexOf('.'), value.indexOf('.'));
        assertEquals("plus.kat.plus".indexOf('k'), value.indexOf('k'));
        assertEquals("plus.kat.plus".indexOf("p"), value.indexOf("p"));
        assertEquals("plus.kat.plus".indexOf("k"), value.indexOf("k"));
        assertEquals("plus.kat.plus".indexOf("kat"), value.indexOf("kat"));

        assertEquals("plus.kat.plus".indexOf('.', 10), value.indexOf('.', 10));
        assertEquals("plus.kat.plus".indexOf("kat", 10), value.indexOf("kat", 10));

    }

    @Test
    public void test_lastIndexOf() {
        Value value = new Value("plus.kat.plus");

        assertEquals("plus.kat.plus".lastIndexOf('.'), value.lastIndexOf('.'));
        assertEquals("plus.kat.plus".lastIndexOf('k'), value.lastIndexOf('k'));
        assertEquals("plus.kat.plus".lastIndexOf("p"), value.lastIndexOf("p"));
        assertEquals("plus.kat.plus".lastIndexOf("k"), value.lastIndexOf("k"));
        assertEquals("plus.kat.plus".lastIndexOf("kat"), value.lastIndexOf("kat"));

        assertEquals("plus.kat.plus".lastIndexOf('.', 1), value.lastIndexOf('.', 1));
        assertEquals("plus.kat.plus".lastIndexOf("kat", 1), value.lastIndexOf("kat", 1));

    }

    @Test
    public void test_contains() {
        Value value = new Value("plus.kat.plus");

        assertTrue(value.contains('.'));
        assertTrue(value.contains("kat"));
        assertTrue(value.contains("plus.kat.plus"));
        assertFalse(value.contains("plus.kat.plus$"));
        assertFalse(value.contains("$plus.kat.plus"));
    }

    @Test
    public void test_compareTo() {
        String s = "kat.plus";
        Value v = new Value(s);

        assertEquals(0, v.compareTo("kat.plus"));
        assertEquals(s.compareTo("+kat.plus"), v.compareTo("+kat.plus"));
        assertEquals(s.compareTo("kat.plus+"), v.compareTo("kat.plus+"));
        assertEquals(s.compareTo("+kat.plus+"), v.compareTo("+kat.plus+"));
    }

    @Test
    public void test_getBytes() {
        Value value = new Value(
            "See the License for the specific language governing permissions and limitations under the License."
        );
        assertTrue(value.length() < 127);

        String s1 = "See the License for the specific language";
        byte[] b1 = new byte[s1.length()];
        assertEquals(s1.length(), value.getBytes(0, b1));
        assertEquals(s1, new String(b1));

        String s2 = "License for the specific language";
        byte[] b2 = new byte[s2.length()];
        assertEquals(s2.length(), value.getBytes(8, b2));
        assertEquals(s2, new String(b2));
        assertThrows(IndexOutOfBoundsException.class, () -> value.getBytes(128, b2));

        // specific language governing permissions
        int length = 16;
        byte[] b3 = new byte[length];

        assertEquals(8, value.getBytes(24, b3, 0, 8));
        assertEquals("specific", new String(b3, 0, 8));

        assertEquals(8, value.getBytes(33, b3, 8, 8));
        assertEquals("language", new String(b3, 8, 8));
        assertEquals("specificlanguage", new String(b3, 0, 16));

        assertEquals(14, value.getBytes(33, b3, 2, 32));
        assertEquals("language gover", new String(b3, 2, 14));
        assertEquals("splanguage gover", new String(b3, 0, 16));

        assertEquals(0, value.getBytes(12, b3, 0, 0));
        assertEquals(length, value.getBytes(12, b3, 0, 128));

        assertEquals(98, value.length());
        assertEquals(98, value.capacity());
        assertEquals(0, value.getBytes(98, b3));
        assertEquals(0, value.getBytes(97, b3, length, length));
        assertEquals(-1, value.getBytes(98, b3, length, length));

        assertThrows(IndexOutOfBoundsException.class, () -> value.getBytes(12, b3, -1, 6));
        assertThrows(IndexOutOfBoundsException.class, () -> value.getBytes(128, b3, 0, 8));
        assertThrows(IndexOutOfBoundsException.class, () -> value.getBytes(1024, b3, 1, 6));
    }

    @Test
    public void test_InputStream() {
        Value v1 = new Value();
        v1.add(
            new ByteArrayInputStream(
                "kat.plus".getBytes(UTF_8)
            )
        );
        assertEquals("kat.plus", v1.toString());

        for (int i = 0; i < 128; i++) {
            v1.add(".kat.plus");
        }
        Value v2 = new Value();
        v2.add(
            new ByteArrayInputStream(
                v1.value, 0, v1.count
            )
        );

        String s1 = v1.toString();
        String s2 = v2.toString();
        assertEquals(s1.length(), s2.length());
        assertEquals(s1, s2);
    }

    @Test
    public void test_chain_InputStream() throws IOException {
        Value value = new Value();
        Alias alias = new Alias("kraity");

        try (InputStream in = alias.toInputStream()) {
            value.add(in, 128);
        }

        assertEquals(6, value.length());
        assertEquals(6, value.capacity());
    }

    @Test
    public void test_grow() {
        Chalk c = new Chalk();
        byte[] b = "kat.plus".getBytes(ISO_8859_1);

        c.value = b;
        c.count = b.length;

        c.grow(b.length + 2);
        assertEquals(b.length, c.count);
        assertTrue(b.length + 2 <= c.value.length);
    }

    @Test
    public void test_swop() {
        Chalk c = new Chalk();
        byte[] b = "kat.plus".getBytes(ISO_8859_1);

        c.value = b;
        c.count = b.length;

        c.swop(1, 6);
        assertEquals("klp.taus", c.toString(ISO_8859_1));
    }

    @Test
    public void test_move() {
        Chalk c = new Chalk();
        c.count = 6;
        c.value = "kat.plus".getBytes(ISO_8859_1);

        c.move(2, 2);
        assertEquals("kat.t.", c.toString(ISO_8859_1));

        c.move(2, -2);
        assertEquals("t.t.t.", c.toString(ISO_8859_1));

        c.grow(32);
        c.chain("kat.plus", 0, 8);
        assertEquals("t.t.t.kat.plus", c.toString(ISO_8859_1));

        c.move(6, -4);
        assertEquals("t.kat.plusplus", c.toString(ISO_8859_1));

        c.move(0, 4);
        assertEquals("t.kat.kat.plus", c.toString(ISO_8859_1));
    }
}
