package kr.krd.dao;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_ReportDAO {

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

	private BufferedReader br;

	public HYJ_ReportDAO(BufferedReader br) {
		this.br = br;
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "──────────────────────────────────────────────────────────────" + RESET);
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

	// ------------------------------------------------
	// Business Logic Methods
	// ------------------------------------------------

	// 보고서 등록
	public void InsertReport(String cust_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		int cnt = 0;

		try {
			conn = DBUtil.getConnection();
			printSubTitle("연구 보고서 제출 (Submit Report)");

			// 1. 내가 맡고 있는 프로젝트가 있는지 확인
			if (!hasMyProject(conn, cust_id)) {
				printError("현재 담당하고 계신 프로젝트 내역이 없습니다.");
				return;
			}

			// 2. 내 프로젝트 목록 출력
			System.out.println(INDENT + CLR_WHT + "[ 내 프로젝트 목록 ]" + RESET);
			showMyProjects(conn, cust_id);

			// 3. 프로젝트 선택
			printInputTag("보고서를 제출할 프로젝트 아이디(ID)");
			String input = br.readLine().trim();

			if (!input.matches("\\d+")) {
				printError("프로젝트 아이디는 숫자만 입력 가능합니다.");
				return;
			}
			int project_id = Integer.parseInt(input);

			// 4. 프로젝트 확인 (내 프로젝트인지)
			if (!checkProject(conn, cust_id, project_id)) {
				printError("해당 번호의 프로젝트가 존재하지 않거나, 본인 소유의 프로젝트가 아닙니다.");
				return;
			}

			// 5. 보고서 입력 폼 
			System.out.println("\n" + INDENT + CLR_WHT + "[ 보고서 상세 정보 입력 ]" + RESET);
			
			String type = "";
			while(true){
			System.out.println(INDENT + CLR_GRY + "  (MID / FINAL 중 하나를 입력하세요)" + RESET);
			printInputTag("보고서 타입 (Type)");
			type = br.readLine().trim();
			if(!type.equals("MID") || !type.equals("FINAL")) {
				printError("MID / FINAL 중 하나를 입력하세요");
				continue;
				}
			break;
			}
			printInputTag("보고서 내용 요약 (Content)");
			String content = br.readLine().trim();

			printInputTag("핵심 키워드 (Keywords)");
			String keywords = br.readLine().trim();

			System.out.println(INDENT + CLR_GRY + "  (예: 80 -> 80% 진행됨)" + RESET);
			printInputTag("연구 진행률 입력 (Progress %)");
			String progressInput = br.readLine().trim();

			if (!progressInput.matches("\\d+")) {
				printError("진행률은 숫자만 입력 가능합니다.");
				return;
			}
			int progress = Integer.parseInt(progressInput);

			if (progress < 0 || progress > 100) {
				printError("진행률은 0에서 100 사이의 값이어야 합니다.");
				return;
			}

			// INSERT 쿼리 실행
			sql = "INSERT INTO REPORTS("
					+ "report_rpt_id, report_project_id, report_rpt_type_cd, report_submitted_at, "
					+ "report_status_cd, report_content, report_keywords, report_progress_rate, report_approved_by) "
					+ "VALUES(REPORTS_SEQ.NEXTVAL, ?, ?, SYSDATE, 'SUBMITTED', ?, ?, ?, ?)";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(++cnt, project_id);
			pstmt.setString(++cnt, type);
			pstmt.setString(++cnt, content);
			pstmt.setString(++cnt, keywords);
			pstmt.setInt(++cnt, progress);
			pstmt.setString(++cnt, "agy01"); // 하드코딩된 담당자ID 유지
			
			
			int result = pstmt.executeUpdate();
			if (result > 0) {
				printSuccess("해당 프로젝트의 보고서가 성공적으로 등록되었습니다.");
			} else {
				printError("보고서 등록 처리에 실패했습니다.");
			}

		} catch (Exception e) {
			printError("보고서 제출 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	// 내 프로젝트가 하나라도 있는지 확인
	private boolean hasMyProject(Connection conn, String cust_id) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			sql = "SELECT COUNT(*) FROM PROJECTS WHERE project_owner_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, cust_id);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, null);
		}
		return false;
	}

	// 내 프로젝트 목록 출력 (리스트 레이아웃)
	private void showMyProjects(Connection conn, String cust_id) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			sql = "SELECT project_id FROM PROJECTS WHERE project_owner_id = ? ORDER BY project_id";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, cust_id);
			rs = pstmt.executeQuery();

			System.out.println(INDENT + CLR_GRY + "---------------------------------" + RESET);
			int count = 0;
			while (rs.next()) {
				count++;
				System.out.println(INDENT + " " + CLR_PRIMARY + "▪" + RESET + " 프로젝트 아이디(ID) : " + CLR_WHT + BOLD + rs.getInt("project_id") + RESET);
			}
			if (count == 0) {
				System.out.println(INDENT + CLR_GRY + "  조회된 내역이 없습니다." + RESET);
			}
			System.out.println(INDENT + CLR_GRY + "---------------------------------" + RESET);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, null);
		}
	}

	// 프로젝트 존재 + 소유 확인
	private boolean checkProject(Connection conn, String cust_id, int project_id) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			sql = "SELECT COUNT(*) FROM PROJECTS WHERE project_id = ? AND project_owner_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, project_id);
			pstmt.setString(2, cust_id);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, null);
		}
		return false;
	}
}