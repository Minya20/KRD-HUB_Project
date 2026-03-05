package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.krd.service.USH_AdminMemberService;
import kr.util.USH_ConsoleUtil;
import kr.krd.service.USH_AdminBudgetService;
import kr.krd.service.USH_AdminStatsService;
import kr.krd.service.USH_AdminAnnouncementService;
import kr.krd.service.USH_AdminRoleApplicationService;

public class USH_KRDAdminMain {
	//콘솔 입력을 받기 위한 BufferedReader
	//Scanner보다 줄 단위 입력(br.readLine())이 편해서 사용
	private final BufferedReader br;
	private final USH_AdminMemberService service;
	private final USH_ConsoleUtil io;
	private final USH_AdminBudgetService budgetService;
	private final USH_AdminStatsService statsService;
	private final USH_AdminAnnouncementService annService;
	private final USH_AdminRoleApplicationService roleAppService;

	public USH_KRDAdminMain(BufferedReader br, String adminId) {
		this.br = br;
		this.service = new USH_AdminMemberService(br, adminId);
		this.budgetService = new USH_AdminBudgetService(br, adminId);
		this.statsService = new USH_AdminStatsService(br, adminId);
		this.annService = new USH_AdminAnnouncementService(br, adminId);
		this.io = new USH_ConsoleUtil(br);
		this.roleAppService = new USH_AdminRoleApplicationService(br, adminId);

		//adminId는 나중에 changedBy 같은데 쓰려고 저장해둠
		//this.adminId = adminId;

		try {
			//시스템 관리자 계정 진입시 최상위 메뉴를 호출
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//여기서는 br 닫지 않음(br은 UserMain 소유)
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
			System.out.println("4.게시물 관리 메뉴"); //후순위 삭제
			System.out.println("5.권한 신청 관리"); //후순위 삭제
			System.out.println("6.로그아웃");
			System.out.println();

			int no = io.readIntInRange("입력 > ", 1, 6);

			if(no == 1) {
				//1.전체 회원 관리 메뉴
				System.out.println();
				callUserMenu();
			}else if(no == 2) {
				callBudgetMenu();
			}else if(no == 3) {
				callStatsMenu();
			}else if(no == 4) {
				callAnnouncementMenu();
			}else if(no == 5) {
				callRoleMenu();
			}else if(no == 6) {
				//로그아웃 서택시 while문 종료
				System.out.println("시스템 관리자 계정에서 로그아웃합니다.");
				return;
			}else {
				//메뉴 범위 밖 숫자 입력
				System.out.println();
				System.out.println("잘못 입력했습니다.");
				System.out.println();
			}

		}
	}

	//전체 회원 관리 메뉴(서브 메뉴)
	private void callUserMenu()throws IOException{

		int restored = service.restoreExpiredSuspendedUsers();
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

			int no = io.readIntInRange("입력 > ", 0, 5);

			if(no == 0) {
				//0.이전 메뉴(뒤로가기)
				//callMenu()를 다시 호출하면 메뉴가 중첩(재귀)될 수 있으니 return이 정석
				return;
			}else if(no == 1) {
				//1.회원 목록 조회
				System.out.println();
				service.userListFlow();
			}else if(no == 2) {
				//2.회원 조건 검색
				service.userSearchFlow();
			}else if(no == 3) {
				//3.회원 상태 변경
				service.changeUserStatusFlow();
			}else if(no == 4) {
				//4.회원 삭제
				service.deleteUserFlow();
			}else if(no == 5) {
				//5.권한(역할) 변경
				service.roleChangeFlow();
			}else {
				System.out.println("잘못 입력했습니다.");
			}

		}
	}
	
	//예산 관리 메뉴(서브 메뉴)
	private void callBudgetMenu() throws IOException{
		while(true) {
			System.out.println("===== 예산 관리 =====");
			System.out.println();
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.예산 변경 이력 조회");
			System.out.println("2.예산 사용 현황 조회");
			System.out.println();
			
			int no = io.readIntInRange("입력 > ", 0, 2);
			
			if(no == 0) {
				return; //시스템 관리자 메뉴로 복귀
			}else if(no == 1) {
				budgetService.budgetHistFlow();
			}else if(no == 2) {
				budgetService.budgetUsageFlow();
			}else {
				System.out.println("잘못 입력했습니다.");
			}
		}
	}
	
	//선정 통계 조회(서브 메뉴)
	private void callStatsMenu() throws IOException {
		while(true) {
			System.out.println("========== 선정 통계 조회 ==========");
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.연도별 선정 건수");
			System.out.println("2.기관별 선정 건수");
			System.out.println("3.평균 경쟁률 조회");
			System.out.println();
			
			int no = io.readIntInRange("입력 > ", 0, 3);
			
			if(no == 0) {	//뒤로 가기
				return;
			}else if(no == 1) {	//연도별 선정 건수
				statsService.selectedByYearFlow();
			}else if(no == 2) {	//기관별 선정 건수
				statsService.selectedByAgencyFlow();
			}else if(no == 3) {	//평균 경쟁률 조회
				statsService.avgCompetitionRateFlow();
			}else {
				System.out.println("잘못 입력했습니다.");
			}
		}
	}

	//공고(게시물) 관리 메뉴(서브 메뉴)
	private void callAnnouncementMenu() throws IOException {
		while(true) {
			System.out.println("===== 시스템 설정(게시물 관리) =====");
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.공고 상태 강제 변경");
			System.out.println();
			
			int no = io.readIntInRange("입력 > ", 0, 1);
			
			if(no == 0) return;
			if(no == 1) annService.forceChangeAnnStatusFlow();
					
		}
	}
	
	//권한 신청 관리 메뉴(서브 메뉴)
	private void callRoleMenu() throws IOException {
		while(true) {
			System.out.println("===== 권한 신청 관리 =====");
			System.out.println();
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.신청 목록 조회");
			System.out.println();
			
			int no = io.readIntInRange("입력 > ", 0, 1);
			
			if(no == 0) return;
			if(no == 1) roleAppService.pendingListFlow();
		}
	}
}


