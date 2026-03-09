package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import kr.util.DBUtil;

public class HYJ_KRDRESOUserDAO {

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

	// 기존 객체 유지
	public HJY_CheckSystem dao1 = new HJY_CheckSystem();
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printInputTag(String tag) {
		System.out.print("\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ");
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + RESET + msg);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + RESET + msg);
	}

	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 2) + "..";
	}

	// ===========================
	// 공고 목록 (Grid Layout 적용)
	// ===========================
	public void selectAnn(String cust_id) {

		while (true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				conn = DBUtil.getConnection();

				String sql = "SELECT * FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_HIDDEN_YN = 0 ORDER BY ANNOUNCEMENT_ANN_ID";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();

				printSubTitle("연구과제 공고 목록 조회 (Announcements)");

				if (!rs.next()) {
					System.out.println(INDENT + CLR_GRY + "  현재 등록된 공고가 없습니다." + RESET);
					return;
				}

				// 표 헤더 생성 (String.format 기반 고정폭)
				System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-6s | %-28s | %15s | %-12s | %-12s", 
						"번호", "공고명", "총예산(원)", "시작일", "종료일") + RESET);
				System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------" + RESET);

				do {
					System.out.println(INDENT + String.format("%-8d | %-28s | %15s | %-12s | %-12s", 
							rs.getInt("ANNOUNCEMENT_ANN_ID"),
							cut(rs.getString("ANNOUNCEMENT_TITLE"), 26), // 제목이 너무 길면 줄임표 처리
							String.format("%,d", rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET")),
							rs.getString("ANNOUNCEMENT_START_DT"),
							rs.getString("ANNOUNCEMENT_END_DT")));
				} while (rs.next());

				System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------" + RESET);
				
				System.out.println();
				printMenuOption("1", "공고 상세 조회 및 신청");
				printMenuOption("2", "이전 화면으로 돌아가기");

				printInputTag("메뉴 선택");
				int sel = Integer.parseInt(br.readLine());
				
				if (sel == 1) {
					try {
						printInputTag("상세조회할 공고 번호 입력");
						int annId = Integer.parseInt(br.readLine());
						detailAnn(annId, cust_id);   // 상세 메서드 분리
					} catch(NumberFormatException e) {
						printError("숫자만 입력 가능합니다.");
					}
				} else if (sel == 2) {
					return;   // 이전 화면으로 복귀
				} else {
					printError("잘못된 선택입니다.");
				}

			} catch(NumberFormatException e) {
				printError("숫자를 입력하세요.");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}

	// ===========================
	// 공고 상세보기 (Profile Card Layout 적용)
	// ===========================
	private void detailAnn(int annId, String cust_id) {

		while (true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				conn = DBUtil.getConnection();

				String sql = "SELECT * FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_ANN_ID = ?"
						+ " AND ANNOUNCEMENT_HIDDEN_YN = 0";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, annId);
				rs = pstmt.executeQuery();

				if (!rs.next()) {
					printError("해당 공고가 존재하지 않습니다.");
					return;
				}

				printSubTitle("공고 상세 정보 (Announcement Details)");
				System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
				
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고번호", rs.getInt("ANNOUNCEMENT_ANN_ID"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고명", rs.getString("ANNOUNCEMENT_TITLE"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "기관번호", rs.getString("ANNOUNCEMENT_AGY_ID"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "모집분야", rs.getString("ANNOUNCEMENT_FIELD"));
				
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
				
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "접수시작일", rs.getString("ANNOUNCEMENT_START_DT"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "접수종료일", rs.getString("ANNOUNCEMENT_END_DT"));
				
				String status = rs.getString("ANNOUNCEMENT_STATUS");
				String statColor = "공고중".equals(status) ? CLR_SUC : ("마감".equals(status) ? CLR_ERR : CLR_PRIMARY);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + statColor + "%s\n" + RESET, "공고상태", status);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%,d 명\n" + RESET, "모집인원", rs.getInt("ANNOUNCEMENT_RECRUIT_CAP"));
				
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
				
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고담당자", rs.getString("ANNOUNCEMENT_CREATED_BY"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "담당자연락처", rs.getString("ANNOUNCEMENT_PM_CONTACT"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "재공고여부", (rs.getInt("ANNOUNCEMENT_REANN_YN") == 1 ? "Y" : "N"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_PRIMARY + "%,d 원\n" + RESET, "총 배정예산", rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET"));
				
				System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

				System.out.println();
				printMenuOption("1", "해당 공고 과제 신청하기");
				printMenuOption("2", "목록으로 돌아가기");

				printInputTag("메뉴 선택");
				int sel = Integer.parseInt(br.readLine());

				if (sel == 1) {
					
					System.out.println("\n" + INDENT + CLR_WHT + "[ 연구과제 신청 폼 ]" + RESET);
					printInputTag("첨부파일 경로 입력 (예: C:/docs/plan.pdf)");
					String attachPath = br.readLine();
					
					int budgetAmt;
					while(true) {
						System.out.println(INDENT + CLR_GRY + "  (신청 예산은 10억 원 미만으로 입력해야 합니다.)" + RESET);
						printInputTag("신청 예산액 입력 (숫자만)");
						try {
							budgetAmt = Integer.parseInt(br.readLine());
							if(budgetAmt > 999999999) {
								printError("정해진 범위(10억 미만) 내에서 입력해 주세요.");
								continue;
							}
							break;
						} catch(NumberFormatException e) {
							printError("숫자만 입력 가능합니다.");
						}
					}
					
					// 시스템 체크용 출력도 UI에 맞게 변경
					System.out.println(INDENT + CLR_GRY + "  [System Log] 신청자 ID 확인: " + cust_id + RESET);

					// 신청 처리 (DB 처리 전부 여기서 수행)
					dao1.applyAnnouncement(annId, cust_id, attachPath, budgetAmt);
					return;
				} 
				else if (sel == 2) {
					return;  // 목록으로
				} 
				else {
					printError("잘못된 선택입니다.");
				}

			} catch (NumberFormatException e) {
				printError("숫자를 입력하세요.");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}
}