package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_RESISearch {

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

	// 반복적인 스트림 생성을 방지하기 위해 클래스 레벨로 이동
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

	// ------------------------------------------------
	// Business Logic Methods
	// ------------------------------------------------

	/* =========================
	   인재 열람 목록 조회 (Grid Layout)
	   ========================= */
	public void RESISearch() {
		while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = null;

			try {
				conn = DBUtil.getConnection();
				// 가독성을 위해 아이디 순 정렬 추가
				sql = "SELECT user_id, user_name, user_birth_dt, user_field FROM USERINFO ORDER BY user_id";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();

				printSubTitle("인재 열람 목록 (Search Researchers)");

				if(rs.next()) {
					// 표 헤더 생성
					System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-15s | %-12s | %-12s | %-15s", 
							"아이디", "이름", "생년월일", "전문 분야") + RESET);
					System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------" + RESET);
					
					do {
						String birthDt = rs.getString("user_birth_dt");
						if(birthDt != null && birthDt.length() == 8) {
							birthDt = birthDt.substring(0,4) + "-" + birthDt.substring(4,6) + "-" + birthDt.substring(6,8);
						} else if (birthDt == null) {
							birthDt = "-";
						}

						System.out.println(INDENT + String.format("%-16s | %-12s | %-12s | %-15s", 
								rs.getString("user_id"),
								cut(rs.getString("user_name"), 10),
								birthDt,
								(rs.getString("user_field") == null ? "미지정" : rs.getString("user_field"))));
					} while(rs.next());
					
					System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------" + RESET);

					int sel;
					while(true) {
						System.out.println();
						printMenuOption("1", "특정 연구자 상세 정보 보기");
						printMenuOption("2", "이전 화면으로 돌아가기");
						printInputTag("메뉴 선택");
						
						try {
							sel = Integer.parseInt(br.readLine());
							if(sel > 2 || sel < 1) {
								printError("1 혹은 2를 입력하세요.");
								continue;
							}
							break;
						} catch(NumberFormatException e) {
							printError("숫자만 입력 가능합니다.");
						}
					}
					
					if(sel == 1) {
						printInputTag("상세조회할 대상의 아이디(ID) 입력");
						String user_id = br.readLine();
						RESISearchDetail(user_id);
					} else if(sel == 2) {
						return;
					}
				} else {
					System.out.println(INDENT + CLR_GRY + "  등록된 인재 정보가 없습니다." + RESET);
					return;
				}
			} catch(Exception e) {
				printError("데이터 조회 중 오류가 발생했습니다.");
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}


	/* =========================
	   인재 상세 정보 조회 (Profile Card Layout)
	   ========================= */
	private void RESISearchDetail(String user_Id) {
		while (true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				conn = DBUtil.getConnection();
				String sql = "SELECT USER_ID, USER_NAME, USER_EMAIL, USER_BIRTH_DT, USER_COUNTRY_CD, USER_AFFILIATION, USER_FIELD "
						+ "FROM USERINFO "
						+ "WHERE USER_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, user_Id);
				rs = pstmt.executeQuery();

				if (!rs.next()) {
					printError("입력하신 아이디와 일치하는 인원이 존재하지 않습니다.");
					return;
				}

				printSubTitle("연구자 상세 정보 (Researcher Profile)");
				
				// 프로필 카드 출력
				System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "아이디", rs.getString("USER_ID"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이름", rs.getString("USER_NAME"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이메일", rs.getString("USER_EMAIL"));
				
				String birth = rs.getString("USER_BIRTH_DT");
				if(birth != null && birth.length() == 8) {
					birth = birth.substring(0, 4) + "-" + birth.substring(4, 6) + "-" + birth.substring(6, 8);
				}
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "생년월일", (birth == null ? "미지정" : birth));
				
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "국적", rs.getString("USER_COUNTRY_CD"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "소속 기관", rs.getString("USER_AFFILIATION"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_PRIMARY + "%s\n" + RESET, "전문 분야", rs.getString("USER_FIELD"));
				System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

				printInputTag("1: 목록으로 돌아가기");
				String sel = br.readLine();
				
				if(sel.equals("1")) {
					return;
				} else {
					printError("잘못 입력하셨습니다. 숫자 1을 입력해주세요.");
				}

			} catch (Exception e) {
				printError("상세 정보 조회 중 오류가 발생했습니다.");
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}
}