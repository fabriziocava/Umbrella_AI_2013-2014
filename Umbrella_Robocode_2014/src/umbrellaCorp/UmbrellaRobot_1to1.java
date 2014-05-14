package umbrellaCorp;
import java.awt.Color;

import robocode.*;

public class UmbrellaRobot_1to1 extends AdvancedRobot {
	
	public void run() {
		setColors(Color.red,Color.white,Color.white);	
		while(true) {
			
		}
	}
	
	
	/*
	 * EVENT
	 */
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	public void onBulletHit(BulletHitEvent e) {
	
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your bit is hit.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		fire(1);
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
	
	
	
}
