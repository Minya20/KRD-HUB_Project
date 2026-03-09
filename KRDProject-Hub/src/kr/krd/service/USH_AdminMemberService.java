package kr.krd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import kr.krd.dao.USH_MemberDAO;
import kr.util.USH_ConsoleUtil;
import kr.krd.vo.USH_UserSummary;
import kr.krd.vo.USH_UserDetail;

public class USH_AdminMemberService {
	// ------------------------------------------------
	// ANSI Escape Codes & UI Elements
	// ------------------------------------------------
	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String UNBOLD = "\u001B[22m";

	public static final String CLR_PRIMARY = "\u001B[36m"; // Cyan
	public static final String CLR_WHT = "\u001B[37m";    // White
	public static final String CLR_GRY = "\u001B[90m";    // Gray
	public static final String CLR_ERR = "\u001B[31m";    // Red
	public static final String CLR_SUC = "\u001B[32m";    // Green

	private static final String INDENT = "      ";

	// ------------------------------------------------
	// Fields
	// ------------------------------------------------
	private final USH_ConsoleUtil io;
	private final USH_MemberDAO dao;
	private final String adminId;
	private static final Set<String> ALLOWED_ROLES = Set.of("GST","RESI","RESO","AGY","REV");
	private static final Set<String> ALLOWED_STATUS = Set.of("ACTIVE","SUSPENDED");

