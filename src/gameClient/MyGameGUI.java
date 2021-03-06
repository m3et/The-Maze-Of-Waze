package gameClient;


import gameComponent.Fruit;
import gameComponent.Robot;
import graph.dataStructure.DGraph;
import graph.dataStructure.edge_data;
import graph.dataStructure.node_data;
import graph.utils.Point3D;
import graph.utils.Range;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 * @authors Lee Fingerhut and Raphael Gozlan
 */
public class MyGameGUI extends JFrame implements ActionListener, MouseListener, Runnable {


	private PlayGround playGround;
	/**
	 * this is the screen parameters
	 */
	protected static final int width = 1239;
	protected static final int height = 595;
	/**
	 * range of node in game
	 */
	private Range rangeX;
	private Range rangeY;
	/**
	 * the graph in the game
	 **/
	private BufferedImage gameLayout;   //Buffer for the graph

	private static DecimalFormat df2 = new DecimalFormat("#.##");


	/**
	 * INIT game
	 **/
	public MyGameGUI() {
		initGUI();
	}

	/**
	 * INIT the screen with all menu's and parameters
	 */
	private void initGUI() {
		//ask for the level of the game and playing type
		int loginID = askForLogin();
		int level = askForLevel();
		boolean manuel = askForPlayType(level);

		//init the Playground
		playGround = new PlayGround(level,manuel,loginID);

		//init the game graph
		playGround.graph = playGround.getGraph();
		//set the window parameters
		this.setSize(width, height);
		this.setTitle("My Game");
		this.setLocationRelativeTo(null);
		this.setFocusable(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		//set the points range of the graph
		setRangeX();
		setRangeY();

		//get the list of fruits and add them to the game
		playGround.getFruits();
		//addRobots
		if (manuel)
			playGround.addRobots();
		else
			playGround.addRobotsAuto();

		playGround.getRobots();
		playGround.startGame();

		Thread gamePlay = new Thread(this);
		gamePlay.start();
		this.addMouseListener(this);
	}

	private int askForLogin() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);

		String ID;
		do{
			ID = JOptionPane.showInputDialog("Enter your ID:");

			if (ID == null|| ID.length() != 9 )
				JOptionPane.showMessageDialog(this, "Please Enter a 9 char length ID",
						"ERROR", JOptionPane.ERROR_MESSAGE);
		}while (ID == null || ID.length() != 9 );

