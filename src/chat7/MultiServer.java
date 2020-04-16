package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 귓속말 고정 상태에서 리스트 출력
귓속말 대상 없으면 오류 메시지 출력
본인에게 귓속말 못하게 설정

 */
public class MultiServer {

	PreparedStatement psmt;
	Connection con;
	ResultSet rs;

	static ServerSocket serverSocket = null;
	static Socket socket = null;
	//클라이언트 정보 저장을 위한 Map컬렉션 정의
	Map<String, PrintWriter> clientMap;

	//생성자
	public MultiServer() {


		//클라이언트의이름과 출력스트림을 저장할 HashMap생성 
		clientMap = new HashMap<String, PrintWriter>();
		//HaspMap동기화 설정. 쓰레드가 사용자정보에 동시에 접근하는 것을 차단한다. 
		Collections.synchronizedMap(clientMap);
	}

	//서버 초기화
	public void init() {
		
		try {
			try {
				Class.forName("oracle.jdbc.OracleDriver");
				con = DriverManager.getConnection
						("jdbc:oracle:thin://@localhost:1521:orcl", 
								"kosmo","1234"
						);
				System.out.println("오라클 DB 연결성공");

				
			}
			catch (ClassNotFoundException e) {
				System.out.println("오라클 드라이버 로딩 실패");
				e.printStackTrace();
			}
			catch (SQLException e) {
				System.out.println("DB 연결 실패");
				e.printStackTrace();
			}
			catch (Exception e) {
				System.out.println("알수 없는 예외 발생");
			}
			
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다. ");

			/*
			1명의 클라이언트가 접속할 때마다 접속을 허용(accept())해주고 
			동시에 MultiServerT쓰레드를 생성한다.  
			해당 쓰레드는 1명의 클라이언트가 전송하는 메시지를 읽어서 
			Echo해주는 역할을 담당한다. 
			 */
			while(true) {
				socket = serverSocket.accept();				
				Thread mst = new MultiServerT(socket);
				/*
				클라이언트의 메시지를 모든 클라이언트에게 
				전달하기 위한 쓰레드 생성 및 start.
				 */

				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//메인메소드 : Server객체를 생성한 후 초기화한다. 
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}


	//귓속말
	public void secretMsg(String from_name, String to_name, String to_content) {
		Iterator<String> it = clientMap.keySet().iterator();

		while (it.hasNext()) {
			String user = it.next();
			
			try {
				PrintWriter it_out = (PrintWriter)
						clientMap.get(user);
						//clientMap.get(it.next());
						//clientMap.get(name);
				
				if(to_name.equals(user)) {
					it_out.println("["+from_name+"] 님이 보낸 메시지:"+to_content);
				}
				
			}
			catch (Exception e) {
				System.out.println("예외:444444"+ e);
			}
		}
		
	}
	
	
	// /만 입력한 접속자에게 명령어 종류 알려주기
	public void iMsg(String name, String msg) {
		Iterator<String> it = clientMap.keySet().iterator();

		
		try {
			PrintWriter list_out = (PrintWriter)
					//clientMap.get(it.next());
					clientMap.get(name);
			list_out.println("명령어를 입력하세요. ");
			list_out.println("[/list] 리스트 보기");
			list_out.println("[/to 아이디] 귓속말 보내기 고정");
			list_out.println("[/to 아이디 메시지] 귓속말 한 번 보내기");
			
		}
		catch (Exception e) {
			System.out.println("예외:55555"+ e);
		}
	}
	
	
	//list를 검색한 클라이언트에게 list를 전달하는 메소드
	public void listMsg(String name, String msg) {
		Iterator<String> it = clientMap.keySet().iterator();

		
		try {
			PrintWriter list_out = (PrintWriter)
					//clientMap.get(it.next());
					clientMap.get(name);
			
			list_out.println("-------------L I S T-------------");
			list_out.println("-------"+clientMap.size()+"명 접속중입니다. -------");
			list_out.println("[접속자 대화명]="+clientMap.keySet());
			list_out.println("---------------------------------");
	
			
		}
		catch (Exception e) {
			System.out.println("예외:66666"+ e);
			
		}
	}
	
	
	
	
	
	//접속된 모든 클라이언트에게 메시지를 전달하는 역할의 메소드
	public void sendAllMsg(String name, String msg){

		//Map에 저장된 객체의 키값(이름)을 먼저 얻어온다. 
		Iterator<String> it = clientMap.keySet().iterator();

		//저장된 객체(클라이언트)의 개수만큼 반복한다. 
		while(it.hasNext()) {

			try {
				//각 클라이언트의 PrintWriter객체를 얻어온다. 
				PrintWriter it_out = (PrintWriter)
						clientMap.get(it.next());

				//클라이언트에게 메시지를 전달한다. 
				/*
					매개변수 name이 있는 경우에는 이름+메시지
					없는 경우에는 메시지만 클라이언트로 전송한다. 
				 */
				if(name.equals("")){
					it_out.println(URLEncoder.encode(msg, "UTF-8"));
				}
				else {
					it_out.println("["+name+"]:"+msg);
			
				}
			}
			catch (Exception e) {
				System.out.println("예외:77777" + e);
			}
		}
	}
	//내부클래스
	class MultiServerT extends Thread{

		//멤버변수
		Socket socket;
		PrintWriter out  = null;
		BufferedReader in = null;

		//생성자 : Socket을 기반으로 입출력 스트림을 생성한다. 
		public MultiServerT(Socket socket) {
			this.socket =socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new
						InputStreamReader(this.socket.getInputStream()));

			}
			catch (Exception e) {
				System.out.println("예외11111:" + e); 
			}

		}
		@Override
		public void run() {

			Date time = new Date();
			SimpleDateFormat format1 = 
					new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			String time1 = format1.format(time);
			//SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");

			//클라이언트로부터 전송된 "대화명"을 저장할 변수
			String name = "";

			//메시지 저장용 변수
			String s= "";
		
			try {
				//클라이언트의 이름을 읽어와서 저장
				name = in.readLine();
				name = URLDecoder.decode(name, "UTF-8");

				//접속한 클라이언트에게 새로운 사용자의 입장을 알림. 
				//접속자를 제외한 나머지 클라이언트만 입장 메시지를 받는다. 
				sendAllMsg("", name+"님이 입장하셨습니다. ");

				//현재 접속한 클라이언트를 HashMap에 저장한다. 
				clientMap.put(name, out);

				//HashMap에 저장된 객체의 수로 접속자 수를 파악할 수 있다. 
				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는 " +clientMap.size()+"명입니다. ");


				//입력한 메시지는 모든 클라이언트에게 Echo된다.  
				while(in !=null) {
					
					s = in.readLine();
					s = URLDecoder.decode(s, "UTF-8");
					
					if(s==null) {
						break;
					}
					//Iterator<String> toIt = clientMap.keySet().iterator();
					try {
						if(s.charAt(0)=='/') {
							
							// /만 입력했을 때
							if(s.equals("/")) {
								iMsg(name, s);
									continue;
							}
							
							//리스트 확인
							if(s.equals("/list")) {
								//	System.out.println(name+" 접속 리스트를 확인했습니다. ");
									listMsg(name, s);
									continue;
								}

							//귓속말
							if(s.substring(1, 3).equals("to") && s.substring(2, 4).equals("o ")) {
								
								String[] sArr = s.split(" ");
							
								
								//아이디를 입력하지 않았을 때 
								if(sArr.length==1) {
									out.println("귓속말 대상을 입력하세요.");
									continue;
								}
								
								String to_name = sArr[1];
								String to_content = "";
								
								
								if(sArr.length==2) {
									out.println(to_name+"님에게 귓속말이 고정되었습니다. ");
									out.println("고정 해제 = x");

									while(true) {
										to_content=in.readLine();
										
										if(to_content.equalsIgnoreCase("x")) {
											out.println("고정 귓속말 해제 - 모두에게 메시지가 전송됩니다.");
											break;
										}
										
										
										if(sArr[1]==name) {
											out.print("테스트테스트테스트");
										}
									
										
										// /만 입력했을 때
										if(to_content.equals("/")) {
											out.println("/ 입력했을 때 ");
											iMsg(name, to_content);
											continue;
										}

										//리스트 확인
										if(to_content.equals("/list")) {
											//	System.out.println(name+" 접속 리스트를 확인했습니다. ");
											out.println("/list입력했을 때 ");
											listMsg(name, to_content);
											continue;
										}
										
										
										
										secretMsg(name, to_name, to_content);
									}
								}
								
								
								if(sArr.length >= 3) {
									for (int i = 2; i<sArr.length; i++) {
										to_content += " " + sArr[i]; 
									}
									secretMsg(name, to_name, to_content);
								}
							}
							else if(s.startsWith("/")) {
								out.println("명령어를 잘못 입력하셨습니다. ");
							}
							continue;
						}
						
					}
					catch (Exception e) {
				//		System.out.println("예외:22222"+e);
					}
					String query = "INSERT INTO chating_tb VALUES "
							+ " (seq_chating.nextval, ? , sysdate, ? )";
					psmt = con.prepareStatement(query);
					
					psmt.setString(1, name);
					psmt.setString(2, s);
					psmt.executeUpdate();
					
					//System.out.print(time1+" [");
					System.out.print(time1+" [");
					System.out.println(name + "] " + s);
			

					sendAllMsg(name, s);
				
				}
			}
			catch (Exception e) {
				System.out.println("예외:333333"+e);
			}
			finally {
				/*
					클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로 넘어오게 된다. 
					이때 "대화명"을 통해 remove()시켜준다.  


				 */
				clientMap.remove(name);
				sendAllMsg("", name+"님이 퇴장하셨습니다. ");
				//퇴장하는 클라이언트의 쓰레드명을 보여준다. 
				System.out.println(name + "["+
						Thread.currentThread().getName()+"] 퇴장");
				System.out.println("현재 접속자 수는 "+clientMap.size()+"명입니다. ");
				try {
					in.close();
					out.close();
					socket.close();
					if(psmt != null) psmt.close();
					if(con != null) con.close();
					if(rs != null) rs.close();
				}
				catch (Exception e) {
					e.printStackTrace();

				}
			}
		}
	}
}





