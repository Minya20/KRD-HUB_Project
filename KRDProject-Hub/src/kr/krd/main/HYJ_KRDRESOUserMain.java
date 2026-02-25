package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.MemberDAO;

public class HYJ_KRDRESOUserMain {
	private BufferedReader br;
	private HYJ_KRDRESOUserDAO dao;
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)

	public HYJ_KRDRESOUserMain() {
		try {
			br = new BufferedReader(
					new InputStreamReader(
							System.in));
			dao = new HYJ_KRDRESOUserDAO();
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
			System.out.print(
					"1.공고 조회,2.과제 신청,3.내 신청조회,4.선정 결과 확인,5.보고서 제출,6.내 정보 수정,7.인재 열람,8.종료>");
			try {
				int no = Integer.parseInt(
						br.readLine());
				if(no == 1) {//공고 조회
					dao.selectAnn();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					System.out.println("프로그램을 종료합니다.");
					break;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}//end of while
	}

	public static void main(String[] args) {
		new HYJ_KRDRESOUserMain();
	}
}
