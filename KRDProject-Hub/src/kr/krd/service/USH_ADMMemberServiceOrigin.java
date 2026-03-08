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

public class USH_ADMMemberServiceOrigin {
	private final USH_ConsoleUtil io;
	private final USH_MemberDAO dao;
	private final String adminId;
	private static final Set<String> ALLOWED_ROLES = Set.of("GST","RESI","RESO","AGY","REV");
	private static final Set<String> ALLOWED_STATUS = Set.of("ACTIVE","SUSPENDED");

	
	public USH_ADMMemberServiceOrigin(BufferedReader br, String adminId) {
		this.io = new USH_ConsoleUtil(br);
		this.dao = new USH_MemberDAO();
		this.adminId = adminId;
		
	}
	
	public int restoreExpiredSuspendedUsers() {
		return dao.restoreExpiredSuspendedUsers();
	}
	
	public void userListFlow() throws IOException {
		List<USH_UserSummary> list = dao.findUserExcludeDeleted();
		printUserSummaryList("회원 목록 조회 (DELETED 제외)", list);
		afterUserListMenu();
	}
	
	//회원 조건 검색: 사용자가 조건을 입력하고 DAO로 넘김
	public void userSearchFlow() throws IOException {
		System.out.println("===== 회원 조건 검색 =====");
		System.out.println("[안내] 엔터만 치면 해당 조건은 생략됩니다.");

		//문자열 조건들은 trim() 후 빈 문자열이면 "조건 없음"처리
		String id = io.readOptional("ID(부분일치) > ");
		String name = io.readOptional("이름(부분일치) > ");
		String email = io.readOptional("이메일(부분일치) > ");
		String role = io.readOptional("권한 코드(예: ADM/AGY/RESI/RESO/REV) > ");
		String status = io.readOptional("상태 코드(예: ACTIVE / (빈값 허용)) > ");
		
		if(role != null) role = role.toUpperCase();
		if(status != null) status = status.toUpperCase();

		//날짜는 잘못 입력하면 ORA-01861 오류 발생
		//그래서 readDateOrEmpty()로 YYYY-MM-DD 형식을 강제 + 실제 날짜 검증까지 함
		String regStart = io.readDateOrEmpty("가입일 시작(YYYY-MM-DD) > ");
		String regEnd = io.readDateOrEmpty("가입일 끝(YYYY-MM-DD) > ");
		String lastStart = io.readDateOrEmpty("마지막접속 시작(YYYY-MM-DD) > ");
		String lastEnd = io.readDateOrEmpty("마지막접속 끝(YYYY-MM-DD) > ");

		//DAO는 입력값을 기반으로 동적 SQL을 만들어 조건 검색 실행 + 출력
		List<USH_UserSummary> list = dao.searchUsers(id, name, email, role, status, regStart, regEnd, lastStart, lastEnd);
		printUserSummaryList("회원 조건 검색 결과", list);
		afterUserListMenu();
	}
	
