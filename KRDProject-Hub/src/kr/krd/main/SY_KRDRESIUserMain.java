package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.CMY_MemberDAO;

public class SY_KRDRESIUserMain {
	private BufferedReader br;
	private String role;
	private HYJ_KRDRESOUserDAO dao1;
	private CMY_MemberDAO dao2;
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)

	public SY_KRDRESIUserMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));

			dao1 = new HYJ_KRDRESOUserDAO();
			dao2 = new CMY_MemberDAO();

			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}
			catch(IOException e) {}
		}
	}
     
	//메뉴
	private void callMenu()throws IOException{
		while(true) {
			System.out.print("1.로그인,2.회원가입,3.종료>");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {//로그인
					//로그인 진행
					System.out.print("ID : ");
					String user_id = br.readLine();
					System.out.print("PW : ");
					String user_pw = br.readLine();
					//메서드이름(user_id,user_pw);
					cust_id=dao2.userLogin(user_id, user_pw);
					if(!cust_id.equals("0") && !cust_id.equals(null)){// cust_id가 0 또는 null이 아니면 : 즉 유저 아이디 값이 존재함.
						
						login = true;//사용자의 로그인 상태를 TRUE로 변경함
						break;
						
					}

				}else if(no == 2) {//회원가입

				}else if(no == 3) {//종료
					System.out.println("프로그램 종료");
					break;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("잘못 입력했습니다.");
			}
		}//end of while
		//로그인시 보여지는 메뉴
		while(login) {
			System.out.println("==========개인 연구자 메뉴==========");
			System.out.println("1.공고 조회");
			System.out.println("2.과제 신청");
			System.out.println("3. 내 신청 조회");
			System.out.println("4. 선정 결과 확인");
			System.out.println("5. 보고서 제출");
			System.out.println("6. 내 정보 수정");
			System.out.println("7. 종료");
			System.out.print("입력 > ");
			
			try {
				int no = Integer.parseInt(br.readLine());
				
				if(no == 1) {
					
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					System.out.println("프로그램을 종료합니다.");
					break;
				}else if(no == 6){
					
				}else if(no == 7) {
					
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}//end of while
	}

	public static void main(String[] args) {
		new SY_KRDRESIUI();
	}
}