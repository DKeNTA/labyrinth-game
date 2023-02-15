import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*; // キーボードイベントのためのクラスをまとめてimport

import java.io.*;
import java.awt.image.*; // 画像ファイルの取り扱い関係(1)
import javax.imageio.*; // 画像ファイルの取り扱い関係(2)

import java.util.Random;
import java.util.Stack;
import java.util.Scanner;

public class Labyrinth {
  	public static void main(String[] args) {
    	JFrame fr = new JFrame("Labyrinth game");
    	fr.setSize(1200,800);
    	fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	fr.getContentPane().setBackground(new Color(0, 0, 0));

    	LabyrinthPanel panel = new LabyrinthPanel();
    	panel.setOpaque(false);
    	fr.add(panel);
    	fr.setVisible(true);
  	}

}

class LabyrinthPanel extends JPanel implements KeyListener, Runnable {

	private int labyrinthSize_x = 37, labyrinthSize_y = 23; // 迷路のサイズ
	private boolean[][] wall;   // 壁: true, 道: false
	private int road_x, road_y;  // 道にしようとしているマスの座標
	private Stack<Integer> road_xStack = new Stack<Integer>();  
	private Stack<Integer> road_yStack = new Stack<Integer>();  // 既に道にしたマスの座標を積んだスタック
	private int start_x, start_y;
	private int goal_x, goal_y; 

	private int dragon_x, dragon_y;
	private int sword_x, sword_y;

	// 新しく迷路を作るメソッド
	public void createLabyrinth() {
		// 初期化
		for (int i = 1; i < labyrinthSize_y + 1; i++) {
			for (int j = 1; j < labyrinthSize_x + 1; j++) {
				wall[i][j] = true;
			}
		}

		// ランダムに開始位置を選ぶ
		Random rand = new Random();
		road_x = rand.nextInt(labyrinthSize_x - 1) + 1;
		road_y = rand.nextInt(labyrinthSize_y - 1) + 1;
		wall[road_y][road_x] = false;
		road_xStack.push(road_x);
		road_yStack.push(road_y);

		boolean continueFlag = true;  // 道を作れるかどうかの判定

		// 以下、wall[][]全体を埋めるまで繰り返し
		while (continueFlag) {

			// 上下左右のいずれかに限界まで道を伸ばす
			extendRoad();

			// 既にある道から次の開始位置を選ぶ
			continueFlag = false;

			while (!road_xStack.empty() && !road_yStack.empty()) {
				road_x = road_xStack.pop();
				road_y = road_yStack.pop();

				if ( canExtendRoad() ) {
					continueFlag = true;
					break;
				}
			}
		}
	}

	// 道を拡張するメソッド
	public void extendRoad() {
		boolean extendFlag = true;

		while (extendFlag) {
			extendFlag = extendRoadSub();
		}
	}

	// 道の拡張に成功したらtrue、失敗したらfalseを返すメソッド
	public boolean extendRoadSub() {
		Random rand = new Random();
		// 上: 0, 下: 1, 左: 2, 右: 3
		int dir = rand.nextInt(4);

		for (int i = 0; i < 4; i++) {
			dir = (dir + i) % 4;
			if ( canExtendRoadWithDir(dir) ) {
				movePoint(dir);
				return true;
			}
		}

		return false;
	}

	// 指定した方向へ拡張可能ならばtrue、不可能ならばfalseを返すメソッド
	public boolean canExtendRoadWithDir(int dir) {
		int exRoad_x = road_x, exRoad_y = road_y;

		switch ( dir ) {
			case 0:	// 上
				exRoad_x--;
				break;

			case 1:	// 下
				exRoad_x++;
				break;

			case 2:	// 左
				exRoad_y--;
				break;

			case 3:	// 右
				exRoad_y++;
				break;
		}

		if (countSurroundingRoad(exRoad_x, exRoad_y) > 1) {
			return false;
		}

		return true;
	}

	// 周囲1マスにある道の数を数えるメソッド
	public int countSurroundingRoad(int road_x, int road_y) {
		int num = 0;

		if (road_x - 1 < 1 || !wall[road_y][road_x - 1]) {
			num++;
		}
		if (road_x + 1 > labyrinthSize_x  || !wall[road_y][road_x + 1]) {
			num++;
		}
		if (road_y - 1 < 1 || !wall[road_y - 1][road_x]) {
			num++;
		}
		if (road_y + 1 > labyrinthSize_y  || !wall[road_y + 1][road_x]) {
			num++;
		}

		return num;
	}


