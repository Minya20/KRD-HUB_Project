package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import kr.util.USH_ConsoleUtil;

import kr.util.DBUtil;

public class HYJ_MyInfoDAO {
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

	private static final String LN_SINGLE = "─";
	private static final String LN_DOUBLE = "═";
	private static final String INDENT = "      ";
	

	private BufferedReader br =
			new BufferedReader(new InputStreamReader(System.in));

	/* =====================
	   내 정보 조회 (Profile Card Layout 적용)
	   ===================== */
	public void SelectInfo(String cust_id) {

		while (true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			String sql = "SELECT * FROM USERINFO WHERE user_id = ?";

			try {
				conn = DBUtil.getConnection();
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cust_id);

				rs = pstmt.executeQuery();

				if (rs.next()) {
					printSubTitle("내 정보 상세 조회 (My Profile)");
					
					System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이름", rs.getString("USER_NAME"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이메일", rs.getString("user_email"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "주소", rs.getString("user_addr"));
					System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "연구 분야", rs.getString("user_field"));
					System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);
				} else {
					printError("회원 정보를 찾을 수 없습니다.");
					return;
				}

				System.out.println("\n" + INDENT + "  [1] 정보 업데이트 (Update)   [2] 이전 화면으로 (Back)");
				printInputTag("메뉴 선택");
				
				int sel = Integer.parseInt(br.readLine());

				if (sel == 1) {
					System.out.println("\n" + INDENT + CLR_WHT + "[ 정보 수정 폼 ]" + RESET);
					
					printInputTag("변경할 이름");
					String name = br.readLine();
					
					printInputTag("변경할 이메일");
					String email = br.readLine();

					printInputTag("변경할 주소");
					String addr = br.readLine();

					InfoUpdate(name, email, addr, cust_id);

				} else if (sel == 2) {
					return;
				} else {
					printError("잘못된 선택입니다. 1 또는 2를 입력하세요.");
				}

			} catch (NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			} catch (Exception e) {
				printError("내 정보 조회 중 오류가 발생했습니다.");
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}

	/* =====================
       정보 수정
       ===================== */
	public void InfoUpdate(String user_name,
			String user_email,
			String user_addr,
			String user_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql =
				"UPDATE userinfo " +
						"SET user_name=?, user_email=?, user_addr=? " +
						"WHERE user_id=?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);

			int cnt = 0;
			pstmt.setString(++cnt, user_name);
			pstmt.setString(++cnt, user_email);
			pstmt.setString(++cnt, user_addr);
			pstmt.setString(++cnt, user_id);

			int count = pstmt.executeUpdate();

			if (count > 0) {
				printSuccess("수정 완료");
			} else {
				printError("수정 삭제");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
	
	
		
		public String getField(int no) {
			String field = null;

			switch(no) {
			case 1:
				field = "의료";
				break;
			case 2:
				field = "바이오";
				break;
			case 3:
				field = "로보틱스";
				break;
			case 4:
				field = "데이터";
				break;
			case 5:
				field = "기획";
				break;
			case 6:
				field = "국방";
				break;
			case 7:
				field = "교통";
				break;
			case 8:
				field = "감사";
				break;
			case 9:
				field = "AI";
				break;
			case 10:
				field = "환경";
				break;
			case 11:
				field = "협약";
				break;
			case 12:
				field = "평가";
				break;
			case 13:
				field = "제도";
				break;
			case 14:
				field = "정책";
				break;
			case 15:
				field = "정산";
				break;
			case 16:
				field = "전기";
				break;
			case 17:
				field = "예산";
				break;
			case 18:
				field = "역사";
				break;
			case 19:
				field = "에너지";
				break;
			case 20:
				field = "소재";
				break;
			case 21:
				field = "반도체";
				break;
			case 22:
				field = "운영";
				break;
			case 23:
				field = "성과";
				break;

			}

			return field;
		}
	
	private void printDrawLine(String start, String middle, String end, String lineType, int width) {
		StringBuilder sb = new StringBuilder();
		sb.append(INDENT).append(CLR_GRY).append(start);
		for(int i=0; i<width; i++) sb.append(lineType);
		sb.append(end).append(RESET);
		System.out.println(sb.toString());
	}
	
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printInputTag(String tag) {
		System.out.print("\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ");
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + msg + RESET);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + msg + RESET);
	}
}