	public USH_AdminMemberService(BufferedReader br, String adminId) {
		this.io = new USH_ConsoleUtil(br);
		this.dao = new USH_MemberDAO();
		this.adminId = adminId;
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + RESET + msg);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + RESET + msg);
	}

	private String getInputPrompt(String tag) {
		return "\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ";
	}

	// ------------------------------------------------
	// Business Logic
	// ------------------------------------------------
	public int restoreExpiredSuspendedUsers() {
		return dao.restoreExpiredSuspendedUsers();
	}
	
	public void userListFlow() throws IOException {
		List<USH_UserSummary> list = dao.findUserExcludeDeleted();
		printUserSummaryList("회원 목록 전체 조회 (DELETED 제외)", list);
		afterUserListMenu();
	}
	
	// 회원 조건 검색
	public void userSearchFlow() throws IOException {
		printSubTitle("회원 조건 검색 (Search Users)");
		System.out.println(INDENT + CLR_GRY + "  [안내] 입력하지 않고 엔터(Enter)를 치면 해당 조건은 생략됩니다." + RESET);

		String id = io.readOptional(getInputPrompt("ID(부분일치)"));
		String name = io.readOptional(getInputPrompt("이름(부분일치)"));
		String email = io.readOptional(getInputPrompt("이메일(부분일치)"));
		String role = io.readOptional(getInputPrompt("권한 코드 (ADM/AGY/RESI/RESO/REV)"));
		String status = io.readOptional(getInputPrompt("상태 코드 (ACTIVE/SUSPENDED)"));
		
		if(role != null) role = role.toUpperCase();
		if(status != null) status = status.toUpperCase();

		System.out.println("\n" + INDENT + CLR_WHT + "[ 가입일 및 접속일 조건 ]" + RESET);
		String regStart = io.readDateOrEmpty(getInputPrompt("가입일 시작(YYYY-MM-DD)"));
		String regEnd = io.readDateOrEmpty(getInputPrompt("가입일 끝(YYYY-MM-DD)"));
		String lastStart = io.readDateOrEmpty(getInputPrompt("마지막접속 시작(YYYY-MM-DD)"));
		String lastEnd = io.readDateOrEmpty(getInputPrompt("마지막접속 끝(YYYY-MM-DD)"));

		List<USH_UserSummary> list = dao.searchUsers(id, name, email, role, status, regStart, regEnd, lastStart, lastEnd);
		printUserSummaryList("회원 조건 검색 결과", list);
		if(list != null && !list.isEmpty()) { 
			afterUserListMenu();
		}
	}
	
	// 회원 상태(패널티 부여/해제) 변경
	public void changeUserStatusFlow() throws IOException {
		printSubTitle("회원 상태 변경 (패널티 부여/해제)");
		String userId = io.readRequired(getInputPrompt("상태 변경할 사용자 ID 입력"));

		if(!dao.existsUser(userId)) {
			printError("존재하지 않는 ID입니다.");
			return;
		}

		String status = io.readFromSetRequired(getInputPrompt("변경할 상태 입력 (ACTIVE/SUSPENDED)"), ALLOWED_STATUS);
		String current = dao.getAcctStatus(userId);

		if(current == null) {
			printError("상태 조회 실패");
			return;
		}

		if("DELETED".equalsIgnoreCase(current)) {
			printError("이미 삭제된 계정입니다. (DELETED 상태는 변경 불가)");
			return;
		}

		boolean isExtend = "SUSPENDED".equalsIgnoreCase(current) && "SUSPENDED".equals(status);
		if(isExtend) {
			System.out.println("\n" + INDENT + CLR_PRIMARY + "[ 패널티 기간 연장 모드 ]" + RESET);
		}

		if("ACTIVE".equalsIgnoreCase(current) && "ACTIVE".equals(status)) {
			printError("이미 ACTIVE한 상태입니다.");
			return;
		}

		if(status.equals("SUSPENDED")) {
			if(isExtend) {
				String oldEndStr = dao.getPenaltyEndDt(userId); 
				String endStr = io.readDateRequired(getInputPrompt("연장할 패널티 종료일(YYYY-MM-DD)"));
				LocalDate newEnd = LocalDate.parse(endStr);

				if(newEnd.isBefore(LocalDate.now())) {
					printError("종료일이 오늘보다 이전입니다. 다시 시도하세요.");
					return;
				}

				if(oldEndStr == null || oldEndStr.trim().isEmpty()) {
					System.out.println(INDENT + CLR_GRY + "  (기존 종료일이 없어 연장 비교를 생략합니다.)" + RESET);
				} else if(!oldEndStr.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
					System.out.println(INDENT + CLR_GRY + "  (기존 종료일 형식이 올바르지 않아 연장 비교를 생략합니다.)" + RESET);
				} else {
					LocalDate oldEnd = LocalDate.parse(oldEndStr.trim());
					if(!newEnd.isAfter(oldEnd)) {
						printError("연장은 기존 종료일(" + oldEndStr.trim() + ") 이후 날짜로만 가능합니다.");
						return;
					}
				}

				if(!io.confirmYN(getInputPrompt("정말 변경하시겠습니까? (Y/N)"))) {
					printError("변경을 취소했습니다.");
					return;
				}

				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) printSuccess("상태 변경(기간 연장)이 완료되었습니다.");
				else printError("변경 실패 (처리 중 오류 발생)");

			} else {
				String startStr = io.readDateRequired(getInputPrompt("패널티 시작일(YYYY-MM-DD)"));
				String endStr = io.readDateRequired(getInputPrompt("패널티 종료일(YYYY-MM-DD)"));

				LocalDate start = LocalDate.parse(startStr);
				LocalDate end = LocalDate.parse(endStr);

				if(start.isAfter(end)) {
					printError("종료일이 시작일보다 빠를 수 없습니다.");
					return;
				}
				if(start.isAfter(LocalDate.now())) {
					printError("시작일이 오늘보다 미래일 수 없습니다. (패널티는 즉시 적용)");
					return;
				}
				if(end.isBefore(LocalDate.now())) {
					printError("종료일이 오늘보다 과거일 수 없습니다.");
					return;
				}

				if(!io.confirmYN(getInputPrompt("정말 변경하시겠습니까? (Y/N)"))) {
					printError("변경을 취소했습니다.");
					return;
				}

				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) printSuccess("상태 변경(패널티 부여)이 완료되었습니다.");
				else printError("변경 실패 (처리 중 오류 발생)");
			}
		} else {
			// ACTIVE로 변환
			if(!io.confirmYN(getInputPrompt("정말로 ACTIVE 상태로 변경하시겠습니까? (Y/N)"))) {
				printError("변경을 취소했습니다.");
				return;
			}

			int result = dao.updateUserStatus(userId, "ACTIVE", null);
			if(result == 1) printSuccess("계정이 ACTIVE 상태로 복구되었습니다.");
			else printError("변경 실패 (처리 중 오류 발생)");
		}
	}
	
	// 삭제 UI/흐름 처리
	public void deleteUserFlow() throws IOException {
		printSubTitle("회원 계정 삭제 (Delete User)");
		String userId = io.readRequired(getInputPrompt("삭제할 회원 ID 입력"));

		String chk = dao.canSoftDelete(userId);

		if("NOT_FOUND".equals(chk)) { printError("존재하지 않는 회원 ID입니다."); return; }
		if("ALREADY_DELETED".equals(chk)) { printError("이미 삭제된 계정입니다."); return; }
		if("ADMIN_BLOCK".equals(chk)) { printError("관리자(ADM) 계정은 삭제할 수 없습니다."); return; }
		if("ERROR".equals(chk)) { printError("삭제 검증 중 DB 오류가 발생했습니다."); return; }

		System.out.println(INDENT + CLR_ERR + "  ※ 경고: 삭제 시 계정 상태가 DELETED로 변경되며 복구가 불가능할 수 있습니다." + RESET);
		if(!io.confirmYN(getInputPrompt("정말 삭제하시겠습니까? (Y/N)"))) {
			printError("삭제 처리를 취소했습니다.");
			return;
		}

		int result = dao.softDeleteUser(userId);
		if(result == 1) printSuccess("해당 계정이 성공적으로 삭제(DELETED) 되었습니다.");
		else printError("삭제 실패 (처리 중 오류 발생)");
	}
	
	// 권한 변경
	public void roleChangeFlow() throws IOException {
		printSubTitle("회원 권한(역할) 변경 (Change Role)");
		String userId = io.readRequired(getInputPrompt("권한을 변경할 대상 ID 입력"));

		String[] info = dao.getRoleAndStatus(userId);
		if(info == null) {
			printError("존재하지 않는 ID입니다.");
			return;
		}

		String currentRole = info[0];
		String status = info[1];

		if(!(status == null || "ACTIVE".equalsIgnoreCase(status))) {
			printError("계정 상태가 ACTIVE가 아니면 권한을 변경할 수 없습니다.");
			return;
		}

		System.out.println(INDENT + CLR_GRY + "  현재 권한: " + RESET + currentRole);

		String newRole = null;
		while(true) {
			newRole = io.readFromSetRequired(getInputPrompt("변경할 권한(역할) 입력 (예: AGY, REV)"), ALLOWED_ROLES);
			if(newRole.equalsIgnoreCase(currentRole)) {
				printError("현재 보유 중인 권한과 동일합니다. 다른 권한을 입력하세요.");
				continue;
			}
			break;
		}

		String changedBy = adminId; 
		String reason = io.readOptional(getInputPrompt("변경 사유 (엔터=생략)"));

		if(!io.confirmYN(getInputPrompt("정말로 권한을 [" + currentRole + " -> " + newRole + "]로 변경하시겠습니까? (Y/N)"))) {
			printError("권한 변경을 취소하였습니다.");
			return;
		}

		int result = dao.changeUserRoleWithHistory(userId, newRole, changedBy, reason);
		if(result == 1) printSuccess("권한(역할) 변경 처리가 완료되었습니다.");
		else printError("권한 변경 실패 (계정 상태 변경 또는 DB 오류)");
	}
	
	// 회원 목록 조회/조건 검색 결과 후 회원 상세 조회
	private void afterUserListMenu() throws IOException {
		while (true) {
			System.out.println();
			printMenuOption("1", "특정 회원 상세 정보 조회");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "목록 닫기 및 이전 메뉴로 돌아가기");
			System.out.println();

			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 1);
			if (no == 0) return;

			String userId = io.readRequired(getInputPrompt("상세 조회할 회원 ID 입력"));
			USH_UserDetail detail = dao.findUserDetail(userId);

			if (detail == null) {
				printError("해당 ID의 회원을 찾을 수 없습니다: " + userId);
				continue;
			}
			printUserDetail(detail);
		}
	}
	
	// 회원 목록 출력 (Grid Layout 적용)
	private void printUserSummaryList(String title, List<USH_UserSummary> list) {
		printSubTitle(title);

		if (list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조건에 맞는 회원 정보가 없습니다." + RESET);
			return;
		}

		// 표 헤더 생성 (문자열 길이에 맞춰 포맷팅)
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-12s | %-8s | %-12s | %-22s | %-6s | %-10s | %-19s | %-19s",
				"ID", "이름", "생년월일", "이메일", "권한", "상태", "가입일자", "마지막접속") + RESET);
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------------------------------" + RESET);

		for (USH_UserSummary u : list) {
			String stat = nvl(u.acctStatusCd);
			String statColor = stat.equals("ACTIVE") ? CLR_SUC : (stat.equals("SUSPENDED") ? CLR_ERR : CLR_GRY);
			
			System.out.println(INDENT + String.format("%-12s | %-8s | %-12s | %-22s | %-6s | " + statColor + "%-10s" + RESET + " | %-19s | %-19s",
					nvl(u.userId),
					nvl(u.userName),
					nvl(u.birthDt),
					nvl(u.email),
					nvl(u.roleCd),
					stat,
					fmtTs(u.createdAt),
					fmtTs(u.lastLoginAt)));
		}
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------------------------------" + RESET);
	}
	
	// 회원 상세 조회 출력 (Profile Card Layout)
	private void printUserDetail(USH_UserDetail d) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ 회원 상세 정보 (User Details)" + RESET);
		System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────┐" + RESET);
		
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "ID", nvl(d.userId));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "이름", nvl(d.userName));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "생년월일", nvl(d.birthDt));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "이메일", nvl(d.email));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "전화번호", nvl(d.phoneNo));
		System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "소속", nvl(d.affiliation));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "담당 분야", nvl(d.field));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "권한(역할)", nvl(d.roleCd));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "계정 상태", nvl(d.acctStatusCd));
		if("SUSPENDED".equals(d.acctStatusCd)) {
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_ERR + "%-12s : %s\n" + RESET, "패널티 종료일", nvl(d.penaltyEndDt));
		}
		System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "국적", nvl(d.countryCd));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "주소", nvl(d.addr));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "성별", nvl(d.genderCd));
		System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "가입 일자", fmtTs(d.createdAt));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "마지막 접속", fmtTs(d.lastLoginAt));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "업데이트 일시", fmtTs(d.updatedAt));
		
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────┘" + RESET);
	}
	
	// 유틸리티 메서드
	private String nvl(String s) {
		return (s == null || s.isBlank()) ? "-" : s;
	}
	
	private String fmtTs(java.sql.Timestamp ts) {
		if (ts == null) return "-";
		return ts.toLocalDateTime().toString().replace('T', ' ').substring(0, 19); // 밀리초 제거
	}
}