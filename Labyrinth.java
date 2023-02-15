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
    	JFrame fr = new JFrame("Labyrinth Game");
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

	private int mode = 0;  // 画面モード （ 0:タイトル， 1:ゲーム中， 2:クリア時）

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
	public boolean canExtendRoadWithDir(int direction) {
		int exRoad_x = road_x, exRoad_y = road_y;

		switch (direction) {
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
	public void movePoint(int direction) {
		switch (direction) {
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

  	// キャラクター画像用
  	private BufferedImage yakitoriImg, mikanImg, mikan2Img, dragonImg, fireImg, swordImg ;
  	private Image[] toriImg = new Image[5];
  	private int tori_x, tori_y, toriNo;
	private int fire_x, fire_y;	
	private int size = 30; // サイズ調整用の変数
	private int dragonSize;

	// 鳥が焼けたかどうか
	private boolean toriGrill = false;

	// 剣を持っているかどうか
	private boolean getSword = false;

  	// ドラゴンを倒したかどうか
	private boolean beatDragon = false;
  
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

    	try {
      		// 画像の読み込み
    		for (int i = 0; i < 5; i++) {
        		File toriFile = new File("figure/tori0" + (i + 1) + ".gif");
        		toriImg[i] = ImageIO.read(toriFile);
			}
      		File yakitoriFile = new File("figure/yakitori.gif");
    		File mikanFile = new File("figure/mikan.gif");
			File mikan2File = new File("figure/mikan2.gif");
			File dragonFile = new File("figure/dragon.gif");
			File fireFile = new File("figure/fire.gif");
			File swordFile = new File("figure/sword.gif");

      		yakitoriImg = ImageIO.read(yakitoriFile);
      		mikanImg = ImageIO.read(mikanFile);
			mikan2Img = ImageIO.read(mikan2File);
			dragonImg = ImageIO.read(dragonFile);
			fireImg = ImageIO.read(fireFile);
			swordImg = ImageIO.read(swordFile);

    	} catch (IOException e) {
      		System.err.println("ファイルの読み込みに失敗しました．");
    	}

		addKeyListener(this); // KeyListenerのリスナーオブジェクトをpanelに登録する
    	setFocusable(true); // JPanelでキーボード入力を受け付けるようフォーカスを当てる
  	}

	// ゲーム初期化
	public void initGame() {
		wall = new boolean[labyrinthSize_y + 1][labyrinthSize_x + 1];

		createLabyrinth();
		resetStart();
		resetGoal();
		resetDragon();
		resetSword();
		toriNo = 2;
		dragonSize = size;
		gameOver = false;
		toriGrill = false;
		getSword = false;
		beatDragon = false;
		fireSet();
		toriSet(start_x, start_y);
		time = System.currentTimeMillis() * 0.001 + remain;
		start = System.currentTimeMillis();
	
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
 		
		th = new Thread(this);
    	th.start();
	}

  	// 描画処理
  	@Override
  	public void paintComponent(Graphics g) {
		if (mode == 0){
			// タイトル画面描画
			g.setColor(Color.black);
            //g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setFont(new Font("TimeRoman", Font.BOLD, 90));
            g.setColor(Color.orange);
            g.drawString("LABYRINTH", 340, 200);
			g.setFont(new Font("TimeRoman", Font.BOLD, 50));
			g.drawString("Take the Mikan", 410, 270);
			g.setFont(new Font("TimeRoman", Font.BOLD, 35));
            g.setColor(Color.green);
            g.drawString("Press Enter!!", 480, 600);
		} else if (mode == 1){
			// ゲーム画面描画
    		for (int y = 1; y <= MY; y++) {
      			for (int x = 1; x <= MX; x++) {
        			int xx = size*x+20, yy = size*y+20;
        			switch (map[y][x]) {
					
        				case 'B': // ブロックの描画 
							g.setColor(Color.green);
							g.fillRect(xx, yy, 25, 25);
							break;
					
        				case 'M': // みかんの描画 
							g.drawImage(mikanImg, xx-3, yy-3, this);
							break;       			

						case 'S': // 剣の描画
							if (!getSword) {
								swordDraw(g);
							};
        			}
      			}
    		} 
    		// 鳥の描画 
    		if (toriGrill) {
				yakitoriDraw(g);
			} else {
				toriDraw(g);
			}

			// ドラゴンの描画
			dragonDraw(g);

			// 火球の描画
			if (!beatDragon) {
				fireDraw(g);
			}

			if (gameOver) {
				g.setColor(Color.black);
				g.fillRect(100, 250, 955, 250);
				g.setFont(new Font("TimeRoman", Font.BOLD, 150));
    			g.setColor(Color.red);
    			g.drawString("GAME OVER", 120, 400);
				g.setFont(new Font("TimeRoman", Font.BOLD, 60));
    			g.setColor(Color.yellow);
    			g.drawString("Retry: Enter", 410, 470);
			} else {
				int dt = (int) (time - System.currentTimeMillis() * 0.001);
    			g.setFont(new Font("TimeRoman", Font.BOLD, 18));
				if (dt > 10) g.setColor(Color.orange);
				else g.setColor(Color.red);
    			g.drawString("Time: " + dt, 40, 35);
    			if (dt == 0) gameOver = true;
			}
		} else {
			// クリア画面描画
			g.setFont(new Font("TimeRoman", Font.BOLD, 90));
    		g.setColor(Color.yellow);
    		g.drawString("CONGRATULATIONS!", 90, 200);
			// クリアタイム
			//g.setFont(new Font("TimeRoman", Font.BOLD, 50));
			//g.setColor(Color.cyan);
			//g.drawString("Your Time is " + clearTime + " seconds.", 300, 300);

			g.setFont(new Font("TimeRoman", Font.BOLD, 60));
    		g.setColor(Color.yellow);
    		g.drawString("Back to Title: Enter", 300, 700);

			g.drawImage(toriImg[4], 320, 350, this); 
			g.drawImage(mikan2Img, 570, 350, this); 
		}
  	}

 	// Runnableインタフェースのメソッド run の実装
  	@Override
  	public void run() {
    	while (th != null && mode == 1) {
      		printLabyrinth();

			// ドラゴンとの当たり判定
			if ((tori_x >= dragon_x && tori_x <= dragon_x+2) && (tori_y >= dragon_y && tori_y <= dragon_y+2)) {
				if (!beatDragon) {
					if (getSword) {
						beatDragon = true;	
						time += 20;
					} else {
						gameOver = true;
					}
				}
			}

			// 火球との当たり判定
			if ((size*tori_x+25 >= fire_x && size*tori_x+25 <= fire_x+80) && (size*tori_y+15 >= fire_y && size*tori_y+15 <= fire_y + 30)) {
				gameOver = true;
			}
			// ゴール判定
			if (tori_x == goal_x && tori_y == goal_y) {
				gameClear = true;
				mode = 2;
				// end = System.currentTimeMillis();
				// clearTime = (end - start) * 0.001;
				repaint();
				break;
			}

			if (beatDragon) dragonErase();

			// 火球の移動
			if (getSword) fire_x -= 60;  
			else fire_x -= 40; 

			// ゲームオーバーかつ火球が消えたら停止
			if (gameOver) {
				toriGrill = true;
				if (fire_x < -80) break;
			}

			// 火球を再起的に発射する
			if (fire_x < -80 && !beatDragon) fireSet();  

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
		if (mode == 0) {
			// タイトル画面
			if (key == KeyEvent.VK_ENTER) {
				mode = 1; // ゲームモードに変更
				initGame(); // ゲームの初期化
			}
		} else if (mode == 1){		
			// ゲーム画面
			dragonMove();
    		switch (key) {
				case KeyEvent.VK_UP: // 上
					toriNo = 0;
					toriMove(toriNo);
					break;
				case KeyEvent.VK_DOWN: // 下
					toriNo = 1;
					toriMove(toriNo);
					break;
				case KeyEvent.VK_RIGHT: // 右
					toriNo = 2;
					toriMove(toriNo);
					break;
    			case KeyEvent.VK_LEFT: // 左
					toriNo = 3;	
					toriMove(toriNo);
					break;
				//case KeyEvent.VK_SPACE: // リスタート
				//	toriSet(start_x, start_y);
				//	break;  
				case KeyEvent.VK_ENTER: // ニューゲーム
					if (gameOver) initGame();	
					else mode = 0;
					break;  
    		}
		} else {
			// タイトル画面
			if (key == KeyEvent.VK_ENTER) {
				mode = 0;
			}
		}
		repaint();
    }

  	// KeyListenerのメソッドkeyReleased
  	@Override
    public void keyReleased(KeyEvent e) { }

  	// KeyListenerのメソッドkeyTyped
  	@Override
   	public void keyTyped(KeyEvent e) { }

  	// トリの各種メソッド定義 
  	public void toriSet(int x, int y) {
    	tori_x = x;
    	tori_y = y;
  	}

  	public void toriDraw(Graphics g) {
		switch (toriNo) {
			case 0: // 上
				g.drawImage(toriImg[0], size*tori_x-23, size*tori_y-20, this); 
				break;
			case 1: // 下
				g.drawImage(toriImg[1], size*tori_x-18, size*tori_y-15, this); 
				break;
     		case 2: // 右
				g.drawImage(toriImg[2], size*tori_x-28, size*tori_y-20, this); 
				break;	
     		case 3: // 左
				g.drawImage(toriImg[3], size*tori_x-28, size*tori_y-20, this); 
				break;  		
		}
  	}

  	public void toriMove(int direction) {
  		int dx = 0, dy = 0;
    	if (!toriGrill) {
			switch (direction) {
    			case 0: dy = -1; break; // up
    			case 1: dy =  1; break; // down
    			case 2: dx =  1; break; // right
    			case 3: dx = -1; break; // left
    		}
    		if (map[tori_y+dy][tori_x+dx] == 'B') return; // block

			tori_x += dx;
			tori_y += dy;

			if (map[tori_y][tori_x] == 'S') getSword = true;	
		} else return;
  	}

	// 焼き鳥の描画メソッド
	public void yakitoriDraw(Graphics g) {
		g.drawImage(yakitoriImg, size*tori_x-30, size*tori_y-30, this);
	}

	// ドラゴンの各種メソッド定義
 	public void dragonDraw(Graphics g) {
    	if (dragonSize < -4) return;
    	switch (dragonSize) {
    		case 30: 
      			g.drawImage(dragonImg, size*dragon_x+15, size*dragon_y+10, this);
      			break;
			case -3:
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
		if (beatDragon) return;
		else {
			int dy = 0;
			if (dragon_y < tori_y) dy = 1;
			else if (dragon_y > tori_y) dy = -1; 

			if (dragon_y+dy + 2 > labyrinthSize_y) dragon_y -= 1; // block
			if (dragon_y+dy < 1 ) dragon_y += 1;

			dragon_y += dy;
		}
	}

  	public void dragonErase() {
		if (dragonSize < -4) return;
    	dragonSize -= 3;
  	}

	// 火球の各種メソッド定義
	public void fireSet() {
		fire_x = size*dragon_x-5;
		fire_y = size*dragon_y+8;
	}

	public void fireDraw(Graphics g) {
    	g.drawImage(fireImg, fire_x, fire_y, this);
  	}

	// 剣の各種メソッド定義
	public void swordDraw(Graphics g) {
		g.drawImage(swordImg, size*sword_x + 18, size*sword_y + 18, this);
	}

}