	// 指定した方向へ1マスroad_xとroad_yを移動させるメソッド
	public void movePoint(int dir) {
		switch ( dir ) {
			case 0:	// 上
				road_x--;
				break;

			case 1:	// 下
				road_x++;
				break;

			case 2:	// 左
				road_y--;
				break;

			case 3:	// 右
				road_y++;
				break;
		}

		wall[road_y][road_x] = false;
		road_xStack.push(road_x);
		road_yStack.push(road_y);
	}

	// 上下左右いずれかの方向へ移動できるならtrue、できないならfalseを返すメソッド
	public boolean canExtendRoad() {
		return (canExtendRoadWithDir(0) || canExtendRoadWithDir(1) || canExtendRoadWithDir(2) || canExtendRoadWithDir(3));
	}

	// スタートを初期位置に配置するメソッド
	public void resetStart() {
		start_x = 1;
		start_y = labyrinthSize_y - 1;

		while (true) {
			if (wall[start_y][start_x + 1]) {
				start_y--;
			} else {
				break;
			}
		}

		wall[start_y][start_x] = false;
	}


	// ゴールを初期位置に配置するメソッド
	public void resetGoal() {
		goal_x = labyrinthSize_x;
		goal_y = 2;

		while ( true ) {
			if ( wall[goal_y][goal_x - 1] ) {
				goal_y++;
			} else {
				break;
			}
		}

		wall[goal_y][goal_x] = false;
	}

	// ドラゴンの座標を設定するメソッド
	public void resetDragon() {
		Random rand = new Random();

		dragon_x = labyrinthSize_x - rand.nextInt(labyrinthSize_x / 3) - 2;
		dragon_y = rand.nextInt(labyrinthSize_y / 3) + labyrinthSize_y / 3;
	}

	// 剣の座標を設定するメソッド
	public void resetSword() {
		Random rand = new Random();

		// 迷路左3分の1の上に剣を配置 
		sword_x = rand.nextInt(labyrinthSize_x / 3) + 2;  
		sword_y = 2;
		
		while ( true ) {
  			if ( wall[sword_y][sword_x] ) {
				sword_y++;
			} else {
				break;
			}
		}	
	}

 	// 二次元マップの準備
 	private char map[][];
  	private int MX = labyrinthSize_x + 1, MY = labyrinthSize_y + 1;

	public void printLabyrinth() {
		for (int i = 1; i < labyrinthSize_y + 1; i++) {
			for (int j = 1; j < labyrinthSize_x + 1; j++) {
			 	if (i == goal_y && j == goal_x) { 
					map[i][j] = ('M');  // ゴールにみかんを配置する
				} else if (i == sword_y && j == sword_x) {
					map[i][j] = ('S'); 
				} else if (wall[i][j]) {
					map[i][j] = ('B');
				} else { 
					map[i][j] = (' ');
			 	}
			}
		}
	}

  	// キャラクタ画像用
  	private BufferedImage yakitoriImg, mikanImg, dragonImg, fireImg, swordImg ;
  	private Image[] toriImg = new Image[4];
  	private int tori_x, tori_y, toriNo;
	private int fire_x, fire_y;	
	private int size = 30; // サイズ調整用の変数
	private int dragonSize = size;

	// 鳥が焼けたかどうか
	private boolean toriGrill = false;

	// 剣を持っているかどうか
	private boolean hasSword = false;

  	// ドラゴンが存在しているかどうか
	private boolean existDragon = false;
  
	// 時間
	private double time, clearTime;
	private int remain = 61; 
	private double start = System.currentTimeMillis(), end;

	// クリアとゲームオーバー
	private boolean gameOver = false;
	private boolean gameClear = false;


	private Thread th;

  	// コンストラクタ
  	LabyrinthPanel() {

		wall = new boolean[labyrinthSize_y + 1][labyrinthSize_x + 1];
		newGame();

    	try {
      		// 画像の読み込み
    		for (int i = 0; i < 4; i++) {
        		File toriFile = new File("figure/tori0" + (i + 1) + ".gif");
        		toriImg[i] = ImageIO.read(toriFile);
			}

      		File yakitoriFile = new File("figure/yakitori.gif");
    		File mikanFile = new File("figure/mikan.gif");
			File dragonFile = new File("figure/dragon.gif");
			File fireFile = new File("figure/fire.gif");
			File swordFile = new File("figure/sword.gif");

      		yakitoriImg = ImageIO.read(yakitoriFile);
      		mikanImg = ImageIO.read(mikanFile);
			dragonImg = ImageIO.read(dragonFile);
			fireImg = ImageIO.read(fireFile);
			swordImg = ImageIO.read(swordFile);

    	} catch (IOException e) {
      		System.err.println("ファイルの読み込みに失敗しました．");
    	}

    	// マップの周囲を見えない壁で囲む
    	map = new char[MY+2][MX+2];
    	for (int x = 0; x <= MX+1; x++) {
      		map[0][x] = 'B'; 
      		map[MY+1][x] = 'B';
    	}
    	for (int y = 0; y <= MY+1; y++) {
      		map[y][0] = 'B'; 
      		map[y][MX+1] = 'B';
    	} 

		time = System.currentTimeMillis() * 0.001 + remain;

    		addKeyListener(this); // KeyListenerのリスナーオブジェクトをpanelに登録する
    		setFocusable(true); // JPanelでキーボード入力を受け付けるようフォーカスを当てる

 		th = new Thread(this);
    		th.start();
  	}

