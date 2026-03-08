package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.HYJ_MyInfoDAO;
import kr.krd.dao.HYJ_RESISearch;
import kr.krd.dao.HYJ_ReportDAO;
import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.HYJ_APPLICATIONCheakDAO;

public class HYJ_KRDRESOUserMain {
	private BufferedReader br;
	private String role;
	private HYJ_KRDRESOUserDAO dao1;
	private CMY_MemberDAO dao2;
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)
	private HYJ_MyInfoDAO dao3;
	private HYJ_APPLICATIONCheakDAO dao4;
	private HYJ_ReportDAO dao5;
	private HYJ_RESISearch dao6;

	
	/*public HYJ_KRDRESOUserMain() {
		try {
			br = new BufferedReader(
					new InputStreamReader(
							System.in));

			dao1 = new HYJ_KRDRESOUserDAO();
			dao2 = new CMY_MemberDAO();
			dao3 = new HYJ_MyInfoDAO();
			dao4 = new HYJ_APPLICATIONCheakDAO();
			dao5 = new HYJ_ReportDAO();
			dao6 = new HYJ_RESISearch();

			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}
			catch(IOException e) {}
		}
	}
	*/

	//메뉴
	public boolean callMenu(String myUser_id, String myRole, boolean myLogin) throws IOException{
		br = new BufferedReader(new InputStreamReader(System.in));
		cust_id = myUser_id;
		role = myRole;
		login = myLogin;
		/*
		while(!login) { //login이 false 이면
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
		*/
		//로그인시 보여지는 메뉴
		while(login) {
			System.out.print(
					"1.공고 조회,2.내 신청조회,3.보고서 제출,4.내 정보 수정,5.인재 열람,6.로그아웃 7.종료>");
			try {
				int no = Integer.parseInt(
						br.readLine());
				if(no == 1) {//공고 조회
					dao1 = new HYJ_KRDRESOUserDAO();
					dao1.selectAnn(cust_id);
				}else if(no == 2) {
					dao4 = new HYJ_APPLICATIONCheakDAO();
					dao4.CheckMyApp(cust_id);
				}else if(no == 3) {
					dao5 = new HYJ_ReportDAO();
					dao5.InsertReport(cust_id);
				}else if(no == 4) {
					dao3 = new HYJ_MyInfoDAO();
					dao3.SelectInfo(cust_id);
				}else if(no == 5) {
					dao6 = new HYJ_RESISearch();
					dao6.RESISearch();
				}else if(no == 6) {
					System.out.println("로그아웃을 진행합니다.");
					break;
				}else if(no == 7) {
					System.out.println("프로그램을 종료합니다.");
					System.exit(1);
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}//end of while
		return login;	//리턴 값을 true로 보내야지 UserMain에서 로그인 처리를 할 수 있음
	}

	public static void main(String[] args) {
		//new HYJ_KRDRESOUserMain();
	}
}
