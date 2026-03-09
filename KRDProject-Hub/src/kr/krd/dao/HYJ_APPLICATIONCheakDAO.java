package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_APPLICATIONCheakDAO {

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

	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "──────────────────────────────────────────────────────────────" + RESET);
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

	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 2) + "..";
	}

	/* =========================
	   내 신청 목록 조회 (Grid Layout)
	   ========================= */
	public void CheckMyApp(String cust_id) {

		while (true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			String sql =
					"SELECT a.APPLICATION_ID, " +
					"       a2.ANNOUNCEMENT_TITLE, " +
					"       a.APPLICATION_STATUS_CD " +
					"FROM APPLICATIONS a " +
					"LEFT JOIN ANNOUNCEMENT a2 " +
					"ON a.APPLICATION_ANN_ID = a2.ANNOUNCEMENT_ANN_ID " +
					"WHERE a.APPLICATION_USER_ID = ?";

			try {
				conn = DBUtil.getConnection();
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cust_id);

				rs = pstmt.executeQuery();

				printSubTitle("내 신청 목록 조회 (My Applications)");

				if (!rs.next()) {
					System.out.println(INDENT + CLR_GRY + "  현재 신청하신 내역이 없습니다." + RESET);
					return;
				}

				// 표 헤더 생성
				System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-28s | %-12s", "신청번호", "공고명", "진행상태") + RESET);
				System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------" + RESET);

				do {
					String status = rs.getString("APPLICATION_STATUS_CD");
					// 상태에 따른 색상 부여 로직 (승인류: 초록, 반려류: 빨강, 대기/진행류: 기본/하늘)
					String statColor = CLR_WHT;
					if(status != null) {
						if(status.contains("APPROVE") || status.contains("승인") || status.contains("PASS")) statColor = CLR_SUC;
						else if(status.contains("REJECT") || status.contains("반려") || status.contains("FAIL")) statColor = CLR_ERR;
						else if(status.contains("SUBMIT") || status.contains("PENDING") || status.contains("대기")) statColor = CLR_PRIMARY;
					}

					System.out.println(INDENT + String.format("%-10d | %-28s | " + statColor + "%-12s" + RESET, 
							rs.getInt("APPLICATION_ID"), 
							cut(rs.getString("ANNOUNCEMENT_TITLE"), 26), 
							(status == null ? "-" : status)));
				} while (rs.next());

				System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------" + RESET);

				System.out.println();
				printMenuOption("1", "신청 내역 상세 조회");
				printMenuOption("2", "이전 화면으로 돌아가기");

				printInputTag("메뉴 선택");
				int sel = Integer.parseInt(br.readLine());

				if (sel == 1) {
					while(true) {
						try {
							printInputTag("상세조회할 신청번호 입력");
							int appId = Integer.parseInt(br.readLine());
							detailApp(appId);
							break;
						} catch(NumberFormatException e) {
							printError("유효한 숫자를 입력하세요.");
						}
					}
				} else if (sel == 2) {
					return;
				} else {
					printError("잘못된 선택입니다.");
				}

			} catch(NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			} catch (Exception e) {
				printError("데이터 조회 중 오류가 발생했습니다.");
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}

	/* =========================
	   신청 상세 조회 (Profile Card Layout)
	   ========================= */
	private void detailApp(int appId) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql =
				"SELECT a.APPLICATION_ID, " +
				"       a.APPLICATION_ANN_ID, " +
				"       a2.ANNOUNCEMENT_TITLE, " +
				"       a.APPLICATION_USER_ID, " +
				"       a.APPLICATION_ATTACH_PATH, " +
				"       a.APPLICATION_STATUS_CD, " +
				"       a.APPLICATION_BUDGET_AMT " +
				"FROM APPLICATIONS a " +
				"LEFT JOIN ANNOUNCEMENT a2 " +
				"ON a.APPLICATION_ANN_ID = a2.ANNOUNCEMENT_ANN_ID " +
				"WHERE a.APPLICATION_ID = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, appId);

			rs = pstmt.executeQuery();

			if (!rs.next()) {
				printError("해당 번호의 신청 정보가 존재하지 않습니다.");
				return;
			}

			String status = rs.getString("APPLICATION_STATUS_CD");
			String statColor = CLR_WHT;
			if(status != null) {
				if(status.contains("APPROVE") || status.contains("승인") || status.contains("PASS")) statColor = CLR_SUC;
				else if(status.contains("REJECT") || status.contains("반려") || status.contains("FAIL")) statColor = CLR_ERR;
				else if(status.contains("SUBMIT") || status.contains("PENDING") || status.contains("대기")) statColor = CLR_PRIMARY;
			}

			printSubTitle("신청 상세 정보 (Application Details)");
			System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "신청번호", rs.getInt("APPLICATION_ID"));
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고번호", rs.getInt("APPLICATION_ANN_ID"));
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고명", rs.getString("ANNOUNCEMENT_TITLE"));
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "신청자 ID", rs.getString("APPLICATION_USER_ID"));
			System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "첨부파일 경로", rs.getString("APPLICATION_ATTACH_PATH"));
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_PRIMARY + "%,d 원\n" + RESET, "신청예산", rs.getInt("APPLICATION_BUDGET_AMT"));
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + statColor + "%s\n" + RESET, "진행 상태", status);
			System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

			printInputTag("엔터(Enter) 키를 누르면 이전 화면으로 돌아갑니다");
			br.readLine();
			 
		} catch (Exception e) {
			printError("상세 정보 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}