package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.MemberDAO;

public class HYJ_KRDAdminMain {
	private BufferedReader br;
	private MemberDAO dao;
	
	public HYJ_KRDAdminMain() {
		try {
			br = new BufferedReader(
					new InputStreamReader(
							       System.in));
			dao = new MemberDAO();
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
			System.out.print(
			"1.공고 조회,2.과제 신청,3.내 신청조회,4.선정 결과 확인,5.보고서 제출,6.내 정보 수정,7.인재 열람,8.종>");
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
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	public static void main(String[] args) {
		new HYJ_KRDAdminMain();
	}
}


