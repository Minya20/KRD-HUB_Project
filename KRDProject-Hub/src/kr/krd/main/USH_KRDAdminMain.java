package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.MemberDAO;
import kr.krd.dao.USH_MemberDAO; //취합시 MemberDAO로 변경해야하니 삭제

public class USH_KRDAdminMain {
	private BufferedReader br;
	private USH_MemberDAO dao; //취합시 MemberDAO로 변경
	
	public USH_KRDAdminMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new USH_MemberDAO();
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
			System.out.println("===== 시스템 관리자 메뉴 =====");
			System.out.println("1.전체 회원 관리 메뉴");
			System.out.println("2.예산 관리 메뉴");
			System.out.println("3.선정 통계 조회");
			System.out.println("4.시스템 설정 메뉴"); //후순위 삭제
			System.out.println("5.권한 신청 관리"); //후순위 삭제
			System.out.println("6.로그아웃");
			System.out.print("입력 >");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {
					//1.전체 회원 관리 메뉴
					callUserMenu();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					
				}else if(no == 6) {
				
				}else {
					
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	//전체 회원 관리 메뉴
	private void callUserMenu()throws IOException{
		while(true) {
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.회원 목록 조회");
			System.out.println("2.회원 조건 검색");
			System.out.println("3.회원 상태 변경");
			System.out.println("4.회원 삭제");
			System.out.println("5.권한(역할 변경)");
			System.out.print("입력 >");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 0) {
					//0.이전 메뉴(뒤로가기)
					callMenu();
				}else if(no == 1) {
					//1.회원 목록 조회
					dao.selectUsers();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
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
		new USH_KRDAdminMain();
	}
}