  	// 描画処理
  	@Override
  	public void paintComponent(Graphics g) {
    	for (int y = 1; y <= MY; y++) {
      		for (int x = 1; x <= MX; x++) {
        		int xx = size*x+20, yy = size*y+20;
        		switch ( map[y][x] ) {
   				
        			case 'B': // ブロックの描画 
						g.setColor(Color.green);
						g.fillRect(xx, yy, 25, 25);
						break;
   				
        			case 'M': // みかんの描画 
						g.drawImage(mikanImg, xx-3, yy-3, this);
						break;       			
								
					case 'S': // 剣の描画
						if (!hasSword) {
							swordDraw(g);
						};
        		}
      		}
    	} 
		
    	// 鳥の描画 
    	if ( !toriGrill ) {
			toriDraw(g);
		} else {
			yakitoriDraw(g);
		}

		// ドラゴンの描画
		dragonDraw(g);

		// 火球の描画
		if ( existDragon ) {
			fireDraw(g);
		}

		displayTime(g);

  	}

 	// Runnableインタフェースのメソッドrun()の実装
  	@Override
  	public void run() {
    	while (th != null) {
      		printLabyrinth(); 
			fire_x -= 40;  // 火球の移動
			if ( fire_x < 0 && existDragon ) fireSet();  // 火球を再起的に発射する
			if ( gameOver ) toriGrill = true;
			if ( (tori_x >= dragon_x && tori_x <= dragon_x + 2) && (tori_y >= dragon_y && tori_y <= dragon_y + 2) ) {
				if ( hasSword || !existDragon ) {
 					dragonErase();
				} else {
					gameOver = true;
				}
			}
			if ( (size*tori_x+25 >= fire_x && size*tori_x+25 <= fire_x + 80) && (size*tori_y+15 >= fire_y && size*tori_y+15 <= fire_y + 30) ) 
				gameOver = true;
			if (tori_x == goal_x && tori_y == goal_y) {
				gameClear = true;
				end = System.currentTimeMillis();
				clearTime = (end - start) * 0.001;
				repaint();
				break;
			}
      		repaint(); 
      		sleep(100);
    	}
  	}

  	public void sleep(int time){
    		try { Thread.sleep(time); }
    		catch (InterruptedException e) {}
  	}

  	// KeyListenerのメソッドkeyPressed
  	@Override
    public void keyPressed(KeyEvent e) {
    	int key = e.getKeyCode();
		dragonMove();
    	int dir = -1;
    	switch ( key ) {
    		case KeyEvent.VK_LEFT: // 左
				dir = 2; 
				toriNo = 2;
				break;  
    		case KeyEvent.VK_RIGHT: // 右
				dir = 0; 
				toriNo = 0;
				break;  
    		case KeyEvent.VK_UP: // 上
				dir = 1; 
				toriNo = 1;
				break;  
    		case KeyEvent.VK_DOWN: // 下
				dir = 3; 
				toriNo = 3;
				break;  
			case KeyEvent.VK_SPACE: // リスタート
				dir = -2; 
				break;  
			case KeyEvent.VK_ENTER: // ニューゲーム
				dir = -3; 	
				break;  
    	}
    	if ( dir >= 0 ) toriMove(dir);
		if ( dir == -2 ) toriSet(start_x, start_y);
		if ( dir == -3 ) {
			newGame();
			return;
		}
    	repaint();
    }

  	// KeyListenerのメソッドkeyReleased
  	@Override
    public void keyReleased(KeyEvent e) { }

  	// KeyListenerのメソッドkeyTyped
  	@Override
   	public void keyTyped(KeyEvent e) { }

  	// 鳥の各種メソッド定義 
  	public void toriSet(int x, int y) {
    	tori_x = x;
    	tori_y = y;
  	}

  	public void toriDraw(Graphics g) {
		switch ( toriNo ) {
     		case 0: // 右
				g.drawImage(toriImg[0], size*tori_x-28, size*tori_y-20, this); 
				break;
     		case 1: // 上
				g.drawImage(toriImg[1], size*tori_x-23, size*tori_y-20, this); 
				break;
     		case 2: // 左
				g.drawImage(toriImg[2], size*tori_x-28, size*tori_y-20, this); 
				break;
     		case 3: // 下
				g.drawImage(toriImg[3], size*tori_x-18, size*tori_y-15, this); 
				break;
		}
  	}

