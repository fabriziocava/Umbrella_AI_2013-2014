package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;

public class MadRobot extends AdvancedRobot {
	
	WaveSurfing ws = new WaveSurfing(this);
	GuessFactorTargeting gft = new GuessFactorTargeting(this);
	
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
		
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	@Override
	public void onBulletMissed(BulletMissedEvent e) {

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
	
}

