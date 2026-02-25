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
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {
					//1.전체 회원 관리 메뉴
					System.out.println();
					callUserMenu();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					
				}else if(no == 6) {
					System.out.println("시스템 관리자 계정에서 로그아웃합니다.");
					break;
				}else {
					System.out.println();
					System.out.println("잘못 입력했습니다.");
					System.out.println();
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
			System.out.println();
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.회원 목록 조회");
			System.out.println("2.회원 조건 검색");
			System.out.println("3.회원 상태 변경");
			System.out.println("4.회원 삭제");
			System.out.println("5.권한(역할) 변경");
			System.out.println();
			System.out.print("입력 >");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 0) {
					//0.이전 메뉴(뒤로가기)
					return;
				}else if(no == 1) {
					//1.회원 목록 조회
					System.out.println();
					dao.selectUsers();
					//목록 조회 후 0/1 메뉴로 연결
					afterUserListMenu();
				}else if(no == 2) {
					//2.회원 조건 검색
					callUserSearch();
				}else if(no == 3) {
					//3.회원 상태 변경
				}else if(no == 4) {
					//4.회원 삭제
					handleUserDelete();
				}else if(no == 5) {
					//5.권한(역할) 변경
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
			System.out.println();
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.회원 상세 조회");
			System.out.println();
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
		
		System.out.print("ID(부분일치) > ");
		String id = br.readLine().trim();
		
		System.out.print("이름(부분일치) > ");
		String name = br.readLine().trim();
		
		System.out.print("이메일(부분일치) > ");
		String email = br.readLine().trim();
		
		System.out.print("권한 코드(예: ADM/AGY/RESI/RESO/REV) > ");
		String role = br.readLine().trim();
		
		System.out.print("상태 코드(예: ACTIVE / (빈값 허용)) > ");
		String status = br.readLine().trim();
		
		String regStart = readDateOrEmpty("가입일 시작(YYYY-MM-DD) > ");
		String regEnd = readDateOrEmpty("가입일 끝(YYYY-MM-DD) > ");
		String lastStart = readDateOrEmpty("마지막접속 시작(YYYY-MM-DD) > ");
		String lastEnd = readDateOrEmpty("마지막접속 끝(YYYY-MM-DD) > ");
	
		
		dao.searchUsers(id, name, email, role, status, regStart, regEnd, lastStart, lastEnd);
		
		//검색 후에 0/1 (뒤로/상세조회)연결하고 싶으면:
		afterUserListMenu();
	}
	
	private String readDateOrEmpty(String prompt) throws IOException {
		while(true) {
			System.out.print(prompt);
			String s = br.readLine().trim();
			
			if(s.isEmpty()) return ""; //엔터면 조건 생략
			
			//1차 형식 체크
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				System.out.println("[날짜 형식 오류] YYYY-MM-DD로 입력하세요. (또는 엔터로 생략)");
				continue;
			}
			
			//2차 실제 날짜 체크 (e.g. 2026-02-30 같은 거 걸러냄)
			try {
				java.time.LocalDate.parse(s);
				return s;
			}catch (java.time.format.DateTimeParseException e) {
				System.out.println("[존재하지 않는 날짜] 다시 입력하세요. (또는 엔터로 생략)");
			}
		}
	}
	
	//삭제 UI/흐름 처리
	private void handleUserDelete() throws IOException{
		//1) 삭제할 회원 ID 입력
		System.out.print("삭제할 회원 ID 입력 > ");
		String userId = br.readLine().trim();
		
		//빈 입력 방지
		if(userId.isEmpty()) {
			System.out.println("ID 입력은 필수입니다.");
			return;
		}
		
		//2) DB에서 삭제 가능한 상태인지 먼저 판단
		String chk = dao.canSoftDelete(userId);
		
		//3) 판단 결과에 따라 메시지 출력하고 종료(삭제 수행x)
		if("NOT_FOUND".equals(chk)) {
			System.out.println("삭제 실패: 존재하지 않는 회원 ID");
			return;
		}
		if("ALREADY_DELETED".equals(chk)) {
			System.out.println("삭제 실패: 이미 삭제된 계정입니다.");
			return;
		}
		if("ADMIN_BLOCK".equals(chk)) {
			System.out.println("삭제 실패: 관리자(ADM) 계정은 삭제할 수 없습니다. 담당자에게 문의하십시오.");
			return;
		}
		if("ERROR".equals(chk)) {
			System.out.println("삭제 실패: DB 오류");
			return;
		}
		
		//4) 여기가지 통과했으면 삭제 가능한 상태
		while(true) {
			System.out.print("정말 삭제(계정상태 = DELETED)하시겠습니까? (Y/N) > ");
			String confirm = br.readLine().trim();
			
			if(confirm.equalsIgnoreCase("Y")) {
				break; //삭제 진행(루프 탈출)
			}else if(confirm.equalsIgnoreCase("N")) {
				System.out.println("삭제를 취소했습니다.");
				return; //삭제 메뉴 종료
			}else {
				System.out.println("잘못된 입력입니다. Y 또는 N만 입력하세요.");
			}
			
		}
		
		
		
		//5) 실제 삭제(논리삭제) 실행
		int result = dao.softDeleteUser(userId);
		
		if(result == 1) System.out.println("삭제 처리 완료.");
		else System.out.println("삭제 실패(처리 중 오류 또는 조건 변경)");
	}
	
	public static void main(String[] args) {
		new USH_KRDAdminMain();
	}
}