  	public void toriMove(int dir) {
  		int dx = 0, dy = 0;
    		if ( !toriGrill ) {
			switch ( dir ) {
    			case 0: dx =  1; break; // right
    			case 1: dy = -1; break; // up
    			case 2: dx = -1; break; // left
    			case 3: dy =  1; break; // down
    		}
    		if ( dx == 0 && dy == 0 ) return;
    		if ( map[tori_y+dy][tori_x+dx] == 'B' ) return; // block
			if ( tori_x+dx > labyrinthSize_x  ) return; 
    		tori_x += dx; tori_y += dy;
			if ( map[tori_y][tori_x] == 'S' ) getSword();
		} else return;
  	}

	// 焼き鳥の描画メソッド
	public void yakitoriDraw(Graphics g) {
		g.drawImage(yakitoriImg, size*tori_x-35, size*tori_y-20, this);
	}


	// ドラゴンの各種メソッド定義
 	public void dragonDraw(Graphics g) {
    	if ( dragonSize < -7 ) return;
    	switch ( dragonSize ) {
    		case 30: 
      			g.drawImage(dragonImg, size*dragon_x+15, size*dragon_y+10, this);
      			break;
			case 0:
			case -1:
			case -2:
    		case -3: 
			case -4:
			case -5:
			case -6:
      			int xx = size*dragon_x+50+10, yy = size*dragon_y+45+20;
      			g.setColor(Color.red);
      			g.drawLine(xx, yy, xx-dragonSize, yy-dragonSize);
      			g.drawLine(xx, yy, xx+dragonSize, yy-dragonSize);
      			g.drawLine(xx, yy, xx-dragonSize, yy+dragonSize);
      			g.drawLine(xx, yy, xx+dragonSize, yy+dragonSize);
      			break;
    		default:
      			g.drawImage(dragonImg, size*dragon_x+50+(10-dragonSize/4), size*dragon_y+45+(20-dragonSize/2), dragonSize/2, dragonSize, this);
  		}
	}

	public void dragonMove() {
		int dy = 0;

		if ( existDragon ) {
			if ( dragon_y < tori_y ) dy = 1;
			if ( dragon_y > tori_y ) dy = -1; 
   			if ( dy == 0 ) return;
			if ( dragon_y+dy + 2 > labyrinthSize_y ) dragon_y -= 1; // block
			if ( dragon_y+dy < 1 ) dragon_y += 1;
			dragon_y += dy;
		} else return;
	}

  	public void dragonErase() {
    	dragonSize -= 3;
		existDragon = false;
  	}

	// 火球の各種メソッド定義
	public void fireSet() {
		fire_x = size*dragon_x-20;
		fire_y = size*dragon_y+5;
	}

	public void fireDraw(Graphics g) {
    	g.drawImage(fireImg, fire_x, fire_y, this);
  	}

	// 剣の各種メソッド定義
	public void swordDraw(Graphics g) {
		g.drawImage(swordImg, size*sword_x + 18, size*sword_y + 18, this);
	}

  	public void getSword() {
    	hasSword = true;
  	}

	// ニューゲームのメソッド
	public void newGame() {
		createLabyrinth();
		resetStart();
		resetGoal();
		resetDragon();
		resetSword();
		toriNo = 0;
		dragonSize = size;
		gameOver = false;
		toriGrill = false;
		hasSword = false;
		existDragon = true;
		fireSet();
		toriSet(start_x, start_y);
		time = System.currentTimeMillis() * 0.001 + remain;
		start = System.currentTimeMillis();
	}

	public void displayTime(Graphics g) {
  		if ( gameOver ) {
    		g.setFont(new Font("TimeRoman", Font.BOLD, 200));
    		g.setColor(Color.red);
    		g.drawString("GAME OVER", 95, 540);
  		} else if ( gameClear ) {
			g.setFont(new Font("TimeRoman", Font.BOLD, 120));
    		g.setColor(Color.orange);
    		g.drawString("CONGRATULATIONS!", 65, 500);
			g.setFont(new Font("TimeRoman", Font.BOLD, 18));
			g.setColor(Color.cyan);
			g.drawString("Clear Time is " + clearTime + " seconds.", 55, 50);
  		} else {
			int dt = (int) (time - System.currentTimeMillis() * 0.001);
    		g.setFont(new Font("TimeRoman", Font.BOLD, 18));
    		g.setColor(Color.orange);
    		g.drawString("Time: " + dt, 40, 35);
    		if ( dt == 0 ) gameOver = true;
  		}
	}
}