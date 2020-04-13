package chat4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {


	static ServerSocket serverSocket = null;
	static Socket socket  =null;
	static PrintWriter out = null;
	static BufferedReader in = null;
	static String s = ""; //클라이언트의 메시지를 저장
	//String name = "";//클라이언트의 이름을 저장

	
	//생성자
	public MultiServer() {
		//실행부 없음
	}

	//서버의 초기화를 담당할 메소드
	public static void init() {
		
		//클라이언트로부터 전송받은 이름을 저장
		String name = "";

		try {
			//9999포트를 열고 클라이언트의 접속을 대기 
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다. ");

			//클라이언트의 접속 요청을 허가함. 
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress()+":"+
					socket.getPort());


			/*
			getInetAddress() : 소켓이 연결되어 있는 원격 IP주소를 얻어옴.
			getPor() : 소켓이 연결되어 있는 원격 포트번호를 얻어옴. 
			 */


			//클라이언트로 메시지를 보낼 준비(output 스트림)
			out = new PrintWriter(socket.getOutputStream(), true);

			//클라이언트가 보내주는 메시지를 읽을 준비(input 스트림)
			in = new BufferedReader(new 
					InputStreamReader(socket.getInputStream()));


			//클라이언트가 최초로 보내는 메시지는 접속자의 이름
			if(in !=null) {
				name = in.readLine();
				//이름을 콘솔에 출력하고 
				System.out.println(name + " 접속");
				//클라이언트로 Echo해준다. 
				out.println("> "+name+"님이 접속했습니다.");
			}


			//클라이언트가 전송하는 메시지를 계속해서 읽어옴
			while(in !=null) {
				s = in.readLine();
				if(s==null) {
					break;
				}
				//읽어온 메시지를 콘솔에 출력하고 
				System.out.println(name+" ==>" + s);
				//클라이언트에게 Echo해준다. 
				sendAllMsg(name, s);
			}

			System.out.println("Bye...!!!");

		}
		catch(Exception e) {
			System.out.println("예외1:"+e);
			//e.printStackTrace();
		}

		finally {
			try {
				//입출력 스트림 종료
				in.close();
				out.close();
				//소켓 종료
				socket.close();
				serverSocket.close();
			}
			catch(Exception e) {
				System.out.println("예외2:"+e);
				//e.printStackTrace();
			}
		}

	}
	//서버가 클라이언트에게 메시지를 Echo해주는 메소드
	public static void sendAllMsg(String name, String msg) {
		try {
			out.print("> "+name+" ==>"+msg);
		}
		catch (Exception e) {
			System.out.println("예외:"+ e);
		}
	}
	public static void main(String[] args){
		init();
	}

}