		return Integer.parseInt(ID);
	}

	@Override
	public void run() {
		while (playGround.isRunning() ) {

			long dt = 100;
			try{
				playGround.getRobots();
				playGround.getFruits();
				playGround.moveRobots();
				repaint();
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		playGround.stopGame();

		endGameScreen();
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void paint(Graphics g){
		if (gameLayout == null){
			paintComponent(g);
		}
		g.drawImage(gameLayout,0,0,this);

		//Show time to End and Grade
		Font font = new Font("Bold", Font.PLAIN,15);
		g.setFont(font);
		g.drawString("Level: " + playGround.getLevel(),width - width/6, height / 9);
		g.drawString("Time to End: " + playGround.timeToEndStr(),width - width/6, height / 9 + 25);
		g.drawString("Grade: " + playGround.getGrade(),width - width/6  , height / 9 + 50);
		g.drawString("Moves: " + playGround.getMoveNumber(),width - width/6  , height / 9 + 75);


		for (Fruit f : playGround.fruits)
			g.drawImage(f.getImg(),(int)rescaleX(f.getLocation().x()) - 8 ,(int)(rescaleY(f.getLocation().y())) - 8 ,this);
		for (Robot r : playGround.robots)
			g.drawImage(r.getImg(),(int)rescaleX(r.getLocation().x()) - 8 ,(int)(rescaleY(r.getLocation().y())) - 8  ,this);

	}

	/**
	 * paint a representation of a graph
	 *
	 * //@param g - DGraph
	 */
	public void paintComponent(Graphics g) {
		if (playGround.graph.nodeSize() == 0)
			return;
		gameLayout = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		DGraph graph = playGround.graph;
		Graphics2D graphics = gameLayout.createGraphics();
		graphics.drawImage(new ImageIcon(playGround.getMap()).getImage(),0,0,this);
		for (node_data p : graph.getV()) {
			double xPixel = rescaleX(p.getLocation().x());
			double yPixel = rescaleY(p.getLocation().y());
			graphics.setColor(Color.BLUE);
			graphics.fillOval((int) xPixel, (int) yPixel, 18, 18);
			graphics.setColor(Color.black);
			graphics.drawRect((int) xPixel, (int) yPixel, 18, 18);
			graphics.setFont(new Font("TimesRoman", Font.BOLD, 15));
			graphics.drawString(String.valueOf(p.getKey()), (int) xPixel - 2, (int) yPixel - 8);
		}
		for (node_data p : graph.getV()) {
			if (graph.getE(p.getKey()) != null) {
				for (edge_data edge : graph.getE(p.getKey())) {
					node_data src = graph.getNode(edge.getSrc());
					node_data dest = graph.getNode(edge.getDest());
					double srcXPixel = rescaleX(src.getLocation().x());
					double srcYPixel = rescaleY(src.getLocation().y());
					double destXPixel = rescaleX(dest.getLocation().x());
					double destYPixel = rescaleY(dest.getLocation().y());
					graphics.setColor(Color.RED);
					graphics.setStroke(new BasicStroke(2));
					Line2D line = new Line2D.Float(
							(int) srcXPixel + 8,
							(int) srcYPixel + 8,
							(int) destXPixel + 8,
							(int) destYPixel + 8);
					graphics.setStroke(new BasicStroke((float) 2.5));
					graphics.draw(line);
					graphics.setColor(Color.yellow);
					graphics.fillOval((int) ((srcXPixel * 10 + destXPixel * 2) / 12) + 3, (int) ((srcYPixel * 10 + destYPixel * 2) / 12) + 3 , 10, 10);
					graphics.setColor(Color.DARK_GRAY);
					graphics.drawString(String.valueOf(df2.format(edge.getWeight())), (int) ((srcXPixel * 2 + destXPixel) / 3) , (int) ((srcYPixel * 2 + destYPixel) / 3));
				}
			}
		}
		Graphics2D layoutCan = (Graphics2D)g;
		layoutCan.drawImage(gameLayout,null,0,0);

	}

	/**
	 * ask from player what level he wants to play
	 * @return int number of chosen level to play
	 */
	private int askForLevel() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		ImageIcon icon = new ImageIcon("src/Utils/icon/robot.png");
		Image image = icon.getImage(); // transform it
		Image newimg = image.getScaledInstance(60, 60,  Image.SCALE_SMOOTH); // scale it the smooth way
		icon = new ImageIcon(newimg);  // transform it back
		Object[] possibilities = {
				"0","1","2","3","4","5","6",
				"7","8","9","10","11","12",
				"13","14","15","16","17","18",
				"19","20","21","22","23","-31"};
		String s;
		do {
			s = (String)JOptionPane.showInputDialog(
					frame,
					"Choose level to play:\n",
					"Level",
					JOptionPane.PLAIN_MESSAGE,
					icon,
					possibilities,
					"0");
			if (s == null)
				JOptionPane.showMessageDialog(this, "Please Choose a Level Number",
						"ERROR", JOptionPane.ERROR_MESSAGE);
		}while (s == null);
		return Integer.parseInt(s);
	}

	/**
	 * ask from player if he wants to play manually or automatic
	 * @return true if chosen to play manually
	 */
	private boolean askForPlayType(int level) {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		ImageIcon icon = new ImageIcon("src/Utils/icon/robot.png");
		Image image = icon.getImage(); // transform it
		Image newimg = image.getScaledInstance(60, 60,  Image.SCALE_SMOOTH); // scale it the smooth way
		icon = new ImageIcon(newimg);  // transform it back
		Object[] possibilities = {"Manually","Automatic"};
		String s;
		do {
			s = (String)JOptionPane.showInputDialog(
					frame,
					"Choose method to play level:" + level +"\n",
					"Level" ,
					JOptionPane.PLAIN_MESSAGE,
					icon,
					possibilities,
					"0");
			if (s == null)
				JOptionPane.showMessageDialog(this, "Please Choose a method to play",
						"ERROR", JOptionPane.ERROR_MESSAGE);
		}while (s == null);
		return s.equals("Manually");
	}

	/**
	 * show the grade in a gui message
	 */
	private void endGameScreen() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


		JOptionPane.showMessageDialog(
				frame,
				playGround.getStats() + "\n",
				"Game Over",
				JOptionPane.INFORMATION_MESSAGE
				);

	}

	/**
	 * @param data denote some data to be scaled
	 * @param r_min the minimum of the range of your data
	 * @param r_max the maximum of the range of your data
	 * @param t_min the minimum of the range of your desired target scaling
	 * @param t_max the maximum of the range of your desired target scaling
	 * @return relative resolution based of given parameters
	 */
	private double rescale(double data, double r_min, double r_max, double t_min, double t_max) {
		return ((data - r_min) / (r_max-r_min)) * (t_max - t_min) + t_min;
	}
	/**
	 * @param x,y - location of data
	 * @return resolution of x/y with screen setting
	 * using rescale method
	 */
	private double rescaleX(double x) {
		return rescale(x,rangeX.get_min(),rangeX.get_max(),width*0.1,width - width*0.1);

	}
	private double rescaleY(double y) {
		return height - rescale(y,rangeY.get_min(),rangeY.get_max(),height*0.1,height - height*0.1);
	}

	/**
	 * set the RangeX of the graph Range[minX,maxX]
	 * go over all nodes and find min,max X
	 */
	private void setRangeX(){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(node_data node : playGround.graph.getV()) {
			if (node.getLocation().x() > max)
				max = node.getLocation().x();
			if (node.getLocation().x() < min)
				min = node.getLocation().x();
		}

		rangeX = new Range(min,max);
	}
	/**
	 * set the RangeY of the graph Range[minY,maxY]
	 * go over all nodes and find min,max Y
	 */
	private void setRangeY(){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(node_data node : playGround.graph.getV()) {
			if (node.getLocation().y() > max)
				max = node.getLocation().y();
			if (node.getLocation().y() < min)
				min = node.getLocation().y();
		}

		rangeY = new Range(min,max);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		playGround.lastPressed(new Point3D(e.getX(),e.getY()));

	}

	@Override
	public void mousePressed(MouseEvent e){

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

}