	//회원 상태(패널티 부여/해제) 변경
	public void changeUserStatusFlow() throws IOException {
		System.out.println("================회원 상태 변경(패널티 부여/해제)===============");
		String userId = io.readRequired("상태 변경할 사용자 ID 입력 > ");

		//ID 존재 확인
		boolean chk = dao.existsUser(userId);

		if(!chk) {
			System.out.println("존재하지 않는 ID입니다.");
			return;
		}


		String status = io.readFromSetRequired("변경할 상태 입력(ACTIVE/SUSPENDED) > ", ALLOWED_STATUS);

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
				String endStr = io.readDateRequired("연장할 패널티 종료일(YYYY-MM-DD) > ");
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

				if(!io.confirmYN("정말 변경하시겠습니까? (Y/N) > ")) {
					System.out.println("변경을 취소했습니다.");
					return;
				}

				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) System.out.println("변경이 완료되었습니다.");
				else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");

			}else {
				String startStr = io.readDateRequired("패널티 시작일(YYYY-MM-DD) > ");
				String endStr = io.readDateRequired("패널티 종료일(YYYY-MM-DD) > ");

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

				if(!io.confirmYN("정말 변경하시겠습니까? (Y/N) > ")) {
					System.out.println("변경을 취소했습니다.");
					return;
				}

				int result = dao.updateUserStatus(userId, "SUSPENDED", endStr);
				if(result == 1) System.out.println("변경이 완료되었습니다.");
				else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");
			}



		}else {
			//ACTIVE일 경우
			//해결할 것 ACTIVE 상태에 ACTIVE로 변환하면 막아야함.

			if(!io.confirmYN("정말 변경하시겠습니까? (Y/N) > ")) {
				System.out.println("변경을 취소했습니다.");
				return;
			}

			int result = dao.updateUserStatus(userId, "ACTIVE", null);


			if(result == 1) System.out.println("변경이 완료되었습니다.");
			else System.out.println("변경 실패(처리 중 오류 또는 조건 변경)");
		}

	}
	
	//삭제 UI/흐름 처리
	public void deleteUserFlow() throws IOException {
		//1) 삭제할 회원 ID 입력
		String userId = io.readRequired("삭제할 회원 ID 입력 > ");

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
		if(!io.confirmYN("정말 삭제(계정 상태 = DELETED)하시겠습니까? (Y/N) > ")) {
			System.out.println("삭제를 취소했습니다.");
			return;
		}

		//5) 실제 삭제(논리삭제) 실행(DAO)
		int result = dao.softDeleteUser(userId);

		if(result == 1) System.out.println("삭제 처리 완료되었습니다.");
		else System.out.println("삭제 실패(처리 중 오류 또는 조건 변경)");
	}
	
	//권한 변경
	public void roleChangeFlow() throws IOException {
		System.out.println("======권한(역할) 변경======");
		String userId = io.readRequired("권한을 변경할 대상 ID 입력 > ");

		String[] info = dao.getRoleAndStatus(userId);

		if(info == null) {
			System.out.println("존재하지 않는 ID입니다.");
			return;
		}

		String currentRole = info[0];
		String status = info[1];

		if(!(status == null || "ACTIVE".equalsIgnoreCase(status))) {
			System.out.println("계정 상태가 ACTIVE가 아니면 권한(역할)을 변경할 수 없습니다.");
			return;
		}

		String newRole = null;
		while(true) {
			newRole = io.readFromSetRequired("바꾸고 싶은 권한(역할) 입력 > ", ALLOWED_ROLES);

			if(newRole.equalsIgnoreCase(currentRole)) {
				System.out.println("현재 갖고 있는 권한(역할)입니다.");
				continue;
			}

			if(!newRole.equalsIgnoreCase(currentRole)) {
				break;
			}
		}

		String changedBy = adminId;// 이후 로그인한 관리자 ID로 교체
		String reason = io.readOptional("변경 사유(엔터=생략) > ");

		if(!io.confirmYN("정말로 권한(역할)을 변경하시겠습니까? (Y/N) > ")) {
			System.out.println("권한 변경을 취소하였습니다.");
			return;
		}

		int result = dao.changeUserRoleWithHistory(userId, newRole, changedBy, reason);
		if(result == 1) System.out.println("권한(역할)을 변경하였습니다.");
		else System.out.println("권한 변경 실패.(계정 상태/DB 오류)");

	}
	
	//회원 목록 조회/조건 검색 결과 후 회원 상세 조회
	private void afterUserListMenu() throws IOException {
	    while (true) {
	        System.out.println();
	        System.out.println("0.이전 메뉴(뒤로가기)");
	        System.out.println("1.회원 상세 조회");
	        System.out.println();

	        int no = io.readIntInRange("입력 > ", 0, 1);

	        if (no == 0) return;

	        // no == 1
	        String userId = io.readRequired("조회할 회원 ID 입력 > ");
	        USH_UserDetail detail = dao.findUserDetail(userId);

	        if (detail == null) {
	            System.out.println("해당 ID의 회원이 없습니다: " + userId);
	            continue;
	        }
	        printUserDetail(detail);
	    }
	}
	
	//회원 목록 조회 출력
	private void printUserSummaryList(String title, List<USH_UserSummary> list) {
	    System.out.println("========== " + title + " ==========");
	    System.out.println();

	    if (list == null || list.isEmpty()) {
	        System.out.println("조건에 맞는 회원 정보가 없습니다.");
	        System.out.println("-".repeat(130));
	        return;
	    }

	    System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n",
	            "ID", "이름", "생년월일", "이메일", "권한", "상태", "가입일자", "마지막접속");
	    System.out.println("-".repeat(130));

	    for (USH_UserSummary u : list) {
	        System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n",
	                nvl(u.userId),
	                nvl(u.userName),
	                nvl(u.birthDt),
	                nvl(u.email),
	                nvl(u.roleCd),
	                nvl(u.acctStatusCd),
	                fmtTs(u.createdAt),
	                fmtTs(u.lastLoginAt));
	    }
	    System.out.println("-".repeat(130));
	}
	
	//회원 상세 조회 출력
	private void printUserDetail(USH_UserDetail d) {
	    System.out.println();
	    System.out.println("===================회원 상세 조회===================");
	    System.out.println("ID : " + nvl(d.userId));
	    System.out.println("이름 : " + nvl(d.userName));
	    System.out.println("생년월일 : " + nvl(d.birthDt));
	    System.out.println("이메일 : " + nvl(d.email));
	    System.out.println("전화번호 : " + nvl(d.phoneNo));
	    System.out.println("국적 : " + nvl(d.countryCd));
	    System.out.println("주소 : " + nvl(d.addr));
	    System.out.println("성별 : " + nvl(d.genderCd));
	    System.out.println("권한 : " + nvl(d.roleCd));
	    System.out.println("계정 상태 : " + nvl(d.acctStatusCd));
	    System.out.println("가입일자 : " + fmtTs(d.createdAt));
	    System.out.println("마지막 접속 : " + fmtTs(d.lastLoginAt));
	    System.out.println("패널티 종료일 : " + nvl(d.penaltyEndDt));
	    System.out.println("소속 : " + nvl(d.affiliation));
	    System.out.println("담당 분야 : " + nvl(d.field));
	    System.out.println("업데이트 일시 : " + fmtTs(d.updatedAt));
	    System.out.println("-".repeat(50));
	}
	
	//nvl() -> DB에서 NULL로 넘어오는 값은 출력하면 null로 보이기 때문에 보기 안 좋음.
		//		   그래서 NULL/빈 문자열이면 -로 통일해서 출력.
	private String nvl(String s) {
	    return (s == null || s.isBlank()) ? "-" : s;
	}
	
	//fmtTs() -> getTimestamp()로 가져오면 날짜+시간까지 있음
		//			 Timestamp.toString()도 되지만, LocalDateTime으로 바꿔서 보기 좋게 만드는 방식. NULL이면 -.
	private String fmtTs(java.sql.Timestamp ts) {
	    if (ts == null) return "-";
	    return ts.toLocalDateTime().toString().replace('T', ' ');
	}
	
}
