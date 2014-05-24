package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

class Enemy {
	public Point2D.Double location;
	public double energy;
	public boolean alive;
}


/*
 * Considera una serie di punti tra cui scegliere quello in cui andare.
 * Ad ogni punto è infatti associato un valore che esprime il "fattore di rischio".
 * Il robot si sposta quindi verso il punto con il più basso fattore di rischio.  
 */

public class MinimumRiskMovement {
	
	public static HashMap<String, Enemy> enemies = new HashMap<String, Enemy>();
	private Enemy target;
	private Point2D.Double nextLocation;
	public static Point2D.Double lastLocation;
	public static Point2D.Double myLocation;
	public static double myEnergy;
	
	private Rectangle2D.Double battleField;
	
	MadRobot mr;
	
	public MinimumRiskMovement(MadRobot mr) {
		this.mr=mr;
	}
	
	public void init() {
		myLocation = new Point2D.Double(mr.getX(), mr.getY());
		target = new Enemy();
		nextLocation = myLocation;
		lastLocation = myLocation;
		mr.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		//battleField= new Rectangle2D.Double(18, 18, mr.getBattleFieldWidth()-36, mr.getBattleFieldHeight()-36);
		battleField = new Rectangle2D.Double(30, 30, mr.getBattleFieldWidth()-60, mr.getBattleFieldHeight()-60);
	}
	
	public void run() {
		myLocation = new Point2D.Double(mr.getX(), mr.getY());
		myEnergy = mr.getEnergy();
		//mr.turnRadarRightRadians(2*Math.PI);
		if(target.alive && mr.getTime()>9)
			doMovementAndGun();
	}
	
	private void doMovementAndGun() {
		double distanceToTarget = myLocation.distance(target.location);
		
		/*
		 * GUN
		 */
		
		if(mr.getGunTurnRemaining()==0 && myEnergy>1) {
//			mr.setFire(Math.min(Math.min(myEnergy/6d, 1300d/distanceToTarget), target.energy/3d));
//			if(distanceToTarget<400)
				mr.setFire(optimalPower(distanceToTarget));
		}
		mr.setTurnGunRightRadians(Utils.normalRelativeAngle(Util.absoluteBearing(myLocation, target.location)-mr.getGunHeadingRadians()));
		
		/*
		 * END_GUN
		 */
		
		/*
		 * MOVE
		 */
		double distanceToNextDestination = myLocation.distance(nextLocation);
		if(distanceToNextDestination<15) {
			double addLast = 1-Math.rint(Math.pow(Math.random(), mr.getOthers()));
			Point2D.Double testPoint;
			int i=0;
			do {
				testPoint = Util.project(myLocation, 2*Math.PI*Math.random(), Math.min(distanceToTarget*0.8, 100 + 200*Math.random()));
				if(battleField.contains(testPoint) && riskEvaluation(testPoint, addLast) < riskEvaluation(nextLocation, addLast)) {
					nextLocation = testPoint;
				}
			} while(i++<200);
			lastLocation = myLocation;
		} else {
			double angle = Util.absoluteBearing(myLocation, nextLocation) - mr.getHeadingRadians();
			double direction = 1;

			if(Math.cos(angle) < 0) {
				angle += Math.PI;
				direction = -1;
			}

			mr.setAhead(distanceToNextDestination*direction);
			mr.setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
			mr.setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);
		}
		/*
		 * END_MOVE
		 */
	}
	
	/*
	 * Funzione di rischio - Determina quanto e' rischioso andare in un punto.
	 * Il rischio e' basato sull'energia del nemico
	 * */
	
	public static double riskEvaluation (Point2D.Double point, double addLast) {
		double eval = addLast*0.08/point.distanceSq(lastLocation);
		for (String key:enemies.keySet()) {
			Enemy enemy = enemies.get(key);
			if(enemy.alive) {
				eval += Math.min(enemy.energy/myEnergy,2) * 
						(1 + Math.abs(Math.cos(Util.absoluteBearing(point, myLocation) - Util.absoluteBearing(point, enemy.location)))) / point.distanceSq(enemy.location);
			}
		}
		return eval;
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		Enemy enemy = enemies.get(e.getName());
		if(enemy==null) {
			enemy = new Enemy();
			enemies.put(e.getName(), enemy);
		}
		enemy.energy = e.getEnergy();
		enemy.alive = true;
		enemy.location = Util.project(myLocation, mr.getHeadingRadians()+e.getBearingRadians(), e.getDistance());
		if(!target.alive || e.getDistance()<myLocation.distance(target.location)) {
			target = enemy;
		}
//		if(mr.getOthers()==1)
//			mr.setTurnGunLeftRadians(mr.getRadarTurnRemainingRadians());
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		enemies.get(e.getName()).alive=false;
	}
	
	public double optimalPower(double distance) {
		final double minPower = 0.1;
		final double maxPower = 3.0;
		final double minDistance = 200;
		double currentEnergy = mr.getEnergy();
		double power;
		if(distance<=minDistance)
			power = maxPower;
		else 
			power = maxPower*minDistance/distance;
		if(currentEnergy<power)
			power=minPower;
		return power;
	}
	
	public void onPaint(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.drawRect((int)nextLocation.x, (int)nextLocation.y, 10, 10);

		Point2D.Double testPoint;
		int i=0;
		double distanceToTarget = myLocation.distance(target.location);

		ArrayList<Point2D.Double> points=new ArrayList<Point2D.Double>();
		do {
			testPoint = Util.project(myLocation, 2*Math.PI*Math.random(), Math.min(distanceToTarget*0.8, 100 + 200*Math.random()));
			if(battleField.contains(testPoint))
				points.add(testPoint);
		} while(i++ < 200);

		for(Point2D.Double p : points){
			g.setColor(new Color(0,100,255));
			g.drawRect((int)p.x, (int)p.y, 10, 10);
		}
	}

}