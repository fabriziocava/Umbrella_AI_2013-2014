package umbrellaCorp;
import java.awt.Color;

import robocode.*;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class CopyOfUmbrellaRobot_1vs1 extends AdvancedRobot {
	
	private int moveDirection = 1;
	private int wallMargin = 60; 
	
	private double idealDistance;
	private double enemyDistance = 0.0;
	private double enemyBearing = 0.0;
	
	public void run() {
		idealDistance = Math.max(getBattleFieldWidth()/2, getBattleFieldHeight()/2);
				
		setColors(Color.red,Color.white,Color.white);
		
		//goOnBorder();
//		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		turnGunRightRadians(Double.POSITIVE_INFINITY);
		while(true) {
			scan();
			//move();
			doMove();
		}
	}
	
	
	/*
	 * EVENT
	 */
	
	public void onStatus(StatusEvent e) {

	}
	
	public void onCustomEvent(CustomEvent e) {
		
	}
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	public void onBulletHit(BulletHitEvent e) {
		
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	public void onBulletMissed(BulletMissedEvent e) {
		
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your robot is hit by bullet.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double radarTurn = getHeadingRadians()+e.getBearingRadians()-getRadarHeadingRadians();
//		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
		setTurnGunRightRadians(Utils.normalRelativeAngle(radarTurn));
		enemyDistance = e.getDistance();
		enemyBearing = e.getBearing();
	}

	/*
	 * ON HIT ROBOT
	 * is called when your robot crash another
	 */
	public void onHitRobot(HitRobotEvent e) {

	}
	
	/*
	 * ON HIT WALL
	 * is called when your robot crash with a wall
	 */
	public void onHitWall(HitWallEvent e) {
		
	}
	
	
	/*
	 * END_EVENT
	 */
	
	
	
	/*
	 * MOVEMENT
	 */
	
	public void goOnBorder() {
		if(getY()>getBattleFieldHeight()/2)
			ahead(getBattleFieldHeight()+getY());
		else
			ahead(getBattleFieldHeight()-getY());
	}
	
	public void move() {
		if(enemyDistance>=idealDistance) {
//			turnRight(enemyBearing);
//			ahead(Math.abs(idealDistance-enemyDistance));
		}
		
	}
	
	public void doMove() {
		if(getVelocity()==0)
			moveDirection *= -1;
		setTurnRight(enemyBearing+90);
		setAhead(100*moveDirection);
	}
	
	/*
	 * END_MOVEMENT
	 */
	
	
	/*
	 * UTILITY
	 */
	
	public double calculatePower(double distance) {
		final double maxPower = 7;
		final double minDistance = 70;
		double currentEnergy = getEnergy();
		
		double power;
		
		if(distance<=minDistance) {
			power = maxPower;
		}
		else {
			power = maxPower*minDistance/distance;
		}
		if(currentEnergy<power)
			power = currentEnergy;
		return power;
	}
	
	
	/*
	 * END_UTILITY
	 */
	
}
