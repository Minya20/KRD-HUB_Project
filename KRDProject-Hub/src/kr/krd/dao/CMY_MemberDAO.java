package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import kr.util.DBUtil;
import kr.krd.dao.MemberDAO;

public class CMY_MemberDAO {
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

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String cust_id;						// 사용자 아이디
	String role;						// 사용자 권한
	String field;						// 사용자 분야
	LocalDate today = LocalDate.now();	// 현재날짜를 저장하는 변수
	LocalDate deadline;					// 데드라인의 값을 저장할 변수
	MemberDAO dao = new MemberDAO(br);

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

	// 평가 배정 목록 조회 (Grid 레이아웃 적용)
	public void readEval() {
		while(true) {
			Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
			try {
				conn = DBUtil.getConnection();
				String sql = "SELECT  e.EVALUATION_ID, e.EVALUATION_APPLICATION_ID, e.EVALUATION_REVIEWER_ID, e.EVALUATION_FIELD, "
						+ "e.EVALUATION_ASSIGNED_at, e.EVALUATION_SCORE, e.EVALUATION_STATUS_CD, e.EVALUATION_IS, e.EVALUATION_DEADLINE_AT, "
						+ "a.APPLICATION_ID, a.APPLICATION_BUDGET_AMT, a.APPLICATION_USER_ID, a.APPLICATION_ATTACH_PATH, ag.AGENCY_AGY_NAME, "
						+ "an.ANNOUNCEMENT_START_DT, an.ANNOUNCEMENT_END_DT, an.ANNOUNCEMENT_TITLE, an.ANNOUNCEMENT_DESC, an.ANNOUNCEMENT_TOTAL_BUDGET "
						+ "FROM EVALUATIONS e JOIN APPLICATIONS a ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID "
						+ "JOIN ANNOUNCEMENT an ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID "
						+ "JOIN AGENCY ag ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID WHERE e.EVALUATION_REVIEWER_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cust_id);
				rs = pstmt.executeQuery();

				printSubTitle("나의 평가 배정 목록");

				if(rs.next()) {
					System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-6s | %-15s | %-10s | %-10s | %-6s | %-8s | %-10s | %-20s", 
							"평가번호", "과제명", "신청자", "기관명", "분야", "상태", "평가마감일", "공고기간") + RESET);
					System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------------------------------------------------------" + RESET);
					do {
						String status = rs.getString("EVALUATION_STATUS_CD");
						String statColor = status.equals("SUBMITTED") ? CLR_SUC : (status.equals("SAVED") ? CLR_PRIMARY : CLR_WHT);

						System.out.println(INDENT + String.format("%-8d | %-15s | %-10s | %-10s | %-6s | " + statColor + "%-8s" + RESET + " | %-10s | %s ~ %s", 
								rs.getInt("EVALUATION_ID"), cut(rs.getString("ANNOUNCEMENT_TITLE"), 15), rs.getString("APPLICATION_USER_ID"), 
								cut(rs.getString("AGENCY_AGY_NAME"), 10), rs.getString("EVALUATION_FIELD"), status, rs.getDate("EVALUATION_DEADLINE_AT"), 
								rs.getDate("ANNOUNCEMENT_START_DT"), rs.getDate("ANNOUNCEMENT_END_DT")));
					} while(rs.next());

					System.out.println("\n" + INDENT + "  [1] 상세 보기   [2] 이전으로 돌아가기");
					printInputTag("메뉴 선택");
					int eval_no = Integer.parseInt(br.readLine());
					if(eval_no == 1) {
						printInputTag("상세 보기할 대상의 번호 입력");
						int chooseEval_no = Integer.parseInt(br.readLine());
						viewAppDetail(chooseEval_no);
					} else if(eval_no == 2) break;

				} else {
					System.out.println(INDENT + CLR_GRY + "  평가할 대상 목록이 없습니다." + RESET);
					return;
				}
			} catch(NumberFormatException e) { printError("숫자를 입력하세요"); }
			catch(Exception e){ e.printStackTrace(); }
			finally { DBUtil.executeClose(rs, pstmt, conn); }
		}
	}

	// 상세 조회 메서드 (프로필 카드 레이아웃 적용)
	public void viewAppDetail(int eval_no) {
		mother:while(true) {
			Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
			try {
				conn = DBUtil.getConnection();
				String sql = "SELECT  e.EVALUATION_ID, e.EVALUATION_APPLICATION_ID, e.EVALUATION_REVIEWER_ID, e.EVALUATION_FIELD, "
						+ "e.EVALUATION_ASSIGNED_at, e.EVALUATION_SCORE, e.EVALUATION_STATUS_CD, e.EVALUATION_IS, e.EVALUATION_DEADLINE_AT, "
						+ "a.APPLICATION_ID, a.APPLICATION_BUDGET_AMT, a.APPLICATION_USER_ID, a.APPLICATION_ATTACH_PATH, ag.AGENCY_AGY_NAME, "
						+ "an.ANNOUNCEMENT_START_DT, an.ANNOUNCEMENT_END_DT, an.ANNOUNCEMENT_TITLE, an.ANNOUNCEMENT_FIELD, an.ANNOUNCEMENT_DESC, "
						+ "an.ANNOUNCEMENT_TOTAL_BUDGET FROM EVALUATIONS e JOIN APPLICATIONS a ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID "
						+ "JOIN ANNOUNCEMENT an ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID JOIN AGENCY ag ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID "
						+ "WHERE e.EVALUATION_ID = ? AND e.EVALUATION_REVIEWER_ID = ? AND an.ANNOUNCEMENT_HIDDEN_YN = 0";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, eval_no);
				pstmt.setString(2, cust_id);
				rs = pstmt.executeQuery();

				if(rs.next()) {
					int eval_id = rs.getInt("EVALUATION_ID");
					String eval_status = rs.getString("EVALUATION_STATUS_CD");
					Date eval_deadline = rs.getDate("EVALUATION_DEADLINE_AT");
					deadline = eval_deadline.toLocalDate();
					String eval_comment = rs.getString("EVALUATION_IS");
					if(eval_comment == null) eval_comment = "의견 없음";

					System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ 연구과제 상세 내역" + RESET);
					System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "과제명", rs.getString("ANNOUNCEMENT_TITLE"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "신청자 ID", rs.getString("APPLICATION_USER_ID"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "기관명", rs.getString("AGENCY_AGY_NAME"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원\n" + RESET, "신청예산", rs.getInt("APPLICATION_BUDGET_AMT"));
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "신청 분야", rs.getString("ANNOUNCEMENT_FIELD"));
					System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "평가 상태", eval_status);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "평가 마감일", deadline);
					System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%d 점\n" + RESET, "평가 점수", rs.getInt("EVALUATION_SCORE"));
					System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
					System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "[ 평가 의견 ]" + RESET);
					// 40자 단위 자르기 포맷
					for(int i = 0; i < eval_comment.length(); i += 40) {
						int end = Math.min(i + 40, eval_comment.length());
						System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_WHT + eval_comment.substring(i, end) + RESET);
					}
					System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

					if(today.isAfter(deadline)) {
						printError("해당 과제는 평가 기한이 마감되었습니다.");
						printInputTag("메뉴 (2: 나가기)");
						if(Integer.parseInt(br.readLine()) == 2) return;
						else printError("숫자 2만 입력 가능합니다.");
						continue;
					}

					System.out.println("\n" + INDENT + "  [1] 평가 진행/수정   [2] 이전으로 돌아가기");
					printInputTag("메뉴 선택");
					int do_eval_no = Integer.parseInt(br.readLine());

					if(do_eval_no == 1) {
						if(reEval(eval_id)) {
							System.out.println(INDENT + CLR_GRY + "  이미 해당 과제를 평가하였습니다. 재평가를 진행하시겠습니까?" + RESET);
							printInputTag("1: 재평가 진행   2: 돌아가기");
							int choose_no = Integer.parseInt(br.readLine());
							if(choose_no == 1) { reSubmiteval(eval_id); return; }
							else if(choose_no == 2) break;
						}

						boolean checkYN = checkTempEval(eval_id);
						if(!checkYN) { submiteval(eval_id); return; }	//임시저장을 사용 안하고 다시 작성
						else { useSaveEval(eval_id); return; }	//임시저장을 사용하는 메서드
					} else if(do_eval_no == 2) return;

				} else {
					printError("본인의 평가 목록에 존재하는 번호를 입력하세요!");
					return;
				}
			}catch(NumberFormatException e){ printError("숫자만 입력 가능합니다."); } 
			catch(Exception e){ e.printStackTrace(); }
			finally { DBUtil.executeClose(rs, pstmt, conn); }	
		}
	}

	public void submiteval(int eval_id) {
		Connection conn = null; PreparedStatement pstmt = null; int score = 0; String comment = null;
		try {
			score = checkScore("평가 점수 (0~100)");
			printInputTag("평가 코멘트 작성");
			comment = br.readLine();

			conn = DBUtil.getConnection();
			String sql = "UPDATE EVALUATIONS SET EVALUATION_SCORE=?, EVALUATION_IS=?, EVALUATION_STATUS_CD=? WHERE EVALUATION_ID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score); pstmt.setString(2, comment); pstmt.setInt(4, eval_id);

			mother:while(true) {
				System.out.println("\n" + INDENT + "  [1] 평가 제출 완료   [2] 나가기 (임시저장 옵션)");
				printInputTag("선택");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					pstmt.setString(3, "SUBMITTED");
					pstmt.executeUpdate();
					printSuccess("평가가 정상적으로 제출되었습니다.");
					evalCount(cust_id);
					break;
				} else if(save_eval_no == 2) {
					while(true) {
						System.out.println(INDENT + CLR_GRY + "  작성 중인 평가를 임시저장 하시겠습니까?" + RESET);
						printInputTag("1: 저장 후 나가기   2: 저장하지 않고 나가기");
						int saveTemp = Integer.parseInt(br.readLine());
						if(saveTemp == 1) { saveTempEval(eval_id, score, comment); break mother; }
						else if(saveTemp == 2) { printError("작성을 취소하고 돌아갑니다."); return; }
					}
				}
			}
		} catch(NumberFormatException e){ printError("숫자를 입력하세요"); }
		catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(null, pstmt, conn); }	
	}

	public void reSubmiteval(int eval_id) {
		Connection conn = null; PreparedStatement pstmt = null; int score = 0; String comment = null;
		try {
			score = checkScore("재평가 점수 (0~100)");
			printInputTag("재평가 코멘트 작성");
			comment = br.readLine();

			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("UPDATE EVALUATIONS SET EVALUATION_SCORE=?, EVALUATION_IS=?, EVALUATION_STATUS_CD=? WHERE EVALUATION_ID=?");
			pstmt.setInt(1, score); pstmt.setString(2, comment); pstmt.setInt(4, eval_id);

			while(true) {
				System.out.println("\n" + INDENT + "  [1] 재평가 제출   [2] 나가기");
				printInputTag("선택");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					pstmt.setString(3, "SUBMITTED");
					pstmt.executeUpdate();
					printSuccess("재평가 내용이 정상 반영되었습니다.");
					break;
				} else if(save_eval_no == 2) return;
			}
		} catch(NumberFormatException e){ printError("숫자만 입력 가능합니다."); }
		catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(null, pstmt, conn); }	
	}

	public void useSaveEval(int eval_id) {
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM TEMP_EVAL WHERE TEMP_EVAL_EVALUATION_ID = ?");
			pstmt.setInt(1, eval_id); rs = pstmt.executeQuery();

			if(rs.next()) {
				int temp_score = rs.getInt("TEMP_EVAL_SCORE");
				String temp_comment = rs.getString("TEMP_EVAL_IS");
				pstmt.close();

				pstmt = conn.prepareStatement("UPDATE EVALUATIONS SET EVALUATION_SCORE=?, EVALUATION_IS=?, EVALUATION_STATUS_CD='SUBMITTED' WHERE EVALUATION_ID=?");
				pstmt.setInt(1, temp_score); pstmt.setString(2, temp_comment); pstmt.setInt(3, eval_id);
				pstmt.executeUpdate();
				printSuccess("임시저장된 데이터를 불러와 제출을 완료했습니다.");

				pstmt.close();
				pstmt = conn.prepareStatement("DELETE FROM TEMP_EVAL WHERE TEMP_EVAL_EVALUATION_ID=?");
				pstmt.setInt(1, eval_id); pstmt.executeUpdate(); pstmt.close();

				evalCount(cust_id); 
			}
		} catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(rs, pstmt, conn); }	
	}

	public void evalCount(String rev_id) {
		// (기존 코드와 로직 동일)
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null; int revCount =0;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("SELECT EVALUATOR_EVAL_CNT FROM EVALUATOR WHERE EVALUATOR_REVIEWER_ID = ?");
			pstmt.setString(1, rev_id); rs = pstmt.executeQuery();
			if(rs.next()) revCount = rs.getInt("EVALUATOR_EVAL_CNT");
			rs.close(); pstmt.close();

			pstmt = conn.prepareStatement("UPDATE EVALUATOR SET EVALUATOR_EVAL_CNT = ? WHERE EVALUATOR_REVIEWER_ID = ?");
			pstmt.setInt(1, ++revCount); pstmt.setString(2, rev_id); pstmt.executeUpdate();
		} catch(Exception e) { e.printStackTrace(); } finally { DBUtil.executeClose(null, pstmt, conn); }
	}

	public int checkScore(String scoreInfo) {
		int score;
		while(true) {
			printInputTag(scoreInfo);	
			try {
				score = Integer.parseInt(br.readLine());
				if(score > 100 || score < 0) {
					printError("점수는 0~100점 사이로 입력해주세요.");
					continue;
				} else break;
			} catch(NumberFormatException e) { printError("숫자만 입력 가능합니다."); }
			catch(IOException e) { e.printStackTrace(); }
		}
		return score;
	}

	public boolean reEval(int reEval_id) {
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null; boolean resultbn = false;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM EVALUATIONS WHERE EVALUATION_ID = ? AND EVALUATION_STATUS_CD = 'SUBMITTED'");
			pstmt.setInt(1, reEval_id); rs = pstmt.executeQuery();
			if(rs.next()) resultbn = true;
		} catch(Exception e) { e.printStackTrace(); } finally { DBUtil.executeClose(rs, pstmt, conn); }
		return resultbn;
	}

	public boolean checkTempEval(int eval_id) {
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null; boolean checkFlag = false;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM EVALUATIONS WHERE EVALUATION_ID = ? AND EVALUATION_STATUS_CD = 'SAVED'");
			pstmt.setInt(1, eval_id); rs = pstmt.executeQuery();
			if(rs.next()) checkFlag = true;

			while(checkFlag) {
				int temp_choose;
				while(true) {
					try {
						System.out.println(INDENT + CLR_PRIMARY + "  해당 과제에 대해 임시저장된 평가 기록이 존재합니다." + RESET);
						printInputTag("1: 임시저장 데이터 사용   2: 폐기 후 새로 작성");
						temp_choose = Integer.parseInt(br.readLine());
						if(temp_choose > 2 || temp_choose < 1) {	
							continue;
						}
						break;
					}catch(NumberFormatException e) {
						printError("숫자를 입력하세요.");
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(temp_choose == 1) break;
				else if(temp_choose == 2) {
					rs.close(); pstmt.close();
					pstmt = conn.prepareStatement("DELETE FROM TEMP_EVAL WHERE TEMP_EVAL_EVALUATION_ID = ?");
					pstmt.setInt(1, eval_id); pstmt.executeUpdate();
					printSuccess("기존 임시평가 데이터를 삭제했습니다.");
					checkFlag = false;
				} else {
					printError("1 또는 2를 입력해주세요.");
					printInputTag("평가를 직접 작성합니다.");
					checkFlag = false;
					//return 
				}
			}
		}catch(NumberFormatException e){ printError("숫자만 입력 가능합니다."); }
		catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(rs, pstmt, conn); }	
		return checkFlag;
	}

	public void saveTempEval(int eval_id, int score, String comment) {
		Connection conn = null; PreparedStatement pstmt = null;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("INSERT INTO TEMP_EVAL(TEMP_EVAL_ID, TEMP_EVAL_EVALUATION_ID, TEMP_EVAL_SCORE, TEMP_EVAL_IS) VALUES(TEMP_EVAL_SEQ.NEXTVAL,?,?,?)");
			pstmt.setInt(1, eval_id); pstmt.setInt(2, score); pstmt.setString(3, comment);
			pstmt.executeUpdate(); pstmt.close();

			pstmt = conn.prepareStatement("UPDATE EVALUATIONS SET EVALUATION_STATUS_CD = 'SAVED' WHERE EVALUATION_ID = ?");
			pstmt.setInt(1, eval_id); pstmt.executeUpdate();
			printSuccess("평가 내용이 임시저장 되었습니다.");
		} catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(null, pstmt, conn); }	
	}

	// 평가위원 내 정보 (프로필 카드 적용)
	public void myInfo(String myCust_id) {
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null; int choose_no;
		try {
			conn = DBUtil.getConnection();
			String sql = "SELECT u.user_id, u.user_name, u.user_email, u.user_birth_dt, u.user_addr, u.user_country_cd, "
					+ "u.user_gender_cd, u.user_affiliation, u.user_field, e.EVALUATOR_EVAL_CNT, u.user_created_at "
					+ "FROM USERINFO u JOIN EVALUATOR e ON u.user_id = e.EVALUATOR_REVIEWER_ID WHERE u.user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, myCust_id);
			rs = pstmt.executeQuery();

			if(rs.next()) {
				printSubTitle("내 정보 관리 (My Profile)");
				System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "ID", rs.getString("user_id"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이름", rs.getString("user_name"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "이메일", rs.getString("user_email"));
				String birth = rs.getString("user_birth_dt");
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "생년월일", birth.substring(0, 4) + "-" + birth.substring(5, 7) + "-" + birth.substring(8, 10));
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "국적", rs.getString("user_country_cd"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "성별", (rs.getInt("user_gender_cd") == 1 ? "남성" : "여성"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "주소", rs.getString("user_addr"));
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "소속", (rs.getString("user_affiliation") == null ? "무소속" : rs.getString("user_affiliation")));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "전문 분야", rs.getString("user_field"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_SUC + "%d 회\n" + RESET, "누적 평가수", rs.getInt("evaluator_eval_cnt"));
				System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

				while(true) {
					System.out.println("\n" + INDENT + "  [1] 비밀번호 변경   [2] 나가기");
					printInputTag("선택");
					try {
						choose_no = Integer.parseInt(br.readLine());
						if(choose_no == 1) { dao.changeMyPassword(myCust_id);break; }
						else if(choose_no == 2) return;
						else printError("1 또는 2를 입력하세요.");
					} catch(NumberFormatException e) { printError("숫자를 입력하세요."); }
				}
			}
		} catch(Exception e) { e.printStackTrace(); } finally { DBUtil.executeClose(rs, pstmt, conn); }
	}

	// 평가위원 대시보드
	public boolean callReviewerMenu(String myCust_id, String myRole, String myField) {
		this.cust_id = myCust_id; this.role = myRole; this.field = myField;
		while(true) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "평가위원 대시보드 (Reviewer)" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + CLR_GRY + "  접속 계정: " + RESET + cust_id + CLR_GRY + " | 등급: 평가위원" + RESET);
			System.out.println();

			printMenuOption("1", "평가 배정 목록 조회 (Tasks)");
			printMenuOption("2", "내 정보 관리 (Profile)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("3", "시스템 로그아웃 (Logout)");
			printMenuOption("4", "프로그램 종료 (Exit)");

			printInputTag("메뉴 선택");
			try {
				int rev_choose = Integer.parseInt(br.readLine());
				if(rev_choose == 1) readEval();
				else if(rev_choose == 2) myInfo(cust_id);
				else if(rev_choose == 3) {
					printSuccess("안전하게 로그아웃 되었습니다.");
					return true;
				} else if(rev_choose == 4) {
					System.out.println("\n" + INDENT + "프로그램 종료"); System.exit(0);
				} else printError("올바른 번호를 입력하세요.");
			} catch(NumberFormatException e) { 
				printError("숫자를 입력하세요."); 
			}catch(Exception e) {
				printError("입력 처리 중 오류가 발생했습니다.");
			}
		}
	}


}