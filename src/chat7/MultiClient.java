package chat7;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {
		
		System.out.println("이름을 입력하세요:");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
	
		
		//Sender가 기능을 가져가므로 여기서는 필요 없음
		//PrintWriter out = null;
		
		//리시버가 기능을 가져가므로 여기서는 필요 없음
		//BufferedReader in = mull;
		
		try {
			String ServerIP = "localhost";
			if(args.length > 0) {
				ServerIP = args[0];
			}
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다. ");
			
			//서버에서 보내는 Echo메시지를 클라이언트에 출력하기 위한 쓰레드 생성
			Thread receiver = new Receiver(socket);
			receiver.start();
			
			//클라이언트의 메시지를 서버로 전송해주는 쓰레드 생성
			Thread sender = new Sender(socket, s_name);
			sender.start();
		}
		catch (Exception e) {
			System.out.println("예외 발생 [MultiClient]"+ e);
		}
		
	}
}
