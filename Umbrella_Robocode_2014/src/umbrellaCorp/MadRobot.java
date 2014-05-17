package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;
import robocode.util.Utils;

public class MadRobot extends AdvancedRobot {
	
	WaveSurfing ws = new WaveSurfing(this);
	GuessFactorTargeting gft = new GuessFactorTargeting(this);
	
	private final int HIT_MAX = 5;
	private int currentHit = 0;
	
	public void run() {
		init();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		ws.init();
		do {
			turnRadarRight(Double.POSITIVE_INFINITY);
		} while(true);
	}
	
	

	/*
	 * EVENT
	 */
	
	@Override
	public void onCustomEvent(CustomEvent e) {
		
	}
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	@Override
	public void onBulletHit(BulletHitEvent e) {
		currentHit = 0;
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	@Override
	public void onBulletMissed(BulletMissedEvent e) {
		currentHit++;
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your robot is hit by bullet.
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		ws.onHitByBullet(e);
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		ws.onScannedRobot(e);
		gft.onScannedRobot(e);
		if(currentHit>=HIT_MAX) {
			goToAngle(e.getBearingRadians(), e.getDistance());
			currentHit = 0;
		}
			
	}

	/*
	 * ON HIT ROBOT
	 * is called when your robot crash another
	 */
	@Override
	public void onHitRobot(HitRobotEvent e) {
		if(e.getBearing()>-10 && e.getBearing()<10) {
			fire(3);
		}
		if(e.isMyFault()) {
			turnRight(10);
		}
	}
	
	/*
	 * ON HIT WALL
	 * is called when your robot crash with a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		
	}
	
	/*
	 * END_EVENT
	 */
	
	@Override
	public void onPaint(Graphics2D g) {
		ws.onPaint(g);
		gft.onPaint(g);
	}
	
	
	
	public void init() {
		setColors(Color.red,Color.white,Color.white);
	}
	
	public void goTo(double x, double y) {
		x -= getX();
		y -= getY();
		
		double angleToTarget = Math.atan2(x, y);
		double targetAngle = Utils.normalRelativeAngle(angleToTarget-getHeadingRadians());
		double distance = Math.hypot(x, y);
		double turnAngle = Math.atan(Math.tan(targetAngle));
		setTurnRightRadians(turnAngle);
		if(targetAngle==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
	}
	
	public void goToAngle(double angleToTarget, double distance) {
		double turnAngle = Math.atan(Math.tan(angleToTarget));
		setTurnRightRadians(turnAngle);
		if(angleToTarget==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
	}
	
}

