package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.MemberDAO;
import kr.krd.main.RR_KRDAdminMain;

public class UserMain {
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

	private BufferedReader br;
	private MemberDAO dao;	//공통클래스
	private CMY_MemberDAO revdao; //평가위원클래스
	private RR_KRDAdminMain agy; //기관관리자 클래스
	private String cust_id;
	private String role; 
	private String field;
	private boolean login;

	public UserMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new MemberDAO();
			revdao = new CMY_MemberDAO();
			// 콘솔 화면 화면 초기화 (지원하는 터미널에서만 작동)
			callMenu();

		} catch(Exception e) {
			printError("시스템 초기화 중 예상치 못한 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			if(br!=null) try { br.close(); } catch(IOException e) {}
		}
	}

	// ------------------------------------------------
	// UI Helper Methods (깔끔한 출력을 위한 유틸리티)
	// ------------------------------------------------
	private void printDrawLine(String start, String middle, String end, String lineType, int width) {
		StringBuilder sb = new StringBuilder();
		sb.append(INDENT).append(CLR_GRY).append(start);
		for(int i=0; i<width; i++) sb.append(lineType);
		sb.append(end).append(RESET);
		System.out.println(sb.toString());
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

	// ------------------------------------------------
	// Screen Outputs (실제 화면 출력)
	// ------------------------------------------------

	// 1. 메인 로고 (ASCII Art - 화려함 담당)
	private void printLogo() {
		System.out.println(CLR_PRIMARY + BOLD);
		System.out.println(INDENT + " ██╗  ██╗██████╗ ██████╗     ██╗  ██╗██╗   ██╗██████╗ ███████╗");
		System.out.println(INDENT + " ██║ ██╔╝██╔══██╗██╔══██╗    ██║  ██║██║   ██║██╔══██╗██╔════╝");
		System.out.println(INDENT + " █████╔╝ ██████╔╝██║  ██║    ███████║██║   ██║██████╔╝███████╗");
		System.out.println(INDENT + " ██╔═██╗ ██╔══██╗██║  ██║    ██╔══██║██║   ██║██╔══██╗╚════██║");
		System.out.println(INDENT + " ██║  ██╗██║  ██║██████╔╝    ██║  ██║╚██████╔╝██████╔╝███████║");
		System.out.println(INDENT + " ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝     ╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝");
		System.out.println(RESET);
	}

	// 2. 메인 메뉴화면
	private void callMenu() throws IOException {
		while(true) {
			System.out.println("\n\n"); // Upper padding
			printLogo();

			System.out.println(INDENT + CLR_WHT + BOLD + "  국가 연구과제 관리 플랫폼" + RESET + CLR_GRY + " | Version 1.0" + RESET);
			printDrawLine("┏", LN_SINGLE, "┓", LN_SINGLE, 50);
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);

			printMenuOption("1", "시스템 로그인 (Login)");
			printMenuOption("2", "신규 회원가입 (Sign Up)");

			printMenuOption("3", "프로그램 종료 (Exit)");

			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printDrawLine("┗", LN_SINGLE, "┛", LN_SINGLE, 50);

			printInputTag("메뉴 선택");

			try {
				String input = br.readLine();
				if(input == null) break;
				int MenuNo = Integer.parseInt(input);

				if(MenuNo == 1) {
					// 로그인 화면 로직
					if(handleLoginScreen()) {
						// 로그인 성공 시 권한별 메뉴 진입
						processRoleMenu();
					}
				} else if(MenuNo == 2) {
					System.out.println("\n" + INDENT + "[ 회원가입 모듈로 이동합니다 ]");
					dao.insertMember(); 
				} else if(MenuNo == 3) {
					System.out.println("\n" + INDENT + CLR_WHT + "KRD Hubs를 종료합니다. 이용해 주셔서 감사합니다." + RESET);
					break;
				} else {
					printError("잘못된 입력입니다. 메뉴 번호를 확인하세요.");
				}
			} catch(NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			}
		}
	}

	// 3. 로그인 및 ID/PW 찾기 선택 화면
	private boolean handleLoginScreen() throws IOException {
		while(true) {
			System.out.println("\n");
			printDrawLine("╔", LN_DOUBLE, "╗", LN_DOUBLE, 40);
			System.out.println(INDENT + CLR_GRY + "║" + RESET + "  " + CLR_WHT + BOLD + "접속 인증" + RESET);
			printDrawLine("╠", LN_SINGLE, "╣", LN_SINGLE, 40);
			System.out.println(INDENT + CLR_GRY + "║" + RESET);

			printMenuOption("1", "로그인 진행");
			printMenuOption("2", "아이디 / 비밀번호 찾기");
			printMenuOption("0", "이전 화면으로");

			System.out.println(INDENT + CLR_GRY + "║" + RESET);
			printDrawLine("╚", LN_DOUBLE, "╝", LN_DOUBLE, 40);

			printInputTag("진행 선택");

			try {
				int secMenuNo = Integer.parseInt(br.readLine());
				if(secMenuNo == 1) {
					// 실제 로그인 입력 폼
					System.out.println("\n" + INDENT + CLR_GRY + LN_SINGLE.repeat(42) + RESET);
					printInputTag("아이디(ID)");
					String user_id = br.readLine();
					printInputTag("비밀번호(PW)");
					String user_pw = br.readLine();
					System.out.println(INDENT + CLR_GRY + LN_SINGLE.repeat(42) + RESET);

					cust_id = dao.userLogin(user_id, user_pw);

					if(cust_id != null && !cust_id.equals("0")) {
						login = true;
						role = dao.getUserRole(cust_id);
						field = dao.getUserField(cust_id);
						printSuccess(cust_id + "님, 환영합니다. 시스템에 접속합니다.");
						return true; // 로그인 성공
					} else {
						printError("인증에 실패했습니다. ID와 PW를 확인하세요.");
					}
				} else if(secMenuNo == 2) {
					findIdPwMenu();
				} else if(secMenuNo == 0) {
					return false; // 뒤로 가기
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력하세요.");
			}
		}
	}

	// 4. 로그인 후 권한별 메뉴 처리 (레이아웃 개선)
	private void processRoleMenu() throws IOException {
		while(login) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + BOLD + "========================================");
			System.out.println(INDENT + "   USER SESSION 활성화 [" + role + "]");
			System.out.println(INDENT + "========================================" + RESET);

			// 기존 리스너/게스트 메뉴 호출 (DAO쪽 UI도 이 스타일로 맞추는 것을 추천)
			if(role.equals("REV")) {
				revdao.callReviewerMenu(cust_id, role, field);
			} else if(role.equals("GST")) {
				callGuestMenu(cust_id, role, field);
			} else if(role.equals("AGY")) {
				RR_KRDAdminMain agyMain = new RR_KRDAdminMain();
				agyMain.callMenu();

				printInputTag("로그아웃 하시겠습니까? (Y/N)");
				String logoutInput = br.readLine();
				if(logoutInput.equalsIgnoreCase("Y")) {
					login = dao.logout(); 
					cust_id = null;
					role = null;
					field = null;
					printSuccess("안전하게 로그아웃 되었습니다.");
					break;
				}
			}
			else if(role.equals("ADM") || role.equals("RESI") || role.equals("RESO")) {
				// 미구현 권한에 대한 깔끔한 처리
				System.out.println("\n" + INDENT + CLR_WHT + "[" + role + "] 권한 전용 화면을 로드 중입니다..." + RESET);
				System.out.println(INDENT + CLR_GRY + "(현재 버전에서는 데모 화면만 제공됩니다)" + RESET);

				printInputTag("로그아웃 하시겠습니까? (Y/N)");
				String logoutInput = br.readLine();
				if(logoutInput.equalsIgnoreCase("Y")) {
					// 세션 클리어 로직
					login = dao.logout(); 
					cust_id = null;
					role = null;
					field = null;
					printSuccess("안전하게 로그아웃 되었습니다.");
					break;
				}
			} else {
				printError("정의되지 않은 권한입니다. 관리자에게 문의하세요.");
				login = false;
				break;
			}

			// DAO 메뉴에서 돌아왔을 때 로그인 상태 확인
			if(!login) {
				break;
			}
		}
	}

	// 5. 아이디/비밀번호 찾기 메뉴 (깔끔하게 정리)
	public void findIdPwMenu() {
		while(true) {
			try {
				System.out.println("\n");
				printDrawLine("┌", LN_SINGLE, "┐", LN_SINGLE, 35);
				System.out.println(INDENT + "┃  " + CLR_WHT + BOLD + "계정 정보 찾기" + RESET);
				printDrawLine("├", LN_SINGLE, "┤", LN_SINGLE, 35);
				System.out.println(INDENT + "┃");
				printMenuOption("1", "아이디(ID) 찾기");
				printMenuOption("2", "비밀번호 재설정");
				printMenuOption("0", "이전 화면으로");
				System.out.println(INDENT + "┃");
				printDrawLine("└", LN_SINGLE, "┘", LN_SINGLE, 35);

				printInputTag("선택");

				int menu = Integer.parseInt(br.readLine());

				if(menu == 1) {
					dao.findUserId();
				} else if(menu == 2) {
					dao.resetPassword();
				} else if(menu == 0) {
					return; // 뒤로가기
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력해주세요.");
			} catch(Exception e) {
				printError("오류가 발생했습니다.");
			}
		}
	}

	// 2. 일반회원 화면 (추가된 부분 UI 개선)
	public void callGuestMenu(String myCust_id, String myRole, String myField) {
		this.cust_id = myCust_id;
		this.role = myRole;
		this.field = myField;

		while(true) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + BOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "일반회원 메인메뉴" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + CLR_GRY + "  접속 계정: " + RESET + cust_id + CLR_GRY + " | 등급: 일반회원" + RESET);
			System.out.println();

			printMenuOption("1", "공고 조회 (Announcement)");
			printMenuOption("2", "권한 신청 (Request Role)");
			printMenuOption("3", "내 정보 관리 (My Profile)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("4", "시스템 로그아웃 (Logout)");
			printMenuOption("5", "프로그램 종료 (Exit)");

			printInputTag("메뉴 선택");
			try {
				int gst_choose = Integer.parseInt(br.readLine());
				if(gst_choose == 1) {
					System.out.println("\n" + INDENT + CLR_WHT + ">> 공고 조회 모듈을 로드합니다..." + RESET);
				} else if(gst_choose == 2) {
					dao.applyRole(cust_id);
				} else if(gst_choose == 3) {
					System.out.println("\n" + INDENT + CLR_WHT + ">> 내 정보 관리 화면으로 이동합니다." + RESET);
				} else if(gst_choose == 4) {
					printSuccess("안전하게 로그아웃 되었습니다.");
					return;
				} else if(gst_choose == 5) {
					System.out.println("\n" + INDENT + "프로그램을 종료합니다.");
					System.exit(0);
				} else printError("잘못된 선택입니다.");
			} catch(Exception e) {
				printError("입력 오류가 발생했습니다.");
			}
		}
	}

	public static void main(String[] args) {
		// ANSI Escape 코드를 지원하지 않는 일부 윈도우 CMD를 위한 설정이 필요할 수 있으나,
		// 최신 터미널(VSCode, IntelliJ, Windows Terminal)에서는 대개 잘 작동합니다.
		new UserMain();
	}
}