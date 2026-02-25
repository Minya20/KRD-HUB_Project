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
			System.out.println("===== 전체 회원 관리 =====");
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
					return;
				}else if(no == 1) {
					//1.회원 목록 조회
					dao.selectUsers();
					//목록 조회 후 0/1 메뉴로 연결
					afterUserListMenu();
				}else if(no == 2) {
					//2.회원 조건 검색
					callUserSearch();
				}else if(no == 3) {
					
				}else if(no == 4) {
				
				}else if(no == 5) {
					
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}

	//회원 목록 조회 후 회원 상세 조회
	private void afterUserListMenu() throws IOException {
		while (true) {
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.회원 상세 조회");
			System.out.print("입력 >");
			try {
				int no = Integer.parseInt(br.readLine());
				
				if(no == 0) {
					return; //회원 관리 메뉴로 복귀
				}else if(no == 1){
					System.out.print("조회할 회원 ID 입력 >");
					String userId = br.readLine();
					dao.selectUserDetail(userId);//상세 조회 호출
				}else {
					System.out.println("잘못 입력했습니다. (0 또는 1)");
				}
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	//회원 조건 검색
	private void callUserSearch() throws IOException {
		System.out.println("===== 회원 조건 검색 =====");
		System.out.println("[안내] 엔터만 치면 해당 조건은 생략됩니다.");
		
		System.out.print("ID(부분일치) >");
		String id = br.readLine().trim();
		
		System.out.print("이름(부분일치) >");
		String name = br.readLine().trim();
		
		System.out.print("이메일(부분일치) >");
		String email = br.readLine().trim();
		
		System.out.print("권한 코드(예: ADM/AGY/RESI/RESO/REV) >");
		String role = br.readLine().trim();
		
		System.out.print("상태 코드(예: ACTIVE / (빈값 허용)) >");
		String status = br.readLine().trim();
		
		System.out.print("가입일 시작(YYYY-MM-DD) >");
		String regStart = br.readLine().trim();
		
		System.out.print("가입일 끝(YYYY-MM-DD) >");
		String regEnd = br.readLine().trim();
		
		System.out.print("마지막접속 시작(YYYY-MM-DD) >");
		String lastStart = br.readLine().trim();

		System.out.print("마지막접속 끝(YYYY-MM-DD) >");
		String lastEnd = br.readLine().trim();
		
		dao.searchUsers(id, name, email, role, status, regStart, regEnd, lastStart, lastEnd);
		
		//검색 후에 0/1 (뒤로/상세조회)연결하고 싶으면:
		afterUserListMenu();
	}
	
	
	public static void main(String[] args) {
		new USH_KRDAdminMain();
	}
}


