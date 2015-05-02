import FileUtil.MmpldParser;


public class Main {

	public static void main(String[] args) {
		// just for testing ...
		try {
			MmpldParser.parse("D:/tools/MegaMol/blasen_all.mmpld");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
