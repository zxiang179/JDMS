package view.util;

public class oenCMD {
	// public static void main(String[] args) {
	// // openWinExe(null);
	// openExe(null,"http://www.baidu.com");
	// }
	// 用 Java 调用windows系统的exe文件，比如notepad，calc之类
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

	// 调用其他的可执行文件，例如：自己制作的exe，或是 下载 安装的软件.
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