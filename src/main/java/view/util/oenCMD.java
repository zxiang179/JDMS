package view.util;

public class oenCMD {
	// public static void main(String[] args) {
	// // openWinExe(null);
	// openExe(null,"http://www.baidu.com");
	// }
	// �� Java ����windowsϵͳ��exe�ļ�������notepad��calc֮��
	public static void openWinExe(String command, String url) {
		if (command == null || command.equals("")) {
			command = "chrome " + url;
		}
		Runtime rn = Runtime.getRuntime();
		Process p = null;
		try {

			p = rn.exec(command);
		} catch (Exception e) {
			System.out.println("Error win exec!");
		}
	}

	// ���������Ŀ�ִ���ļ������磺�Լ�������exe������ ���� ��װ�����.
	public static void openExe(String pathAndName, String url) {
		if (pathAndName == null || pathAndName.equals("")) {
			pathAndName = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
		}
		if (url != null && !url.equals("")) {
			pathAndName += " ";
			pathAndName += url;
		}
		Runtime rn = Runtime.getRuntime();
		Process p = null;
		try {
			p = rn.exec(pathAndName);
		} catch (Exception e) {
			System.out.println("Error exec!");
		}
	}
}