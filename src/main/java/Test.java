import de.crazydev22.utils.CipherUtil;

public class Test {

	public static void main(String[] args) {
		String key = "";
		String text = "Rechte strich loffel ri laufen kommst ab mussen. Tanzmusik sie verharrte das gepfiffen angerufen gut. Madele gar singen aus loffel regnen sie. Eigentlich aneinander ja te langweilig gesprachig ja bilderbuch in aufzulosen. Leber bis einen fremd adieu bis. Das das flick ten flo ruhig viele wills knapp denen.";

		var pair = CipherUtil.encrypt(text.getBytes(), key);

		String iv = CipherUtil.toString(pair.iv());
		System.out.println(iv);

		System.out.println(new String(CipherUtil.decrypt(pair.encrypted(), CipherUtil.toBytes(iv), key)));
	}
}
