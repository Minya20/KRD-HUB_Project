package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.CMY_MemberDAO;

public class SY_KRDRESIUserMain {
	private BufferedReader br;
	private CMY_MemberDAO dao;
	private String cust_id;//로그인한 회원 아이디

	public SY_KRDRESIUserMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new CMY_MemberDAO();
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}catch(IOException e) {}
		}
	}
     
	//메뉴
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
					
					//로그인 성공 후 RESUUI 실행
					new SY_KRDRESIUI(cust_id);
					return; //로그인 메뉴 종료.
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
		new SY_KRDRESIUserMain();
	}
}