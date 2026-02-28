package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.CMY_MemberDAO;
import kr.util.USH_ConsoleUtil;

public class USH_KRDUserMain {
	private final BufferedReader br;
	private final CMY_MemberDAO dao;
	private final USH_ConsoleUtil io;
	private String cust_id;//로그인한 회원 아이디

	public USH_KRDUserMain() {
		br = new BufferedReader(new InputStreamReader(System.in));
		io = new USH_ConsoleUtil(br);
		dao = new CMY_MemberDAO();
		try {
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}catch(IOException e) {}
		}
	}
     
	//공통 메뉴(로그인/회원가입/종료)
	private void callMenu()throws IOException{
		while(true) {
			System.out.println("1.로그인");
			System.out.println("2.회원가입");
			System.out.println("3.종료");
			System.out.print("입력 > ");
			
			int no;
			
			try {
				no = Integer.parseInt(br.readLine());
			} catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
				continue;
			}
			
			if(no == 1) {
				System.out.print("ID : ");
				String user_id = br.readLine();
				System.out.print("PW : ");
				String user_pw = br.readLine();
				
				cust_id = dao.userLogin(user_id, user_pw);
				
				if(cust_id != null && !cust_id.equals("0")) {
					System.out.println("로그인 성공: " + cust_id);
					
					//1.권한 조회
					String role = dao.getUserRole(cust_id);
					if(role == null) {
						System.out.println("권한 조회 실패.");
						cust_id = null;
						continue;
					}
					
					//2.권한에 따라 분기
					if("ADM".equalsIgnoreCase(role)) {
						//주입 생성자 호출로 변경
						new USH_KRDAdminMain(br, cust_id);
						continue;
					}else if("REV".equalsIgnoreCase(role)) {
						//추후 메뉴 합칠 때 구현
						String field = dao.getUserField(cust_id);
						dao.callReviewerMenu(cust_id, role, field);
					}else {
						System.out.println("해당 권한 메뉴는 아직 미구현입니다.");
					}
					cust_id = null;					//메뉴로 돌아오면 로그아웃 처리(선택)
					continue;//로그인 메뉴 종료.		//다시 로그인 메뉴로.
				}else {
					System.out.println("로그인 실패: 아이디/비밀번호 확인");
				}
				
			}else if(no == 2){
				System.out.println("회원가입은 아직 구현 전입니다.");
			}else if(no == 3) {
				System.out.println("프로그램 종료");
				return;
			}else {
				System.out.println("잘못 입력했습니다.");
			}
			
		}

	}

	public static void main(String[] args) {
		new USH_KRDUserMain();
	}
}