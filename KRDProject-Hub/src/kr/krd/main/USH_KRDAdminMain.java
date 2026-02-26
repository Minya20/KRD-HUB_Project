package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import kr.krd.dao.USH_MemberDAO; //취합시 MemberDAO로 변경해야하니 삭제

public class USH_KRDAdminMain {
	//콘솔 입력을 받기 위한 BufferedReader
	//Scanner보다 줄 단위 입력(br.readLine())이 편해서 사용
	private BufferedReader br;
	//DB 작업(조회/검색/삭제 등)을 담당하는 DAO
	//AdminMain은 '입력/흐름', DAO는 'DB 처리'로 역할 분리
	private USH_MemberDAO dao; //취합시 MemberDAO로 변경
	
	public USH_KRDAdminMain() {
		try {
			//표준 입력(System.in)을 BufferedReader로 감싼다
			br = new BufferedReader(new InputStreamReader(System.in));
			//DAO 생성
			dao = new USH_MemberDAO();
			//시스템 관리자 계정 진입시 최상위 메뉴를 호출
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리(프로그램 종료시 입력 스트림 닫기)
			if(br!=null)try {br.close();}catch(IOException e) {}
		}
	}
	//최상위(시스템 관리자) 메뉴
	private void callMenu()throws IOException{
		//while(true) : 사용자가 로그아웃 선택하기 전까지 계속 메뉴를 보여준다.
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
					callUserMenu();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					
				}else if(no == 6) {
					//로그아웃 서택시 while문 종료
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
	
	//전체 회원 관리 메뉴(서브 메뉴)
	private void callUserMenu()throws IOException{
		
		int restored = dao.restoreExpiredSuspendedUsers();
		if(restored > 0) {
			System.out.println("[알림] 만료된 정지 계정 " + restored + "건을 ACTIVE로 복구했습니다.");
		}
		
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
					//callMenu()를 다시 호출하면 메뉴가 중첩(재귀)될 수 있으니 return이 정석
					return;
				}else if(no == 1) {
					//1.회원 목록 조회
					System.out.println();
					dao.selectUsers();
					//목록 조회 후 상세조회 0/1 메뉴로 연결
					afterUserListMenu();
				}else if(no == 2) {
					//2.회원 조건 검색
					callUserSearch();
				}else if(no == 3) {
					//3.회원 상태 변경
					changeUserStatus();
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

	//회원 목록 조회/조건 검색 결과 후 회원 상세 조회
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
					//상세조회는 ID를 입력받아 DAO로 넘김
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
	
	//회원 조건 검색: 사용자가 조건을 입력하고 DAO로 넘김
	private void callUserSearch() throws IOException {
		System.out.println("===== 회원 조건 검색 =====");
		System.out.println("[안내] 엔터만 치면 해당 조건은 생략됩니다.");
		
		//문자열 조건들은 trim() 후 빈 문자열이면 "조건 없음"처리
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
		
		//날짜는 잘못 입력하면 ORA-01861 오류 발생
		//그래서 readDateOrEmpty()로 YYYY-MM-DD 형식을 강제 + 실제 날짜 검증까지 함
		String regStart = readDateOrEmpty("가입일 시작(YYYY-MM-DD) > ");
		String regEnd = readDateOrEmpty("가입일 끝(YYYY-MM-DD) > ");
		String lastStart = readDateOrEmpty("마지막접속 시작(YYYY-MM-DD) > ");
		String lastEnd = readDateOrEmpty("마지막접속 끝(YYYY-MM-DD) > ");
	
		//DAO는 입력값을 기반으로 동적 SQL을 만들어 조건 검색 실행 + 출력
		dao.searchUsers(id, name, email, role, status, regStart, regEnd, lastStart, lastEnd);
		
		//검색 후에 0/1 (뒤로/상세조회)연결하고 싶으면:
		afterUserListMenu();
	}
	//날짜 입력 헬퍼
	//엔터면 "" 반환(조건 생략)
	//형식이 틀리거나 존재하지 않는 날짜면 다시 입력받음
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
		//실수 방지를 위해 Y/N 확인을 올바르게 입력받을 때까지 반복
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
		
		
		
		//5) 실제 삭제(논리삭제) 실행(DAO)
		int result = dao.softDeleteUser(userId);
		
		if(result == 1) System.out.println("삭제 처리 완료되었습니다.");
		else System.out.println("삭제 실패(처리 중 오류 또는 조건 변경)");
	}
	
	//회원 상태(패널티 부여/해제) 변경
	private void changeUserStatus() throws IOException {
		System.out.println("================회원 상태 변경(패널티 부여/해제)===============");
		System.out.print("상태 변경할 사용자 ID 입력 > ");
		String userId = br.readLine().trim();//입력 공백 방지
		
		//빈 입력 방지
		if(userId.isEmpty()) {
			System.out.println("ID 입력은 필수입니다.");
			return;
		}
		
		//ID 존재 확인
		boolean chk = dao.existsUser(userId);
		
		
		
		if(!chk) {
			System.out.println("존재하지 않는 ID입니다.");
			return;
		}
		
		String status;
		
		//존재 확인 통과 상태 변경 기능 시작
		while(true) {
			System.out.print("변경할 상태 입력(ACTIVE/SUSPENDED) > ");
			status = br.readLine().trim().toUpperCase();
			
			if(status.equals("ACTIVE") || status.equals("SUSPENDED")) break;
			System.out.println("잘못된 입력입니다. ACTIVE 또는 SUSPENDED만 입력하세요.");
			
		}
		
		String current = dao.getAcctStatus(userId);
		
		if(current == null) {
			System.out.println("상태 조회 실패");
			return;
		}
		
		//DELETED일 경우 상태 변경 자체 금지
		if("DELETED".equalsIgnoreCase(current)) {
			System.out.println("이미 삭제된 계정입니다.(DELETED 상태는 변경 금지) 담당자에게 문의하십시오.");
			return;
		}
		
		//연장 모드인지 판단
		boolean isExtend = "SUSPENDED".equalsIgnoreCase(current) && "SUSPENDED".equals(status);
		if(isExtend) System.out.println("======기간 연장======");
		
		
		if("ACTIVE".equalsIgnoreCase(current) && "ACTIVE".equals(status)) {
			System.out.println("이미 ACTIVE한 상태입니다.");
			return;
		}
		
		//SUSPEDED 일 경우
		if(status.equals("SUSPENDED")) {
			
			if(isExtend) {
				//기간 연장 모드 : 종료일만 다시 받고, 기존 종료일보다 뒤여야 함.
				String oldEndStr = dao.getPenaltyEndDt(userId); //기존 종료일(YYYY-MM-DD) 또는 null
				String endStr = readDateOrRequired("연장할 패널티 종료일(YYYY-MM-DD) > ");
				LocalDate newEnd = LocalDate.parse(endStr);
				
				//종료일이 오늘보다 과거면 불가
				if(newEnd.isBefore(LocalDate.now())) {
					System.out.println("종료일이 오늘보다 이전입니다. 다시 입력하세요.");
					return;
				}
				
				//기존 종료일이 있으면, 새 종료일은 반드시 더 뒤여야 "연장"
				if(oldEndStr == null || oldEndStr.trim().isEmpty()) {
					System.out.println("기존 종료일이 없어 연장 비교를 생략합니다.");
				}else if(!oldEndStr.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
					System.out.println("기존 종료일 형식이 올바르지 않아 연장 비교를 생략합니다.");
				}else {
					LocalDate oldEnd = LocalDate.parse(oldEndStr.trim());
					if(!newEnd.isAfter(oldEnd)) {
						System.out.println("연장은 기존 종료일("+ oldEndStr.trim() + ") 이후 날짜로만 가능합니다.");
						return;
					}
				}
				
				while(true) {
					System.out.print("정말 변경하시겠습니까? (Y/N) >");
					String confirm = br.readLine().trim();
					
					if(confirm.equalsIgnoreCase("Y")) {
						break;
					}else if(confirm.equalsIgnoreCase("N")) {
						System.out.println("변경을 취소했습니다.");
						return;
					}else {
						System.out.println("잘못된 입력입니다. Y 또는 N만 입력하세요.");
					}
				}
				
				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) System.out.println("변경이 완료되었습니다.");
				else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");
				
			}else {
				String startStr = readDateOrRequired("패널티 시작일(YYYY-MM-DD) > ");
				String endStr = readDateOrRequired("패널티 종료일(YYYY-MM-DD) > ");
				
				LocalDate start = LocalDate.parse(startStr);
				LocalDate end = LocalDate.parse(endStr);
				
				if(start.isAfter(end)) {
					System.out.println("종료일이 시작일보다 날짜가 빠릅니다. 다시 입력하세요.");
					return;
				}
				
				if(start.isAfter(LocalDate.now())) {
					System.out.println("시작일이 오늘보다 미래입니다.(패널티는 입력시 바로 적용) 다시 입력하세요.");
					return;
				}
				
				if(end.isBefore(LocalDate.now())) {
					System.out.println("종료일이 오늘보다 과거입니다. 다시 입력하세요.");
					return;
				}
				
				while(true) {
					System.out.print("정말 변경하시겠습니까? (Y/N) >");
					String confirm = br.readLine().trim();
					
					if(confirm.equalsIgnoreCase("Y")) {
						break;
					}else if(confirm.equalsIgnoreCase("N")) {
						System.out.println("변경을 취소했습니다.");
						return;
					}else {
						System.out.println("잘못된 입력입니다. Y 또는 N만 입력하세요.");
					}
				}
				
				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) System.out.println("변경이 완료되었습니다.");
				else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");
			}
			
			
			
		}else {
		//ACTIVE일 경우
		//해결할 것 ACTIVE 상태에 ACTIVE로 변환하면 막아야함.
			
			
			while(true) {
				System.out.print("정말 변경하시겠습니까? (Y/N) >");
				String confirm = br.readLine().trim();
				
				if(confirm.equalsIgnoreCase("Y")) {
					break;
				}else if(confirm.equalsIgnoreCase("N")) {
					System.out.println("변경을 취소했습니다.");
					return;
				}else {
					System.out.println("잘못된 입력입니다. Y 또는 N만 입력하세요.");
				}
			}
			
			int result = dao.updateUserStatus(userId, "ACTIVE", null);
			
			
			if(result == 1) System.out.println("변경이 완료되었습니다.");
			else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");
		}
	}
	
	//회원 상태 변경 - 날짜 입력 헬퍼
	private String readDateOrRequired(String prompt) throws IOException {
		while(true) {
			System.out.print(prompt);
			String s = br.readLine().trim();
			
			if(s.isEmpty()) {
				System.out.println("날짜를 입력해주세요");
				continue;
			} //엔터(빈 값) 치면 다시 입력
			
			//1차 형식 체크
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				System.out.println("[날짜 형식 오류] YYYY-MM-DD로 입력하세요.");
				continue;
			}
			
			//2차 실제 날짜 체크 (e.g. 2026-02-30 같은 거 걸러냄)
			try {
				java.time.LocalDate.parse(s);
				return s;
			}catch (java.time.format.DateTimeParseException e) {
				System.out.println("[존재하지 않는 날짜] 다시 입력하세요.");
			}
		}
	}
	
	
	public static void main(String[] args) {
		new USH_KRDAdminMain();
	}
	

}


