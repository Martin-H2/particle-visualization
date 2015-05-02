import java.io.FileInputStream;

import FileUtil.MmpldParser;


public class Main {

	public static void main(String[] args) {
		// just for testing ...
		try {
			MmpldParser.parse(new FileInputStream("D:/tools/MegaMol/blasen_all.mmpld"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
