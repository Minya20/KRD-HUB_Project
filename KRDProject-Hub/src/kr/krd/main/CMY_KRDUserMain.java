package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import kr.krd.dao.CMY_MemberDAO;

public class CMY_KRDUserMain {
	private BufferedReader br;
	private CMY_MemberDAO dao;
	private String cust_id;//로그인한 회원 아이디
	private String role; //사용자의 역할
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)
	
	public CMY_KRDUserMain() {
		try {
			br = new BufferedReader(
					new InputStreamReader(
							     System.in));
			dao = new CMY_MemberDAO();
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
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 로그인						 │");
			System.out.println("│	2. 회원가입					 │");
			System.out.println("│	3. 공고조회					 │");
			System.out.println("│	4. 종료						 │");
			System.out.println("│							 │");
			System.out.println("│						ver.1.0	 │");
			System.out.println("└────────────────────────────────────────────────────────┘");
			System.out.println("［원하시는 메뉴를 선택하세요 ]");
			System.out.print(">>");
			try {
				int MenuNo = Integer.parseInt(br.readLine());
				if(MenuNo == 1) {//로그인
					System.out.println("┌────────────────────────────────────────┐");
					System.out.println("│					 │ ");
					System.out.println("│	로그인[1]	 			 │");
					System.out.println("│					 │");
					System.out.println("│	ID	________________	 │");
					System.out.println("│	PW	________________	 │");
					System.out.println("│					 │");
					System.out.println("│	아이디 / 비밀번호 찾기 [2]		 │");
					System.out.println("│					 │");
					System.out.println("└────────────────────────────────────────┘");
					System.out.println(">> 진행하실 사항을 선택하세요 [1] : 로그인 | [2] : 아이디/비밀번호 찾기 :  | [3] 뒤로 가기");
					int secMenuNo = Integer.parseInt(br.readLine());
					if(secMenuNo == 1) {
						//로그인 진행
						System.out.print("ID : ");
						String user_id = br.readLine();
						System.out.print("PW : ");
						String user_pw = br.readLine();
						//메서드이름(user_id,user_pw);
						cust_id=dao.userLogin(user_id, user_pw);
						if(!cust_id.equals("0") && !cust_id.equals(null)) {// cust_id가 0 또는 null이 아니면 : 즉 유저 아이디 값이 존재함.
							
							login = true;					//사용자의 로그인 상태를 TRUE로 변경함
							role = dao.getUserRole(cust_id);//사용자의 권한을 반환하는 메서드를 사용하여 role에 해당하는 권한을 넣음
							break;
						}
					}else if(secMenuNo == 2) {
						//아이디/비밀번호 찾기로 진행
					}else if(secMenuNo == 3) {
						continue;
					}
				}else if(MenuNo == 2) {//회원가입
					
				}else if(MenuNo == 3) {//공고조회
					
				}else if(MenuNo == 4) {
					System.out.println("프로그램 종료");
					break;
				}
				else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("잘못 입력했습니다.");
			}
		}//end of while
		//로그인시 보여지는 메뉴
		while(login) {
			System.out.println("일단 뭐든 로그인으로 들어왔어");
			if(role.equals("ADM")) {
				System.out.println("-".repeat(20));
				System.out.println("어드민으로 진입시 구현될 화면");
				//로그아웃은 개인 구현
				System.out.println("-".repeat(20));
				break;
			}else if(role.equals("AGY")) {
				System.out.println("-".repeat(20));
				System.out.println("기관 관리자로 진입시 구현될 화면");
				//로그아웃은 개인 구현
				System.out.println("-".repeat(20));
				break;
			}else if(role.equals("RESI")) {
				System.out.println("-".repeat(20));
				System.out.println("개인 연구자로 진입시 구현될 화면");
				//로그아웃은 개인 구현
				System.out.println("-".repeat(20));
				break;
			}else if(role.equals("RESO")) {
				System.out.println("-".repeat(20));
				System.out.println("단체 연구자로 진입시 구현될 화면");
				//로그아웃은 개인 구현
				System.out.println("-".repeat(20));
				break;
			}else if(role.equals("REV")) {
				dao.callReviewerMenu();
				break;
			}else if(role.equals("GST")){
				System.out.println("GST는 아직 미구현이야...");
				//로그아웃은 개인 구현
				break;
			}else {
				System.out.println("로그인 했는데 권한이 없어???");
			}
		}//end of while
		System.out.println("일단 프로그램 종료 부분");
	}
	
	public static void main(String[] args) {
		new CMY_KRDUserMain();
	}
}
