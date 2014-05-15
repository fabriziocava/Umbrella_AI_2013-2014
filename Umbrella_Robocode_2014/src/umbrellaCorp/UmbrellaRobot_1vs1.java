package umbrellaCorp;
import java.awt.Color;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class UmbrellaRobot_1vs1 extends AdvancedRobot {
	
	final double maxDistance = 400;

	final int maxAttempts = 3;
	int currentAttempts = 0;
	
	final int maxHit = 3;
	int currentHit = 0;
	
	int turnDirection = 1;
		
	public void run() {
		setColors(Color.red,Color.white,Color.white);
		while(true) {
			turnGunRight(10);
			
		}
	}
	
	
	/*
	 * EVENT
	 */
	
	public void onCustomEvent(CustomEvent e) {
		
	}
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	public void onBulletHit(BulletHitEvent e) {
		currentAttempts = 0;
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	public void onBulletMissed(BulletMissedEvent e) {
		currentAttempts++;
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your robot is hit by bullet.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		currentHit++;
		if(currentHit>=maxHit) {
			/*
			 * movimento per seminare l'avversario
			 */
//			ahead(500);
//			turnLeft(30);
//			back(50);
//			turnRight(300);
			currentHit = 0;
			currentAttempts = 0;
		}
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Calculate exact location of the robot
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		// If it's close enough, fire!
		if(Math.abs(bearingFromGun)<=3) {
			turnGunRight(bearingFromGun);
			/*
			 * We check gun heat here, because calling fire()
			 * uses a turn, which could cause us to lose track
			 * of the other robot
			 */
			if(e.getDistance()<=maxDistance) {
				if(currentAttempts>=maxAttempts) {
					goOnBorder();
					currentAttempts = 0;
				}
				else {
					if(getGunHeat()==0)
						fire(calculatePower(e.getDistance()));
				}
			}
			else {
				/*
				 * Move to robot
				 */
				if(e.getBearing()>=0)
					turnDirection = 1;
				else
					turnDirection = -1;
				turnRight(e.getBearing());
				//setTurnGunRight(e.getBearing());
				ahead(getCloser(e.getDistance()));
			}
		} // otherwise just set the gun to turn.
		// Note: This will have no effect until we call scan()
		else {
			turnGunRight(bearingFromGun);
		}
		/*
		 * Generates another scan event if we see a robot.
		 * We only need to call this if the gun (and therefore radar)
		 * are not turning. Otherwise, scan is called automatically.
		 */
		if(bearingFromGun==0) {
			scan();
		}
	}

	/*
	 * ON HIT ROBOT
	 * is called when your robot crash another
	 */
	public void onHitRobot(HitRobotEvent e) {
		if(e.getBearing()>-10 && e.getBearing()<10) {
			fire(3);
			/*
			 * adeguare in base alla distanza
			 */
		}
		if(e.isMyFault()) {
			turnRight(10);
			/*
			 * cercare di non andare a sbattere ai muri
			 */
		}
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
	
	public double getCloser(double distance) {
		final double maxCloser = 200;
		return (maxCloser*distance/maxDistance);
	}
	
	/*
	 * END_UTILITY
	 */
	
}