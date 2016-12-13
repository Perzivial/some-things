package randomTerrainGen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Human {
	static Component comp;
	// gene stuff
	int eatWeight = 0;
	int treeWeight = 0;
	int buildWeight = 0;
	int multiplyWeight = 0;
	String genome = "";
	// movement stuff
	double x = Helper.randInt(10, 4990);
	double y = 20;
	double velX = 0;
	double velY = 0;
	double health = 100;
	double hunger = 50;
	int sleepDays = 0;
	int updateTimer = 20;
	Tree nearestTree = null;
	Berry nearestBerry = null;
	ArrayList<String> inventory = new ArrayList<String>();
	boolean isDead = false;

	public Human(int eat, int tree, int build, int steal) {
		eatWeight = eat;
		treeWeight = tree;
		buildWeight = build;
		multiplyWeight = steal;
	}

	/**
	 * The reason that im creating the genome as a string is so that i can
	 * mutate it later ;3
	 */
	public Human() {
		generateGenome();
		createStatsFromGenome();
	}

	public void draw(Graphics g) {
		if (!isDead) {
			move();
			g.setColor(Color.yellow);
		} else
			g.setColor(Color.yellow.darker());

		g.drawLine((int) x, (int) y, (int) x, (int) y - 10);
		g.fillRect((int) x - 2, (int) y - 12, 4, 4);

	}

	public void move() {

		x += velX;
		y += velY;
		gravity();
		groundPhysics();
		updateRelations();

		hunger();

		chooseAction();
	}

	public void die() {
		isDead = true;
	}

	public void hunger() {

		if (hunger <= 0)
			health -= 0.01388888889;
		else
			hunger -= 0.001388888889;

		if (hunger < 30)
			eat();
	}

	public void gravity() {
		velY += .1;
	}

	public void walkRight() {

		Rectangle rect = new Rectangle((int) x + 2, (int) y - 4, 2, 4);
		velX = .3;
		for (Block block : comp.blocks) {
			if (rect.intersects(block.rect))
				jump();
			if (rect.intersects(block.rect))
				velX = -.5;
		}
	}

	public void walkLeft() {
		Rectangle rect = new Rectangle((int) x - 2, (int) y - 4, 2, 4);
		velX = -.3;
		for (Block block : comp.blocks) {
			if (rect.intersects(block.rect))
				jump();
			if (rect.intersects(block.rect))
				velX = .5;
		}
	}

	public void jump() {
		if (isGrounded()) {
			velY = -1;
		}
	}

	public void chooseAction() {
		int eatNum = 0;
		int treeNum = 0;
		int buildNum = 0;
		int multiplyNum = 0;
		eatNum = 5 - Helper.amountOf("berry", inventory);
		treeNum = 5 - Helper.amountOf("log", inventory);
		buildNum = Helper.amountOf("log", inventory);
		eatNum *= eatWeight;
		treeNum *= treeWeight;
		buildNum = buildWeight;
		multiplyNum *= multiplyWeight;
		if(hasHouse())
			buildNum = Integer.MIN_VALUE;
		System.out.println("eatnum " + eatNum);
		System.out.println("treenum " + treeNum);
		System.out.println("buildnum " + buildNum);
		System.out.println("multiplynum " + multiplyNum);
		switch (Helper.weigh(eatNum, treeNum, buildNum, multiplyNum) + 1) {
		case 1:
			goToNearestBerry();
			break;
		case 2:
			goToNearestTree();
			break;
		case 3:
			buildHouse();
			break;
		case 4:
			// multiplying
			break;
		}
	}

	public void eat() {
		inventory.remove("berry");
		hunger += 10;
	}

	// actions
	public void findNearestTree() {

		for (Tree tree : comp.trees) {
			if (nearestTree == null)
				nearestTree = tree;
			else if (Math.abs(tree.x - x) < Math.abs(nearestTree.x - x))
				nearestTree = tree;
		}

	}
	
	public void goToNearestTree() {
		if (nearestTree != null) {
			if (nearestTree.x < x - 5)
				walkLeft();
			else if (nearestTree.x > x + 5)
				walkRight();
			else {
				velX = 0;
				chopTree();
			}
		}
	}

	public void chopTree() {
		nearestTree.health--;
		if (nearestTree.health <= 0) {
			comp.trees.remove(nearestTree);
			inventory.add("log");
			nearestTree = null;
		}
	}

	// actions
	public void findNearestBerry() {

		for (Berry berry : comp.berries) {
			if (nearestBerry == null)
				nearestBerry = berry;
			else if (Math.abs(berry.x - x) < Math.abs(nearestBerry.x - x))
				nearestBerry = berry;
		}

	}

	public void goToNearestBerry() {
		if (nearestBerry != null) {
			if (nearestBerry.x < x - 5)
				walkLeft();
			else if (nearestBerry.x > x + 5)
				walkRight();
			else {
				velX = 0;
				pickBerry();
			}
		}
	}

	public void pickBerry() {
		comp.berries.remove(nearestBerry);
		inventory.add("berry");
		inventory.add("berry");
		nearestBerry = null;
	}

	public void buildHouse() {
		comp.houses.add(new House(genome, (int) x, (int) y));
		for (int i = 0; i < 3; i++) {
			inventory.remove("log");
		}
	}

	public boolean hasHouse(){
		for(House house: comp.houses){
			if(house.creatorGenome == genome)
				return true;
		}
		return false;
	}
	
	public void updateRelations() {
		updateTimer--;
		if (updateTimer <= 0) {

			findNearestTree();
			findNearestBerry();

			updateTimer = 20;
		}
	}

	public boolean isGrounded() {
		Point point = new Point();
		point.setLocation(x, y);
		for (Block block : comp.blocks) {
			if (block.rect.contains(point))
				return true;
		}
		return false;
	}

	public void groundPhysics() {
		if (isGrounded()) {
			velY = 0;
		}
		Rectangle rect = new Rectangle((int) x - 1, (int) y - 4, 2, 4);
		for (Block block : comp.blocks) {
			if (rect.intersects(block.rect)) {
				y--;
			}
			Rectangle rectleft = new Rectangle((int) x - 1, (int) y - 4, 2, 4);
			Rectangle rectright = new Rectangle((int) x + 1, (int) y - 4, 2, 4);

		}

	}

	public void createStatsFromGenome() {
		for (char gene : genome.toCharArray()) {
			int num = (int) gene;
			if (num < 43)
				multiplyWeight++;
			else if (num < 73)
				treeWeight++;
			else if (num < 90)
				buildWeight++;
			else
				eatWeight++;
		}
		System.out.println(this);
	}

	@Override
	public String toString() {
		String out = "";
		out += "Eat Weight : " + eatWeight + "\n";
		out += "Tree Weight : " + treeWeight + "\n";
		out += "Build Weight : " + buildWeight + "\n";
		out += "Multiplication Weight : " + multiplyWeight + "\n";
		return out;
	}

	public void generateGenome() {
		for (int i = 0; i < 20; i++) {
			genome += (char) Helper.randInt(33, 126);
		}
		System.out.println("The genome is " + genome);
	}

	public static Component getComp() {
		return comp;
	}

	public static void setComp(Component comp) {
		Human.comp = comp;
	}
}
