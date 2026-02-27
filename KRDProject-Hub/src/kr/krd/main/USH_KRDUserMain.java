package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.MemberDAO;

public class USH_KRDUserMain {
	private BufferedReader br;
	private MemberDAO dao;
	private USH_KRDAdminMain admMain = new USH_KRDAdminMain();
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)
	
	public USH_KRDUserMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
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
			System.out.println("===== 시스템 관리자 메뉴 =====");
			System.out.println();
			System.out.println("1.전체 회원 관리 메뉴");
			System.out.println("2.예산 관리 메뉴");
			System.out.println("3.선정 통계 조회");
			System.out.println("4.시스템 설정 메뉴"); //후순위 삭제
			System.out.println("5.권한 신청 관리"); //후순위 삭제
			System.out.println("6.로그아웃");
			System.out.println();
			System.out.print("입력 > ");
			try {
				//숫자 입력을 받아 메뉴 분기
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {
					//1.전체 회원 관리 메뉴
					System.out.println();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					
				}else if(no == 6) {
					//로그아웃 선택시 while문 종료
					System.out.println("시스템 관리자 계정에서 로그아웃합니다.");
					break;
				}else {
					//메뉴 범위 밖 숫자 입력
					System.out.println();
					System.out.println("잘못 입력했습니다.");
					System.out.println();
				}
			}catch(NumberFormatException e) {
				//숫자가 아닌 입력 처리
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	public static void main(String[] args) {
		new USH_KRDUserMain();
	}
}
